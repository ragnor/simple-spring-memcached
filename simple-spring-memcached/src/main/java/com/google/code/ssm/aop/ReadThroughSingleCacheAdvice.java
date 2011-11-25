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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.api.ReadThroughSingleCache;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
@Aspect
public class ReadThroughSingleCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughSingleCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.ReadThroughSingleCache)")
    public void getSingle() {
    }

    @Around("getSingle()")
    public Object cacheGetSingle(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        final ReadThroughSingleCache annotation;
        Class<?> jsonClass = null;
        String cacheKey = null;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            annotation = methodToCache.getAnnotation(ReadThroughSingleCache.class);
            verifyReturnTypeIsNoVoid(methodToCache, ReadThroughSingleCache.class);
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughSingleCache.class,
                    methodToCache);

            cacheKey = getCacheKey(annotationData, pjp, methodToCache);

            jsonClass = getReturnJsonClass(methodToCache);

            final Object result = get(cacheKey, jsonClass);
            if (result != null) {
                getLogger().debug("Cache hit.");
                return getResult(result);
            }
        } catch (Throwable ex) {
            warn(String.format("Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey), ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            final Object submission = getSubmission(result);
            set(cacheKey, annotation.expiration(), submission, jsonClass);
        } catch (Throwable ex) {
            warn(String.format("Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey), ex);
        }
        return result;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
