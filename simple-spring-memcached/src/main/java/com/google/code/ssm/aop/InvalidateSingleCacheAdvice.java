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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.api.InvalidateSingleCache;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Aspect
public class InvalidateSingleCacheAdvice extends CacheAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidateSingleCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.InvalidateSingleCache)")
    public void invalidateSingle() {
    }

    @Around("invalidateSingle()")
    public Object cacheInvalidateSingle(final ProceedingJoinPoint pjp) throws Throwable {
        if (isDisabled()) {
            getLogger().info("Cache disabled");
            return pjp.proceed();
        }

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        String cacheKey = null;
        final AnnotationData data;
        final Method methodToCache;
        try {
            methodToCache = getCacheBase().getMethodToCache(pjp);
            final InvalidateSingleCache annotation = methodToCache.getAnnotation(InvalidateSingleCache.class);
            data = AnnotationDataBuilder.buildAnnotationData(annotation, InvalidateSingleCache.class, methodToCache);
            if (!data.isReturnKeyIndex()) {
                cacheKey = getCacheBase().getCacheKeyBuilder().getCacheKey(data, pjp.getArgs(), methodToCache.toString());
            }
        } catch (Throwable ex) {
            warn(ex, "Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            if (data.isReturnKeyIndex()) {
                getCacheBase().verifyReturnTypeIsNoVoid(methodToCache, InvalidateSingleCache.class);
                cacheKey = getCacheBase().getCacheKeyBuilder().getCacheKey(result, data.getNamespace());
            }

            getCacheBase().getCache(data).delete(cacheKey);
        } catch (Throwable ex) {
            warn(ex, "Caching on method %s and key [%s] aborted due to an error.", pjp.toShortString(), cacheKey);
        }
        return result;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
