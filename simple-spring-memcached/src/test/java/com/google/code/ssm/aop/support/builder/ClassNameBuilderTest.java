/*
 * Copyright (c) 2014-2017 Jakub Białek
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
public class ClassNameBuilderTest {

    private final Class<? extends Annotation> expected = InvalidateAssignCache.class;
    private final String method = "populateClassName01";
    private final ClassNameBuilder builder = new ClassNameBuilder();

    private AnnotationData data;
    private Method targetMethod;
    private Annotation annotation;

    @Before
    public void init() {
        data = new AnnotationData();
        targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method(method).withArgs(String.class);
        annotation = new Mirror().on(AnnotationDataDummy.class).reflect().annotation(expected).atMethod(method).withArgs(String.class);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfNullAnnotation() throws Exception {
        builder.populate(data, null, expected, targetMethod);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfAnnotationNotMatchExpectedClass() throws Exception {
        builder.populate(data, annotation, UpdateAssignCache.class, targetMethod);
    }

    @Test
    public void populateShouldBuildClassName() throws Exception {
        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(expected.getName(), data.getClassName());
    }

    private static class AnnotationDataDummy {

        @InvalidateAssignCache
        public void populateClassName01(final String key1) {
        }

    }

}
