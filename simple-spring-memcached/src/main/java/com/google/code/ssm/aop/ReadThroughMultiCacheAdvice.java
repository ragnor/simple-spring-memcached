/*
 * Copyright (c) 2008-2013 Nelson Carpentier, Jakub Białek
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
import java.util.Iterator;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOption;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Aspect
public class ReadThroughMultiCacheAdvice extends MultiCacheAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughMultiCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.ReadThroughMultiCache)")
    public void getMulti() {
    }

    @Around("getMulti()")
    @SuppressWarnings("unchecked")
    public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
        if (isDisabled()) {
            getLogger().info("Cache disabled");
            return pjp.proceed();
        }

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        final ReadThroughMultiCache annotation;
        final MultiCacheCoordinator coord;
        final AnnotationData data;
        final SerializationType serializationType;

        Object[] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            final Method methodToCache = getCacheBase().getMethodToCache(pjp);
            getCacheBase().verifyReturnTypeIsList(methodToCache, ReadThroughMultiCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            annotation = methodToCache.getAnnotation(ReadThroughMultiCache.class);
            serializationType = getCacheBase().getSerializationType(methodToCache);

            data = AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughMultiCache.class, methodToCache);
            coord = new MultiCacheCoordinator(methodToCache, data);
            setMultiCacheOptions(coord, annotation.option());

            // Create key->object and object->key mappings.
            coord.setHolder(createObjectIdCacheKeyMapping(data, pjp.getArgs(), coord.getMethod()));

            List<Object> listKeyObjects = (List<Object>) Utils.getMethodArg(data.getListIndexInMethodArgs(), pjp.getArgs(), coord
                    .getMethod().toString());
            coord.setListKeyObjects(listKeyObjects);

            // Get the full list of cache keys and ask the cache for the corresponding values.
            coord.setInitialKey2Result(getCacheBase().getCache(data).getBulk(coord.getKey2Obj().keySet(), serializationType));

            // We've gotten all positive cache results back, so build up a results list and return it.
            if (coord.getMissedObjects().size() < 1) {
                return coord.generateResultList();
            }

            // Create the new list of arguments with a subset of the key objects that aren't in the cache.
            args = coord.modifyArgumentList(args);
        } catch (Throwable ex) {
            warn(ex, "Caching on %s aborted due to an error.", pjp.toShortString());
            return pjp.proceed();
        }

        /*
         * Call the target method with the new subset of arguments. We are calling this outside of the try/catch block
         * in case there are some 'not our fault' problems with the target method. (Connection issues, etc...) Though,
         * this decision could go either way, really.
         */
        final List<Object> results = (List<Object>) pjp.proceed(args);

        try {
            // there are no results
            if (results == null || results.isEmpty()) {
                if (coord.isAddNullsToCache()) {
                    addNullValues(coord.getMissedObjects(), coord, serializationType);
                }
                return coord.generatePartialResultList();
            }

            if (coord.isGenerateKeysFromResult()) {
                return generateByKeysFromResult(results, coord, serializationType);
            } else {
                return generateByKeysProviders(results, coord, serializationType);
            }
        } catch (Throwable ex) {
            warn(ex, "Caching on %s aborted due to an error. The underlying method will be called twice.", pjp.toShortString());
            return pjp.proceed();
        }
    }

    private void setMultiCacheOptions(final MultiCacheCoordinator coord, final ReadThroughMultiCacheOption options) {
        coord.setGenerateKeysFromResult(options.generateKeysFromResult());
        coord.setAddNullsToCache(options.addNullsToCache());
        coord.setSkipNullsInResult(options.skipNullsInResult());
    }

    private List<?> generateByKeysFromResult(List<Object> results, final MultiCacheCoordinator coord,
            final SerializationType serializationType) throws Exception {
        if (results == null) {
            results = new ArrayList<Object>();
        } else if (!results.isEmpty()) {
            final AnnotationData data = coord.getAnnotationData();
            String cacheKey;

            for (Object resultObject : results) {
                cacheKey = getCacheBase().getCacheKeyBuilder().getCacheKey(resultObject, data.getNamespace());
                getCacheBase().getCache(coord.getAnnotationData()).setSilently(cacheKey, data.getExpiration(), resultObject,
                        serializationType);
                coord.getMissedObjects().remove(coord.getKey2Obj().get(cacheKey));
            }
        }

        if (coord.isAddNullsToCache()) {
            addNullValues(coord.getMissedObjects(), coord, serializationType);
        }

        results.addAll(coord.generatePartialResultList());
        return results;
    }

    private List<?> generateByKeysProviders(final List<Object> results, final MultiCacheCoordinator coord,
            final SerializationType serializationType) {
        if (results.size() != coord.getMissedObjects().size()) {
            getLogger().info("Did not receive a correlated amount of data from the target method.");
            results.addAll(coord.generatePartialResultList());
            return results;
        }

        Iterator<Object> misssedObjectsIter = coord.getMissedObjects().iterator();
        for (Object resultObject : results) {
            resultObject = getCacheBase().getSubmission(resultObject);
            Object keyObject = misssedObjectsIter.next();
            String cacheKey = coord.getObj2Key().get(keyObject);
            getCacheBase().getCache(coord.getAnnotationData()).setSilently(cacheKey, coord.getAnnotationData().getExpiration(),
                    resultObject, serializationType);
            coord.getKey2Result().put(cacheKey, resultObject);

        }

        return coord.generateResultList();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
