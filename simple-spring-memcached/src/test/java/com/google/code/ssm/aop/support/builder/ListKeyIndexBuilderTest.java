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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateMultiCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.UpdateMultiCache;

/**
 * 
 * @author Jakub Białek
 *
 */
public class ListKeyIndexBuilderTest {

    private final ListKeyIndexBuilder builder = new ListKeyIndexBuilder();
    private AnnotationData data;

    @Before
    public void init() {
        data = new AnnotationData();
    }

    @Test
    public void shouldNotPopulateIfExpectedAnnotationNotSupportListKey() throws Exception {
        final Class<? extends Annotation> expected = InvalidateAssignCache.class;
        final Method targetMethod = null;

        builder.populate(data, null, expected, targetMethod);

        assertEquals(Integer.MIN_VALUE, data.getListIndexInKeys());
        assertEquals(Integer.MIN_VALUE, data.getListIndexInMethodArgs());
    }

    @Test
    public void shouldPopulateListKeyIndex() throws Exception {
        final Class<? extends Annotation> expected = InvalidateMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey01").withArgs(List.class);
        data.setKeyIndexes(Collections.singleton(0));

        builder.populate(data, null, expected, targetMethod);

        assertEquals(0, data.getListIndexInKeys());
        assertEquals(0, data.getListIndexInMethodArgs());
    }

    @Test(expected = InvalidAnnotationException.class)
    public void shouldThrowExceptionIfNoListParamAnnotated() throws Exception {
        final Class<? extends Annotation> expected = InvalidateMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey02")
                .withArgs(String.class, String.class);
        data.setKeyIndexes(Arrays.asList(0, 1));

        builder.populate(data, null, expected, targetMethod);
    }

    @Test(expected = InvalidAnnotationException.class)
    public void shouldThrowExceptionIfMoreThanOneListParamAnnotated() throws Exception {
        final Class<? extends Annotation> expected = InvalidateMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey03")
                .withArgs(List.class, List.class);
        data.setKeyIndexes(Arrays.asList(0, 1));

        builder.populate(data, null, expected, targetMethod);
    }

    @Test
    public void shouldPopulateListKeyIndexIfArrayList() throws Exception {
        final Class<? extends Annotation> expected = UpdateMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey04")
                .withArgs(ArrayList.class);
        data.setKeyIndexes(Collections.singleton(0));

        builder.populate(data, null, expected, targetMethod);

        assertEquals(0, data.getListIndexInKeys());
        assertEquals(0, data.getListIndexInMethodArgs());
    }

    @Test
    public void shouldPopulateListKeyIndexIfLinkedList() throws Exception {
        final Class<? extends Annotation> expected = ReadThroughMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey05")
                .withArgs(LinkedList.class);
        data.setKeyIndexes(Collections.singleton(0));

        builder.populate(data, null, expected, targetMethod);

        assertEquals(0, data.getListIndexInKeys());
        assertEquals(0, data.getListIndexInMethodArgs());
    }

    @Test
    public void shouldPopulateCorrectListKeyIndex() throws Exception {
        final Class<? extends Annotation> expected = InvalidateMultiCache.class;
        final Method targetMethod = new Mirror().on(AnnotationDataDummy.class).reflect().method("populateListKey06")
                .withArgs(String.class, List.class);
        data.setKeyIndexes(Arrays.asList(1, 0));

        builder.populate(data, null, expected, targetMethod);

        assertEquals(0, data.getListIndexInKeys());
        assertEquals(1, data.getListIndexInMethodArgs());
    }

    private static class AnnotationDataDummy {

        @InvalidateMultiCache
        public void populateListKey01(@ParameterValueKeyProvider final List<String> keys) {

        }

        @InvalidateMultiCache
        public void populateListKey02(@ParameterValueKeyProvider(order = 1) final String key1,
                @ParameterValueKeyProvider(order = 2) final String key2) {

        }

        @InvalidateMultiCache
        public void populateListKey03(@ParameterValueKeyProvider(order = 1) final List<String> keys1,
                @ParameterValueKeyProvider(order = 2) final List<String> keys2) {

        }

        @UpdateMultiCache
        @ReturnDataUpdateContent
        public List<String> populateListKey04(@ParameterValueKeyProvider final ArrayList<String> keys) {
            return null;
        }

        @ReadThroughMultiCache
        public List<String> populateListKey05(@ParameterValueKeyProvider final LinkedList<String> keys) {
            return null;
        }

        @InvalidateMultiCache
        public void populateListKey06(@ParameterValueKeyProvider(order = 2) final String keys,
                @ParameterValueKeyProvider(order = 1) final List<String> keys2) {
        }

    }

}
