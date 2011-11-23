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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOptions;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
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
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        final MultiCacheCoordinator coord = new MultiCacheCoordinator();
        Class<?> jsonClass = null;

        Object[] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            coord.setMethod(getMethodToCache(pjp));
            verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            final ReadThroughMultiCache annotation = coord.getMethod().getAnnotation(ReadThroughMultiCache.class);
            jsonClass = getJsonClass(coord.getMethod(), AnnotationData.RETURN_INDEX);

            coord.setAnnotationData(AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughMultiCache.class, coord.getMethod()));
            ReadThroughMultiCacheOptions options = annotation.options();

            coord.setGenerateKeysFromResult(options.generateKeysFromResult());
            coord.setAddNullsToCache(options.addNullsToCache());
            coord.setSkipNullsInResult(options.skipNullsInResult());

            // Get the list of objects that will provide the keys to all the cache values.
            coord.setKeyObjects(getKeyObjects(coord.getAnnotationData().getKeysIndex(), pjp, coord.getMethod(), coord, annotation));

            // Create key->object and object->key mappings.
            coord.setHolder(convertIdObjectsToKeyMap(coord.getListObjects(), coord.getKeyObjects(), coord.getListIndexInKeys(),
                    coord.getAnnotationData()));

            if (options.readDirectlyFromDB()) {
                coord.setInitialKey2Result(Collections.EMPTY_MAP);
            } else {
                // Get the full list of cache keys and ask the cache for the corresponding values.
                coord.setInitialKey2Result(getBulk(coord.getKey2Obj().keySet(), jsonClass));
            }

            // We've gotten all positive cache results back, so build up a results list and return it.
            if (coord.getMissObjects().size() < 1) {
                return coord.generateResultList();
            }

            // Create the new list of arguments with a subset of the key objects that aren't in the cache.
            args = coord.modifyArgumentList(args);
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
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
                    addNullsValuesForMissedObjects(coord.getMissObjects(), coord, jsonClass);
                }
                return coord.generatePartialResultList();
            }

            if (coord.isGenerateKeysFromResult()) {
                return generateByKeysFromResult(results, coord, jsonClass);
            } else {
                return generateByKeysProviders(results, coord, jsonClass);
            }
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error. The underlying method will be called twice.", ex);
            return pjp.proceed();
        }
    }

    private List<?> generateByKeysFromResult(List<Object> results, MultiCacheCoordinator coord, Class<?> jsonClass) {
        if (results == null) {
            results = new ArrayList<Object>();
        } else if (!results.isEmpty()) {
            List<String> keys = defaultKeyProvider.generateKeys(results);
            String cacheKey;

            int ix = 0;
            for (Object resultObject : results) {
                cacheKey = buildCacheKey(keys.get(ix), coord.getAnnotationData());
                setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject, jsonClass);
                coord.getMissObjects().remove(coord.getKey2Obj().get(cacheKey));
                ix++;
            }
        }

        if (coord.isAddNullsToCache()) {
            addNullsValuesForMissedObjects(coord.getMissObjects(), coord, jsonClass);
        }

        results.addAll(coord.generatePartialResultList());
        return results;
    }

    private List<?> generateByKeysProviders(List<Object> results, MultiCacheCoordinator coord, Class<?> jsonClass) {
        if (results.size() != coord.getMissObjects().size()) {
            getLogger().info("Did not receive a correlated amount of data from the target method.");
            results.addAll(coord.generatePartialResultList());
            return results;
        }

        int ix = 0;
        for (Object resultObject : results) {
            resultObject = getSubmission(resultObject);
            Object keyObject = coord.getMissObjects().get(ix);
            String cacheKey = coord.getObj2Key().get(keyObject);
            setSilently(cacheKey, coord.getAnnotationData().getExpiration(), resultObject, jsonClass);
            coord.getKey2Result().put(cacheKey, resultObject);
            ix++;
        }

        return coord.generateResultList();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private void addNullsValuesForMissedObjects(List<Object> missObjects, MultiCacheCoordinator coord, Class<?> jsonClass) {
        for (Object keyObject : missObjects) {
            addSilently(coord.getObj2Key().get(keyObject), coord.getAnnotationData().getExpiration(), PertinentNegativeNull.NULL, jsonClass);
        }
    }

}
