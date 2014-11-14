/*
 * Copyright (c) 2008-2014 Nelson Carpentier, Jakub Białek
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

package com.google.code.ssm.aop.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.code.ssm.aop.support.builder.AbstractDataBuilder;
import com.google.code.ssm.aop.support.builder.AssignedKeyBuilder;
import com.google.code.ssm.aop.support.builder.CacheNameBuilder;
import com.google.code.ssm.aop.support.builder.ClassNameBuilder;
import com.google.code.ssm.aop.support.builder.DataIndexBuilder;
import com.google.code.ssm.aop.support.builder.ExpirationBuilder;
import com.google.code.ssm.aop.support.builder.KeyIndexesBuilder;
import com.google.code.ssm.aop.support.builder.ListKeyIndexBuilder;
import com.google.code.ssm.aop.support.builder.NamespaceBuilder;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
public class AnnotationDataBuilder {

    // order is important because some builders require other to prepare annotation data
    private static final AbstractDataBuilder[] BUILDERS = { new ClassNameBuilder(), new CacheNameBuilder(), new KeyIndexesBuilder(),
            new DataIndexBuilder(), new ExpirationBuilder(), new NamespaceBuilder(), new AssignedKeyBuilder(), new ListKeyIndexBuilder() };

    private AnnotationDataBuilder() {

    }

    public static AnnotationData buildAnnotationData(final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final Method targetMethod) {
        final AnnotationData data = new AnnotationData();
        try {
            for (final AbstractDataBuilder builder : BUILDERS) {
                builder.populate(data, annotation, expectedAnnotationClass, targetMethod);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }

}
