/*
 * Copyright (c) 2014-2018 Jakub Białek
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

package com.google.code.ssm.aop.support.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.CacheOperation;
import com.google.code.ssm.api.CacheOperation.Type;

/**
 * 
 * @author Jakub Białek
 * @since 3.6.0
 * 
 */
public abstract class AbstractDataBuilder {

    /**
     * Populates additional data into annotation data.
     * 
     * @param data
     *            the annotation data to fill in
     * @param annotation
     *            the cache annotation
     * @param expectedAnnotationClass
     *            the expected class of cache annotation
     * @param targetMethod
     *            the intercepted (cached) method
     * @throws Exception
     */
    public void populate(final AnnotationData data, final Annotation annotation, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) throws Exception {
        if (support(expectedAnnotationClass)) {
            build(data, annotation, expectedAnnotationClass, targetMethod);
        }
    }

    protected abstract void build(final AnnotationData data, final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final Method targetMethod) throws Exception;

    protected boolean support(final Class<? extends Annotation> expectedAnnotationClass) {
        return true;
    }

    protected boolean isType(final Class<?> clazz, final Type type) {
        final CacheOperation operation = clazz.getAnnotation(CacheOperation.class);
        if (operation == null) {
            return false;
        }

        for (final Type operationType : operation.value()) {
            if (type.equals(operationType)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Annotation> T getAnnotation(final Class<T> annotationClass, final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (final Annotation annotation : annotations) {
                if (annotationClass.equals(annotation.annotationType())) {
                    return (T) annotation;
                }
            }
        }

        return null;
    }

    protected void throwException(final String msg, final Class<? extends Annotation> clazz, final Method method) {
        throw new InvalidParameterException(String.format(msg, clazz.getName(), method.getName()));
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeMethod(final Annotation annotation, final Class<? extends Annotation> annotationClass, final String methodName)
            throws Exception {
        final Method method = annotationClass.getDeclaredMethod(methodName, (Class<?>[]) null);
        return (T) method.invoke(annotation, (Object[]) null);
    }

}
