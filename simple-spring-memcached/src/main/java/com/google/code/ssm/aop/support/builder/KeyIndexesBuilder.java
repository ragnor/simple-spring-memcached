/*
 * Copyright (c) 2014 Jakub Białek
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.CacheOperation.Type;

/**
 * 
 * @author Jakub Białek
 * @since 3.6.0
 * 
 */
public class KeyIndexesBuilder extends AbstractDataBuilder {

    private static final Comparator<ParameterValueKeyProvider> COMPARATOR = new ParameterValueKeyProviderComparator();

    @Override
    protected void build(final AnnotationData data, final Annotation annotation, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) {
        // Read*Cache annotations don't support ReturnValueKeyProvider, to cache method result cache keys must be
        // available before executing method.
        if (!isType(expectedAnnotationClass, Type.READ)) {
            final ReturnValueKeyProvider returnAnnotation = targetMethod.getAnnotation(ReturnValueKeyProvider.class);
            if (returnAnnotation != null) {
                data.setReturnKeyIndex(true);
                return;
            }
        }

        final Collection<Integer> keyIndexes = getKeyIndexes(targetMethod);
        if (keyIndexes.isEmpty()) {
            throw new InvalidParameterException(String.format("No KeyProvider annotation found method [%s]", targetMethod.getName()));
        }

        data.setKeyIndexes(keyIndexes);
    }

    @Override
    protected boolean support(Class<? extends Annotation> expectedAnnotationClass) {
        return !isType(expectedAnnotationClass, Type.ASSIGN);
    }

    private Collection<Integer> getKeyIndexes(final Method targetMethod) {
        final Annotation[][] paramAnnotationArrays = targetMethod.getParameterAnnotations();
        final SortedMap<ParameterValueKeyProvider, Integer> keyProviders = new TreeMap<ParameterValueKeyProvider, Integer>(COMPARATOR);
        final Set<Integer> order = new HashSet<Integer>();

        if (paramAnnotationArrays == null || paramAnnotationArrays.length < 1) {
            return Collections.emptyList();
        }

        for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
            final ParameterValueKeyProvider keyProviderAnnotation = getAnnotation(ParameterValueKeyProvider.class,
                    paramAnnotationArrays[ix]);
            if (keyProviderAnnotation == null) {
                continue;
            }

            // throw if order below 0
            if (keyProviderAnnotation.order() < 0) {
                throw new InvalidParameterException(String.format(
                        "No valid order [%d] defined in annotation [%s] on method [%s], only no negative integers are allowed.",
                        keyProviderAnnotation.order(), ParameterValueKeyProvider.class.getName(), targetMethod.getName()));
            }
            // throw exception if there are two annotations with the same value of order
            if (!order.add(keyProviderAnnotation.order())) {
                throwException("No valid order defined in annotation [%s] on method [%s]. "
                        + "There are two annotations with the same order.", ParameterValueKeyProvider.class, targetMethod);
            }

            keyProviders.put(keyProviderAnnotation, ix);
        }

        return keyProviders.values();
    }

    private static class ParameterValueKeyProviderComparator implements Comparator<ParameterValueKeyProvider>, Serializable {

        private static final long serialVersionUID = 2791887056140560908L;

        @Override
        public int compare(final ParameterValueKeyProvider o1, final ParameterValueKeyProvider o2) {
            return o1.order() - o2.order();
        }

    }

}
