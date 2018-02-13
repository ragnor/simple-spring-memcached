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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateSingleCache;

/**
 * 
 * @author Jakub Białek
 *
 */
public class AssignedKeyBuilderTest {

    private final AssignedKeyBuilder builder = new AssignedKeyBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldNotPopulateIfNotAssignAnnotation() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final String method = "populateAssign01";
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals("", data.getAssignedKey());
    }

    @Test(expected = InvalidParameterException.class)
    public void populateShouldThrowExceptionIfNoAssignedKey() throws Exception {
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final String method = "populateAssign02";
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);
    }

    @Test(expected = InvalidParameterException.class)
    public void populateShouldThrowExceptionIfEmptyAssignedKey() throws Exception {
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final String method = "populateAssign03";
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);
    }

    @Test
    public void populateShouldBuildAssignedKey() throws Exception {
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final String method = "populateAssign04";
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        final Annotation annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method)
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(AnnotationDataDummy.SAMPLE_KEY, data.getAssignedKey());
    }
    
    private static class AnnotationDataDummy {

        public static final String SAMPLE_KEY = "bigSampleKey";

        @InvalidateSingleCache
        public void populateAssign01(final String key1) {
        }

        @InvalidateAssignCache
        public void populateAssign02(final String key1) {
        }

        @InvalidateAssignCache(assignedKey = "")
        public void populateAssign03(final String key1) {
        }

        @InvalidateAssignCache(assignedKey = SAMPLE_KEY)
        public void populateAssign04(final String key1) {
        }

    }

}
