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
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.UpdateAssignCache;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class DataIndexBuilderTest {

    private final Class<? extends Annotation> expected = UpdateAssignCache.class;
    private final DataIndexBuilder builder = new DataIndexBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldNotPopulateIfNotUpdateAnnotation() throws Exception {
        builder.populate(data, null, InvalidateAssignCache.class, null);
        assertEquals(Integer.MIN_VALUE, data.getDataIndex());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfNotUpdateContentAnnotation() throws Exception {
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData01").withArgs(String.class);

        builder.populate(data, null, expected, targetMethod);
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfMultipleUpdateContentAnnotations() throws Exception {
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData02")
                .withArgs(String.class, String.class);

        builder.populate(data, null, expected, targetMethod);
    }

    @Test
    public void shouldPopulateReturnData() throws Exception {
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData03").withArgs(String.class);

        builder.populate(data, null, expected, targetMethod);

        assertEquals(-1, data.getDataIndex());
    }

    @Test
    public void shouldPopulateParameterContent() throws Exception {
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateData04")
                .withArgs(String.class, String.class);

        builder.populate(data, null, expected, targetMethod);

        assertEquals(1, data.getDataIndex());
    }

    private static class AnnotationDataDummy {

        @UpdateAssignCache
        public void populateData01(final String key1) {
        }

        @UpdateAssignCache
        public void populateData02(@ParameterDataUpdateContent final String key1, @ParameterDataUpdateContent final String key2) {
        }

        @UpdateAssignCache
        @ReturnDataUpdateContent
        public String populateData03(final String key1) {
            return null;
        }

        @UpdateAssignCache
        public void populateData04(final String key1, @ParameterDataUpdateContent final String key2) {
        }
    }

}
