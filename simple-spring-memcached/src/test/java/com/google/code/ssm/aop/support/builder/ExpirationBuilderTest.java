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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.UpdateAssignCache;

/**
 * 
 * @author Jakub Białek
 *
 */
public class ExpirationBuilderTest {

    private final ExpirationBuilder builder = new ExpirationBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldNotPopulateIfAnnotationNotSupportExpiration() throws Exception {
        final String method = "populateExpiration01";
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(0, data.getExpiration());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfNegativeValue() throws Exception {
        final String method = "populateExpiration02";
        final Class<? extends Annotation> expected = UpdateAssignCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);
    }

    @Test
    public void shouldPopulateExpiration() throws Exception {
        final String method = "populateExpiration03";
        final Class<? extends Annotation> expected = UpdateAssignCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(AnnotationDataDummy.SAMPLE_EXP, data.getExpiration());
    }

    private static class AnnotationDataDummy {

        public static final int SAMPLE_EXP = 42;

        @InvalidateAssignCache
        public void populateExpiration01(final String key1) {
        }

        @UpdateAssignCache(expiration = -1)
        public void populateExpiration02(final String key1) {
        }

        @UpdateAssignCache(expiration = SAMPLE_EXP)
        public void populateExpiration03(final String key1) {
        }

    }

}
