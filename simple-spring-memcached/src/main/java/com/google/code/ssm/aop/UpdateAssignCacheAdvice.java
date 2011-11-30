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

import com.google.code.ssm.api.UpdateAssignCache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Aspect
public class UpdateAssignCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssignCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.UpdateAssignCache)")
    public void updateAssign() {
    }

    @AfterReturning(pointcut = "updateAssign()", returning = "retVal")
    public void cacheUpdateAssign(final JoinPoint jp, final Object retVal) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        String cacheKey = null;
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateAssignCache annotation = methodToCache.getAnnotation(UpdateAssignCache.class);
            final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateAssignCache.class, methodToCache);

            cacheKey = cacheKeyBuilder.getAssignCacheKey(data);

            final Object dataObject = this.<Object> getUpdateData(data, methodToCache, jp, retVal);
            final Class<?> jsonClass = getDataJsonClass(methodToCache, data);
            final Object submission = getSubmission(dataObject);
            set(cacheKey, data.getExpiration(), submission, jsonClass);
        } catch (Exception ex) {
            warn(String.format("Caching on method %s and key [%s] aborted due to an error.", jp.toShortString(), cacheKey), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
