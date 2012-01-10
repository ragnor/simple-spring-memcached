/*
 * Copyright (c) 2008-2012 Nelson Carpentier, Jakub Białek
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package com.google.code.ssm.aop;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateMultiCacheOption;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Aspect
public class UpdateMultiCacheAdvice extends MultiCacheAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMultiCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.UpdateMultiCache)")
    public void updateMulti() {
    }

    @AfterReturning(pointcut = "updateMulti()", returning = "retVal")
    public void cacheUpdateMulti(final JoinPoint jp, final Object retVal) throws Throwable {
        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateMultiCache annotation = methodToCache.getAnnotation(UpdateMultiCache.class);
            final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateMultiCache.class, methodToCache);
            final List<Object> dataList = this.<List<Object>> getUpdateData(data, methodToCache, jp, retVal);
            final Class<?> jsonClass = getDataJsonClass(methodToCache, data);
            final MultiCacheCoordinator coord = new MultiCacheCoordinator(methodToCache, data);
            coord.setAddNullsToCache(annotation.option().addNullsToCache());

            final List<String> cacheKeys;
            if (data.isReturnKeyIndex()) {
                @SuppressWarnings("unchecked")
                final List<Object> keyObjects = (List<Object>) retVal;
                coord.setHolder(convertIdObjectsToKeyMap(keyObjects, data));
                cacheKeys = cacheKeyBuilder.getCacheKeys(keyObjects, data.getNamespace());
            } else {
                // Create key->object and object->key mappings.
                coord.setHolder(createObjectIdCacheKeyMapping(coord.getAnnotationData(), jp.getArgs(), coord.getMethod()));
                @SuppressWarnings("unchecked")
                List<Object> listKeyObjects = (List<Object>) Utils.getMethodArg(data.getListIndexInMethodArgs(), jp.getArgs(),
                        methodToCache.toString());
                coord.setListKeyObjects(listKeyObjects);
                // keySet is sorted
                cacheKeys = new ArrayList<String>(coord.getKey2Obj().keySet());
            }

            if (!annotation.option().addNullsToCache()) {
                updateCache(cacheKeys, dataList, methodToCache, data, jsonClass);
            } else {
                Map<String, Object> key2Result = new HashMap<String, Object>();
                for (String cacheKey : cacheKeys) {
                    key2Result.put(cacheKey, null);
                }
                coord.setInitialKey2Result(key2Result);
                updateCacheWithMissed(dataList, coord, annotation.option(), jsonClass);
            }
        } catch (Exception ex) {
            warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    MapHolder convertIdObjectsToKeyMap(final List<Object> idObjects, final AnnotationData data) throws Exception {
        final MapHolder holder = new MapHolder();

        for (final Object obj : idObjects) {
            if (obj == null) {
                throw new InvalidParameterException("One of the passed in key objects is null");
            }

            String cacheKey = cacheKeyBuilder.getCacheKey(obj, data.getNamespace());
            if (holder.getObj2Key().get(obj) == null) {
                holder.getObj2Key().put(obj, cacheKey);
            }
            if (holder.getKey2Obj().get(cacheKey) == null) {
                holder.getKey2Obj().put(cacheKey, obj);
            }
        }

        return holder;
    }

    void updateCache(final List<String> cacheKeys, final List<Object> returnList, final Method methodToCache, final AnnotationData data,
            final Class<?> jsonClass) {
        if (returnList.size() != cacheKeys.size()) {
            throw new InvalidAnnotationException(String.format(
                    "The key generation objects, and the resulting objects do not match in size for [%s].", methodToCache.toString()));
        }

        Iterator<Object> returnListIter = returnList.iterator();
        Iterator<String> cacheKeyIter = cacheKeys.iterator();
        while (returnListIter.hasNext()) {
            final Object result = returnListIter.next();
            final String cacheKey = cacheKeyIter.next();
            final Object cacheObject = getSubmission(result);
            getCache(data).setSilently(cacheKey, data.getExpiration(), cacheObject, jsonClass);
        }
    }

    private void updateCacheWithMissed(final List<Object> dataUpdateContents, final MultiCacheCoordinator coord,
            final UpdateMultiCacheOption option, final Class<?> jsonClass) throws Exception {
        if (!dataUpdateContents.isEmpty()) {
            List<String> cacheKeys = cacheKeyBuilder.getCacheKeys(dataUpdateContents, coord.getAnnotationData().getNamespace());
            String cacheKey;

            Iterator<String> iter = cacheKeys.iterator();
            for (Object resultObject : dataUpdateContents) {
                cacheKey = iter.next();
                getCache(coord.getAnnotationData()).setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject,
                        jsonClass);
                coord.getMissedObjects().remove(coord.getKey2Obj().get(cacheKey));
            }
        }

        if (option.overwriteNoNulls()) {
            setNullValues(coord.getMissedObjects(), coord, jsonClass);
        } else {
            addNullValues(coord.getMissedObjects(), coord, jsonClass);
        }
    }

}
