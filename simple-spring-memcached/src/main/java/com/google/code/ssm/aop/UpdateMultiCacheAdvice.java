/*
 * Copyright (c) 2008-2011 Nelson Carpentier, Jakub Białek
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

import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateMultiCacheOption;
import com.google.code.ssm.exceptions.InvalidAnnotationException;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
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
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateMultiCache.class,
                    methodToCache);
            final List<Object> dataList = this.<List<Object>> getUpdateData(annotationData, methodToCache, jp, retVal);
            final Class<?> jsonClass = getDataJsonClass(methodToCache, annotationData);
            final MultiCacheCoordinator coord = new MultiCacheCoordinator();
            coord.setAnnotationData(annotationData);
            coord.setAddNullsToCache(annotation.option().addNullsToCache());

            final List<String> cacheKeys;
            if (annotationData.isReturnKeyIndex()) {
                @SuppressWarnings("unchecked")
                final List<Object> keyObjects = (List<Object>) retVal;
                coord.setHolder(convertIdObjectsToKeyMap(keyObjects, annotationData));
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            } else {
                // Get the list of objects that will provide the keys to all the cache values.
                coord.setKeyObjects(getKeyObjects(coord.getAnnotationData().getKeyIndexes(), jp, coord.getMethod(), coord, annotation));

                // Create key->object and object->key mappings.
                coord.setHolder(convertIdObjectsToKeyMap(coord.getListObjects(), coord.getKeyObjects(), coord.getListIndexInKeys(),
                        coord.getAnnotationData()));
                // keySet is sorted
                cacheKeys = new ArrayList<String>(coord.getKey2Obj().keySet());
            }
            
            
            if (!annotation.option().addNullsToCache()) {
                updateCache(cacheKeys, dataList, methodToCache, annotationData, jsonClass);
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

    void updateCache(final List<String> cacheKeys, final List<Object> returnList, final Method methodToCache,
            final AnnotationData annotationData, Class<?> jsonClass) {
        if (returnList.size() != cacheKeys.size()) {
            throw new InvalidAnnotationException(String.format(
                    "The key generation objects, and the resulting objects do not match in size for [%s].", methodToCache.toString()));
        }

        Iterator<Object> returnListIter = returnList.iterator();
        Iterator<String> cacheKeyIter = cacheKeys.iterator();
        while(returnListIter.hasNext()) {
            final Object result = returnListIter.next();
            final String cacheKey = cacheKeyIter.next();
            final Object cacheObject = getSubmission(result);
            setSilently(cacheKey, annotationData.getExpiration(), cacheObject, jsonClass);
        }
    }

    private void updateCacheWithMissed(List<Object> dataUpdateContents, final MultiCacheCoordinator coord, final UpdateMultiCacheOption option, final Class<?> jsonClass) throws Exception {
        if (dataUpdateContents == null) {
            dataUpdateContents = new ArrayList<Object>();
        } else if (!dataUpdateContents.isEmpty()) {
            List<String> cacheKeys = getCacheKeys(dataUpdateContents, coord.getAnnotationData());
            String cacheKey;
            
            Iterator<String> iter = cacheKeys.iterator();
            for (Object resultObject : dataUpdateContents) {
                cacheKey = iter.next();
                setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject, jsonClass);
                coord.getMissObjects().remove(coord.getKey2Obj().get(cacheKey));
            }
        }

        if (option.overwriteNoNulls()) {
            setNullValues(coord.getMissObjects(), coord, jsonClass);
        } else {
            addNullValues(coord.getMissObjects(), coord, jsonClass);
        }
        
    }

}
