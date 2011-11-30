/*
 * Copyright (c) 2010-2011 Jakub Białek
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

import com.google.code.ssm.aop.AnnotationData;
import com.google.code.ssm.aop.AnnotationDataBuilder;
import com.google.code.ssm.api.counter.IncrementCounterInCache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Aspect
public class IncrementCounterInCacheAdvice extends CounterInCacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementCounterInCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.counter.IncrementCounterInCache)")
    public void incrementSingleCounter() {
    }

    @AfterReturning("incrementSingleCounter()")
    public void incrementSingle(final JoinPoint jp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        // It will be invoked only if underlying method completes successfully.
        String cacheKey = null;
        IncrementCounterInCache annotation;
        try {
            Method methodToCache = getMethodToCache(jp);
            annotation = methodToCache.getAnnotation(IncrementCounterInCache.class);
            AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, IncrementCounterInCache.class, methodToCache);
            cacheKey = cacheKeyBuilder.getCacheKey(data, jp.getArgs(), methodToCache.toString());
            incr(cacheKey, 1, 1);
        } catch (Throwable ex) {
            getLogger().warn(String.format("Incrementing counter [%s] via %s aborted due to an error.", cacheKey, jp.toShortString()), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
