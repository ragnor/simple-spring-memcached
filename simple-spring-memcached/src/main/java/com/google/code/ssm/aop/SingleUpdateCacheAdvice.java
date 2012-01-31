/*
 * Copyright (c) 2012 Nelson Carpentier, Jakub Białek
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.AnnotationDataBuilder;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
abstract class SingleUpdateCacheAdvice<T extends Annotation> extends CacheAdvice {

    private final Class<T> annotationClass;

    public SingleUpdateCacheAdvice(final Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    protected void update(final JoinPoint jp, final Object retVal) throws Throwable {
        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        String cacheKey = null;
        try {
            final Method methodToCache = getCacheBase().getMethodToCache(jp);
            final T annotation = methodToCache.getAnnotation(annotationClass);
            final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, annotationClass, methodToCache);

            cacheKey = getCacheKey(data, jp.getArgs(), methodToCache.toString());

            final Object dataObject = getCacheBase().<Object> getUpdateData(data, methodToCache, jp, retVal);
            final Class<?> jsonClass = getCacheBase().getDataJsonClass(methodToCache, data);
            final Object submission = getCacheBase().getSubmission(dataObject);
            getCacheBase().getCache(data).set(cacheKey, data.getExpiration(), submission, jsonClass);
        } catch (Exception ex) {
            getLogger().warn(String.format("Caching on method %s and key [%s] aborted due to an error.", jp.toShortString(), cacheKey), ex);
        }
    }

    protected abstract String getCacheKey(final AnnotationData data, final Object[] args, final String methodDesc) throws Exception;

}
