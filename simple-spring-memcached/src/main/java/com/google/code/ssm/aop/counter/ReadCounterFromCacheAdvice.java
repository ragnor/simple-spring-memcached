package com.google.code.ssm.aop.counter;

import java.lang.reflect.Method;

import com.google.code.ssm.aop.AnnotationData;
import com.google.code.ssm.aop.AnnotationDataBuilder;
import com.google.code.ssm.api.counter.ReadCounterFromCache;
import com.google.code.ssm.providers.MemcacheTranscoder;
import com.google.code.ssm.transcoders.LongToStringTranscoder;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * @author Jakub Białek
 * 
 */
@Aspect
public class ReadCounterFromCacheAdvice extends CounterInCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(ReadCounterFromCacheAdvice.class);

    private final MemcacheTranscoder<Long> transcoder = new LongToStringTranscoder();

    @Pointcut("@annotation(com.google.code.ssm.api.counter.ReadCounterFromCache)")
    public void readSingleCounter() {
    }

    @Around("readSingleCounter()")
    public Object readCounter(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        // It will be invoked only if underlying method completes successfully. 
        String cacheKey;
        ReadCounterFromCache annotation;
        try {
            Method methodToCache = getMethodToCache(pjp);
            verifyMethodSignature(methodToCache);
            annotation = methodToCache.getAnnotation(ReadCounterFromCache.class);
            AnnotationData annotationData = AnnotationDataBuilder
                    .buildAnnotationData(annotation, ReadCounterFromCache.class, methodToCache);
            String[] objectsIds = getObjectIds(annotationData.getKeysIndex(), pjp, methodToCache);
            cacheKey = buildCacheKey(objectsIds, annotationData);
            Long result = get(cacheKey, transcoder);

            if (result != null) {
                getLogger().debug("Cache hit.");
                return convertResult(methodToCache, result);
            }
        } catch (Throwable ex) {
            getLogger().warn(String.format("Caching on %s aborted due to an error.", pjp.toShortString()), ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            if (checkData(result, pjp)) {
                long value = ((Number) result).longValue();
                // tricky way to read counter
                incr(cacheKey, 0, value, annotation.expiration());
            }
        } catch (Throwable ex) {
            getLogger().warn(String.format("Caching on %s aborted due to an error.", pjp.toShortString()), ex);
        }
        return result;
    }

    protected void verifyMethodSignature(Method methodToCache) {
        if (!isReturnTypeSupported(methodToCache.getReturnType())) {
            throw new RuntimeException(String.format("Wrong method return type %s", methodToCache.toString()));
        }
    }

    protected Number convertResult(Method method, long result) {
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
