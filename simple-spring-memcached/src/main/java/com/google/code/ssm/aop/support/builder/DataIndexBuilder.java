/*
 * Copyright (c) 2014-2015 Jakub Białek
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
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.CacheOperation.Type;

/**
 * 
 * @author Jakub Białek
 * @since 3.6.0
 * 
 */
public class DataIndexBuilder extends AbstractDataBuilder {

    @Override
    protected void build(final AnnotationData data, final Annotation annotation, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) {
        if (isReturnDataUpdateContent(targetMethod)) {
            data.setReturnDataIndex(true);
            return;
        }

        final Integer foundIndex = getIndexOfAnnotatedParam(targetMethod, ParameterDataUpdateContent.class);
        if (foundIndex == null) {
            throw new InvalidParameterException(String.format(
                    "No ReturnDataUpdateContent or ParameterDataUpdateContent annotation found on method [%s]", targetMethod.getName()));
        }

        data.setDataIndex(foundIndex);
    }

    @Override
    protected boolean support(final Class<? extends Annotation> expectedAnnotationClass) {
        return isType(expectedAnnotationClass, Type.UPDATE);
    }

    private boolean isReturnDataUpdateContent(final Method method) {
        final ReturnDataUpdateContent returnAnnotation = method.getAnnotation(ReturnDataUpdateContent.class);
        if (returnAnnotation != null) {
            if (method.getReturnType().equals(void.class)) {
                throwException("Annotation [%s] is defined on void method [%s]", ReturnDataUpdateContent.class, method);
            }
            return true;
        }

        return false;
    }

    private Integer getIndexOfAnnotatedParam(final Method method, final Class<? extends Annotation> annotationClass) {
        final Annotation[][] paramAnnotationArrays = method.getParameterAnnotations();
        Integer foundIndex = null;

        if (paramAnnotationArrays != null && paramAnnotationArrays.length > 0) {
            for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
                if (getAnnotation(annotationClass, paramAnnotationArrays[ix]) != null) {
                    if (foundIndex != null) {
                        throwException("Multiple annotations of type [%s] found on method [%s]", annotationClass, method);
                    }
                    foundIndex = ix;
                }
            }
        }

        return foundIndex;
    }
}
