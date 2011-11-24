/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

import com.google.code.ssm.api.ReadThroughAssignCache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
@Aspect
public class ReadThroughAssignCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughAssignCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.ReadThroughAssignCache)")
    public void getSingleAssign() {
    }

    @Around("getSingleAssign()")
    public Object cacheSingleAssign(final ProceedingJoinPoint pjp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        final String cacheKey;
        final AnnotationData annotationData;
        Class<?> jsonClass = null;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            verifyReturnTypeIsNoVoid(methodToCache, ReadThroughAssignCache.class);
            final ReadThroughAssignCache annotation = methodToCache.getAnnotation(ReadThroughAssignCache.class);
            jsonClass = getReturnJsonClass(methodToCache);
            annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughAssignCache.class, methodToCache);
            cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
            final Object result = get(cacheKey, jsonClass);
            if (result != null) {
                getLogger().debug("Cache hit.");
                return (result instanceof PertinentNegativeNull) ? null : result;
            }
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        try {
            final Object submission = getSubmission(result);
            set(cacheKey, annotationData.getExpiration(), submission, jsonClass);
        } catch (Throwable ex) {
            warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
