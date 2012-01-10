/*
 * Copyright (c) 2010-2012 Jakub Białek
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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.api.counter.UpdateCounterInCache;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Aspect
public class UpdateCounterInCacheAdvice extends CounterInCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateCounterInCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.counter.UpdateCounterInCache)")
    public void updateCounter() {
    }

    @AfterReturning(pointcut = "updateCounter()", returning = "retVal")
    public void cacheCounterInCache(final JoinPoint jp, final Object retVal) throws Throwable {
        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        String cacheKey = null;
        UpdateCounterInCache annotation;
        try {
            Method methodToCache = getMethodToCache(jp);
            annotation = methodToCache.getAnnotation(UpdateCounterInCache.class);
            AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateCounterInCache.class, methodToCache);
            cacheKey = cacheKeyBuilder.getCacheKey(data, jp.getArgs(), methodToCache.toString());

            Object dataObject = getUpdateData(data, methodToCache, jp, retVal);
            if (checkData(dataObject, jp)) {
                long value = ((Number) dataObject).longValue();
                getCache(data).set(cacheKey, annotation.expiration(), value, Long.class);
            }
        } catch (Exception ex) {
            getLogger().warn(String.format("Updating counter [%s] in cache via %s aborted due to an error.", cacheKey, jp.toShortString()),
                    ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
