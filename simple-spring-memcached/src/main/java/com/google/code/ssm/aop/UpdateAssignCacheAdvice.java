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
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateAssignCache annotation = methodToCache.getAnnotation(UpdateAssignCache.class);
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, UpdateAssignCache.class,
                    methodToCache);

            final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);

            final Object dataObject = this.<Object> getUpdateData(annotationData, methodToCache, jp, retVal);
            final Class<?> jsonClass = getJsonClass(methodToCache, annotationData.getDataIndex());
            final Object submission = getSubmission(dataObject);
            set(cacheKey, annotationData.getExpiration(), submission, jsonClass);
        } catch (Exception ex) {
            warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
