/*
 * Copyright (c) 2010-2013 Jakub Białek
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
 */

package com.google.code.ssm.aop.counter;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.api.counter.ReadCounterFromCache;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Aspect
public class ReadCounterFromCacheAdvice extends CounterInCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(ReadCounterFromCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.counter.ReadCounterFromCache)")
    public void readSingleCounter() {
    }

    @Around("readSingleCounter()")
    public Object readCounter(final ProceedingJoinPoint pjp) throws Throwable {
        if (isDisabled()) {
            getLogger().info("Cache disabled");
            return pjp.proceed();
        }

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        // It will be invoked only if underlying method completes successfully.
        String cacheKey = null;
        ReadCounterFromCache annotation;
        AnnotationData data;
        try {
            Method methodToCache = getCacheBase().getMethodToCache(pjp);
            verifyMethodSignature(methodToCache);
            annotation = methodToCache.getAnnotation(ReadCounterFromCache.class);
            data = AnnotationDataBuilder.buildAnnotationData(annotation, ReadCounterFromCache.class, methodToCache);
            cacheKey = getCacheBase().getCacheKeyBuilder().getCacheKey(data, pjp.getArgs(), methodToCache.toString());
            Long result = getCacheBase().getCache(data).getCounter(cacheKey);

            if (result != null) {
                getLogger().debug("Cache hit.");
                return convertResult(methodToCache, result);
            }
        } catch (Exception ex) {
            warn(ex, "Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            if (checkData(result, pjp)) {
                long value = ((Number) result).longValue();
                // tricky way to update counter
                getCacheBase().getCache(data).incr(cacheKey, 0, value, annotation.expiration());
            }
        } catch (Exception ex) {
            warn(ex, "Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey);
        }
        return result;
    }

    protected void verifyMethodSignature(final Method methodToCache) {
        if (!isReturnTypeSupported(methodToCache.getReturnType())) {
            throw new RuntimeException(String.format("Wrong method return type %s", methodToCache.toString()));
        }
    }

    protected Number convertResult(final Method method, final long result) {
        if (method.getReturnType().equals(int.class) || method.getReturnType().equals(Integer.class)) {
            return (int) result;
        } else {
            return result;
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
