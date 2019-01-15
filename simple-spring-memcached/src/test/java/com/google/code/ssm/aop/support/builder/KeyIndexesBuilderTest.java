/*
 * Copyright (c) 2014-2019 Jakub Białek
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnValueKeyProvider;

/**
 * 
 * @author Jakub Białek
 *
 */
public class KeyIndexesBuilderTest {

    private final KeyIndexesBuilder builder = new KeyIndexesBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldNotPopulateIfExpectedAnnotationNotRequireKeyProvider() throws Exception {
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final Method targetMethod = null;

        builder.populate(data, null, expected, targetMethod);

        assertTrue(data.getKeyIndexes().isEmpty());
        assertFalse(data.isReturnKeyIndex());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfNoKeyProviderAnnotation() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider01")
                .withArgs(String.class);

        builder.populate(data, null, expected, targetMethod);
    }

    @Test
    public void shouldPopulateReturnKeyAnnotation() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider02")
                .withArgs(String.class);

        builder.populate(data, null, expected, targetMethod);

        assertTrue(data.getKeyIndexes().isEmpty());
        assertTrue(data.isReturnKeyIndex());
    }

    @Test
    public void shouldPopulateParamKeyAnnotation() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider02")
                .withArgs(String.class);

        builder.populate(data, null, expected, targetMethod);

        assertTrue(data.getKeyIndexes().isEmpty());
        assertTrue(data.isReturnKeyIndex());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionIfMultipleParamKeyAnnotationWithTheSameOrder() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider04")
                .withArgs(String.class, String.class);

        builder.populate(data, null, expected, targetMethod);
    }

    @Test
    public void shouldPopulateSingleParamKeyAnnotation() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider05")
                .withArgs(String.class, String.class, String.class);

        builder.populate(data, null, expected, targetMethod);

        assertCollectionEquals(Collections.singleton(2), data.getKeyIndexes());
        assertFalse(data.isReturnKeyIndex());
    }

    @Test
    public void shouldPopulateWithCorrectOrder() throws Exception {
        final Class<? extends Annotation> expected = InvalidateSingleCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateKeyProvider09")
                .withArgs(String.class, String.class);

        builder.populate(data, null, expected, targetMethod);

        assertEquals(2, data.getKeyIndexes().size());
        final Iterator<Integer> iter = data.getKeyIndexes().iterator();
        assertEquals(1, iter.next().intValue());
        assertEquals(0, iter.next().intValue());
        assertFalse(data.isReturnKeyIndex());
    }

    private static void assertCollectionEquals(final Collection<?> expected, final Collection<?> target) {
        assertTrue(String.format("Expected %s, currect %s", expected, target), expected.containsAll(target));
        assertTrue(String.format("Expected %s, currect %s", expected, target), target.containsAll(expected));
    }

    private static class AnnotationDataDummy {

        @InvalidateSingleCache
        public void populateKeyProvider01(final String key1) {
        }

        @InvalidateSingleCache
        @ReturnValueKeyProvider
        public void populateKeyProvider02(final String key1) {
        }

        @InvalidateSingleCache
        public void populateKeyProvider03(@ParameterValueKeyProvider final String key1) {
        }

        @InvalidateSingleCache
        public void populateKeyProvider04(@ParameterValueKeyProvider final String key1, @ParameterValueKeyProvider final String key2) {
        }

        @InvalidateSingleCache
        public void populateKeyProvider05(final String key1, final String key2, @ParameterValueKeyProvider final String key3) {
        }

        @InvalidateSingleCache
        @ReturnValueKeyProvider
        public void populateKeyProvider06(final String key1) {
        }

        @InvalidateSingleCache
        public void populateKeyProvider07(final String key1, @ParameterValueKeyProvider final String key2, final String key3) {
        }

        @InvalidateSingleCache
        @ReturnValueKeyProvider
        public void populateKeyProvider08(final String key1) {
        }

        @InvalidateSingleCache
        public void populateKeyProvider09(@ParameterValueKeyProvider(order = 2) final String key1,
                @ParameterValueKeyProvider(order = 1) final String key2) {
        }

    }

}
