/*
 * Copyright (c) 2012-2014 Jakub Białek
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
 */

package com.google.code.ssm.aop.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.code.ssm.api.BridgeMethodMapping;
import com.google.code.ssm.api.BridgeMethodMappings;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class BridgeMethodMappingStoreImplTest {

    private final BridgeMethodMappingStoreImpl store = new BridgeMethodMappingStoreImpl();

    @Test
    public void testValid() {
        Class<?>[] targetParamTypes = store.getTargetParamsTypes(SubGeneric1.class, "set", new Class<?>[] { Object.class, Object.class });
        assertNotNull(targetParamTypes);
        assertEquals(Number.class, targetParamTypes[0]);
        assertEquals(String.class, targetParamTypes[1]);

        // try again
        targetParamTypes = store.getTargetParamsTypes(SubGeneric1.class, "set", new Class<?>[] { Object.class, Object.class });
        assertNotNull(targetParamTypes);
        assertEquals(Number.class, targetParamTypes[0]);
        assertEquals(String.class, targetParamTypes[1]);

        targetParamTypes = store.getTargetParamsTypes(SubGeneric1.class, "get", new Class<?>[] { Object.class });
        assertNotNull(targetParamTypes);
        assertEquals(Number.class, targetParamTypes[0]);

        targetParamTypes = store.getTargetParamsTypes(SubGeneric1.class, "put",
                new Class<?>[] { Object.class, Object.class, boolean.class });
        assertNotNull(targetParamTypes);
        assertEquals(Number.class, targetParamTypes[0]);
        assertEquals(String.class, targetParamTypes[1]);
        assertEquals(boolean.class, targetParamTypes[2]);
    }

    @Test(expected = RuntimeException.class)
    public void testNoMappingsAnnotation() {
        store.getTargetParamsTypes(SubGeneric2.class, "set", new Class<?>[] { Object.class, Object.class });
    }

    @Test(expected = InvalidAnnotationException.class)
    public void testWrongMappings() {
        store.getTargetParamsTypes(SubGeneric3.class, "set", new Class<?>[] { Object.class, Object.class });
    }

    static interface Generic<K, V> {
        void set(final K key, final V value);

        V get(final K key);

        void put(final K key, final V value, final boolean overwrite);
    }

    @BridgeMethodMappings({
            @BridgeMethodMapping(methodName = "set", erasedParamTypes = { Object.class, Object.class }, targetParamTypes = { Number.class,
                    String.class }),
            @BridgeMethodMapping(methodName = "get", erasedParamTypes = { Object.class }, targetParamTypes = { Number.class }),
            @BridgeMethodMapping(methodName = "put", erasedParamTypes = { Object.class, Object.class, boolean.class }, targetParamTypes = {
                    Number.class, String.class, boolean.class }) })
    static class SubGeneric1 implements Generic<Number, String> {
        @Override
        public void set(final Number key, final String value) {

        }

        @Override
        public String get(final Number key) {
            return null;
        }

        @Override
        public void put(final Number key, final String value, final boolean overwrite) {

        }
    }

    static class SubGeneric2 implements Generic<Number, String> {
        @Override
        public void set(final Number key, final String value) {

        }

        @Override
        public String get(final Number key) {
            return null;
        }

        @Override
        public void put(final Number key, final String value, final boolean overwrite) {

        }
    }

    @BridgeMethodMappings({ @BridgeMethodMapping(methodName = "set", erasedParamTypes = { Object.class, Object.class }, targetParamTypes = { Number.class }) })
    static class SubGeneric3 implements Generic<Number, String> {
        @Override
        public void set(final Number key, final String value) {

        }

        @Override
        public String get(final Number key) {
            return null;
        }

        @Override
        public void put(final Number key, final String value, final boolean overwrite) {

        }
    }

}
