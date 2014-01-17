/*
 * Copyright (c) 2012-2014 Nelson Carpentier, Jakub Białek
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
import com.google.code.ssm.api.format.SerializationType;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * @since 2.0.0
 * 
 * @param <T>
 *            the type of SSM update cache annotation
 */
abstract class SingleUpdateCacheAdvice<T extends Annotation> extends CacheAdvice {

    private final Class<T> annotationClass;

    protected SingleUpdateCacheAdvice(final Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    protected void update(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isDisabled()) {
            getLogger().info("Cache disabled");
            return;
        }

        // For Update*Cache, an AfterReturning aspect is fine. We will only
        // apply our caching after the underlying method completes successfully, and we will have
        // the same access to the method params.
        String cacheKey = null;
        try {
            final Method methodToCache = getCacheBase().getMethodToCache(jp);
            final T annotation = methodToCache.getAnnotation(annotationClass);
            final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(annotation, annotationClass, methodToCache);

            if (data.isReturnKeyIndex()) {
                cacheKey = getCacheBase().getCacheKeyBuilder().getCacheKey(retVal, data.getNamespace());
            } else {
                cacheKey = getCacheKey(data, jp.getArgs(), methodToCache.toString());
            }

            final Object dataObject = getCacheBase().<Object> getUpdateData(data, methodToCache, jp.getArgs(), retVal);
            final SerializationType serializationType = getCacheBase().getSerializationType(methodToCache);
            final Object submission = getCacheBase().getSubmission(dataObject);
            getCacheBase().getCache(data).set(cacheKey, data.getExpiration(), submission, serializationType);
        } catch (Exception ex) {
            warn(ex, "Caching on method %s and key [%s] aborted due to an error.", jp.toShortString(), cacheKey);
        }
    }

    protected abstract String getCacheKey(final AnnotationData data, final Object[] args, final String methodDesc) throws Exception;

}
