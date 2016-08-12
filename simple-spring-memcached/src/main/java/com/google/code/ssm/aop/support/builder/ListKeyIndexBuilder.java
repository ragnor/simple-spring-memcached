/*
 * Copyright (c) 2014-2016 Jakub Białek
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
import java.util.List;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.api.CacheOperation.Type;
import com.google.code.ssm.util.Utils;

/**
 * Should be called after {@link KeyIndexesBuilder}.
 * 
 * @author Jakub Białek
 * @since 3.6.0
 * 
 */
public class ListKeyIndexBuilder extends AbstractDataBuilder {

    @Override
    protected void build(final AnnotationData data, final Annotation annotation, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) throws Exception {
        if (data.getKeyIndexes().isEmpty()) {
            return;
        }

        final Class<?>[] keyMethodParamTypes = Utils.getMethodParameterTypes(data.getKeyIndexes(), targetMethod);
        final Integer[] keyIndexes = data.getKeyIndexes().toArray(new Integer[data.getKeyIndexes().size()]);
        final Integer listIndexInKeys = getListIndexInKeys(keyMethodParamTypes, targetMethod);
        if (listIndexInKeys == null) {
            throw new InvalidAnnotationException(String.format("No one parameter objects found at dataIndexes [%s] is not a [%s]. "
                    + "[%s] does not fulfill the requirements.", data.getKeyIndexes(), List.class.getName(), targetMethod.toString()));
        }

        data.setListIndexInKeys(listIndexInKeys);
        data.setListIndexInMethodArgs(keyIndexes[listIndexInKeys]);
    }

    @Override
    protected boolean support(Class<? extends Annotation> expectedAnnotationClass) {
        return isType(expectedAnnotationClass, Type.MULTI);
    }

    private Integer getListIndexInKeys(final Class<?>[] keyMethodParamTypes, final Method targetMethod) {
        Integer listIndexInKeys = null;
        for (int i = 0; i < keyMethodParamTypes.length; i++) {
            if (isList(keyMethodParamTypes[i])) {
                if (listIndexInKeys != null) {
                    throw new InvalidAnnotationException(
                            "There are more than one method's parameter annotated by @ParameterValueKeyProvider that is list "
                                    + targetMethod.toString());
                }
                listIndexInKeys = i;
            }
        }

        return listIndexInKeys;
    }

    private boolean isList(final Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

}
