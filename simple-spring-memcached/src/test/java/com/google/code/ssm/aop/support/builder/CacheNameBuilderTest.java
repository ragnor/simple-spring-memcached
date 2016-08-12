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

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.AnnotationConstants;
import com.google.code.ssm.api.CacheName;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;

/**
 * 
 * @author Jakub Białek
 *
 */
public class CacheNameBuilderTest {

    private final CacheNameBuilder builder = new CacheNameBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldPopulateDefaultIfNoCacheAnnotation() throws Exception {
        final Class<? extends Annotation> expected = null;
        final Annotation annotation = null;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy2.class).reflect().method("populateCacheName03")
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(AnnotationConstants.DEFAULT_CACHE_NAME, data.getCacheName());
    }

    @Test
    public void shouldPopulateFromMethod() throws Exception {
        final Class<? extends Annotation> expected = null;
        final Annotation annotation = null;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateCacheName01")
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(AnnotationDataDummy.METHOD_CACHE, data.getCacheName());
    }

    @Test
    public void shouldPopulateFromClass() throws Exception {
        final Class<? extends Annotation> expected = null;
        final Annotation annotation = null;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateCacheName02")
                .withArgs(String.class);

        builder.populate(data, annotation, expected, targetMethod);

        assertEquals(AnnotationDataDummy.CLASS_CACHE, data.getCacheName());
    }

    @CacheName(AnnotationDataDummy.CLASS_CACHE)
    private static class AnnotationDataDummy {

        public static final String CLASS_CACHE = "classCache";
        public static final String METHOD_CACHE = "methodCache";

        @CacheName(METHOD_CACHE)
        @InvalidateSingleCache
        public void populateCacheName01(@ParameterValueKeyProvider final String key1) {
        }

        @InvalidateSingleCache
        public void populateCacheName02(@ParameterValueKeyProvider final String key1) {
        }

    }

    private static class AnnotationDataDummy2 {

        @InvalidateSingleCache
        public void populateCacheName03(@ParameterValueKeyProvider final String key1) {
        }

    }

}
