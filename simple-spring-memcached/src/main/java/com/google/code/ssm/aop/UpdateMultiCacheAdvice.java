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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.api.UpdateMultiCache;
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
            final List<Object> dataList = this.<List<Object>>getUpdateData(annotationData, methodToCache, jp, retVal);
            final Class<?> jsonClass = getDataJsonClass(methodToCache, annotationData);
            
            final List<String> cacheKeys;
            if (annotationData.isReturnKeyIndex()) {
                @SuppressWarnings("unchecked")
                final List<Object> keyObjects = (List<Object>) retVal;
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            } else {
                final MultiCacheCoordinator coord = new MultiCacheCoordinator();
                coord.setAnnotationData(annotationData);
                // Get the list of objects that will provide the keys to all the cache values.
                coord.setKeyObjects(getKeyObjects(coord.getAnnotationData().getKeysIndex(), jp, coord.getMethod(), coord, annotation));

                // Create key->object and object->key mappings.
                coord.setHolder(convertIdObjectsToKeyMap(coord.getListObjects(), coord.getKeyObjects(), coord.getListIndexInKeys(),
                        coord.getAnnotationData()));
                // keySet is sorted
                cacheKeys = new ArrayList<String>(coord.getKey2Obj().keySet());
            }
            
            updateCache(cacheKeys, dataList, methodToCache, annotationData, jsonClass);
        } catch (Exception ex) {
            warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
    }

    protected void updateCache(final List<String> cacheKeys, final List<Object> returnList, final Method methodToCache,
            final AnnotationData annotationData, Class<?> jsonClass) {
        if (returnList.size() != cacheKeys.size()) {
            throw new InvalidAnnotationException(String.format(
                    "The key generation objects, and the resulting objects do not match in size for [%s].", methodToCache.toString()));
        }

        for (int ix = 0; ix < returnList.size(); ix++) {
            final Object result = returnList.get(ix);
            final String cacheKey = cacheKeys.get(ix);
            final Object cacheObject = getSubmission(result);
            setSilently(cacheKey, annotationData.getExpiration(), cacheObject, jsonClass);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
