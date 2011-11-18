package com.google.code.ssm.impl;

import static org.junit.Assert.*;
import com.google.code.ssm.exceptions.InvalidAnnotationException;
import com.google.code.ssm.aop.CacheKeyMethodStoreImpl;
import com.google.code.ssm.api.CacheKeyMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.security.InvalidParameterException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
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
 * @author Nelson Carpentier
 * 
 */
public class DefaultKeyProviderTest {
    private static DefaultKeyProvider cut;

    @BeforeClass
    public static void beforeClass() {
        cut = new DefaultKeyProvider();
        cut.setMethodStore(new CacheKeyMethodStoreImpl());
    }

    @Test
    public void testKeyMethodArgs() throws Exception {
        try {
            cut.getKeyMethod(new KeyObject01());
            fail("Expected exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().indexOf("0 arguments") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            cut.getKeyMethod(new KeyObject02());
            fail("Expected exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().indexOf("String") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            cut.getKeyMethod(new KeyObject03());
            fail("Expected exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().indexOf("String") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            cut.getKeyMethod(new KeyObject04());
            fail("Expected exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().indexOf("only one method") != -1);
            System.out.println(ex.getMessage());
        }

        assertEquals("doIt", cut.getKeyMethod(new KeyObject05()).getName());
        assertEquals("toString", cut.getKeyMethod(new KeyObject06(null)).getName());
    }

    @Test
    public void testGenerateCacheKey() throws Exception {
        final Method method = KeyObject.class.getMethod("toString", (Class<?>[]) null);

        try {
            cut.generateObjectId(method, new KeyObject(null));
            fail("Expected Exception.");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().indexOf("empty key value") != -1);
        }

        try {
            cut.generateObjectId(method, new KeyObject(""));
            fail("Expected Exception.");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().indexOf("empty key value") != -1);
        }

        final String result = "momma";
        assertEquals(result, cut.generateObjectId(method, new KeyObject(result)));
    }

    @Test
    public void testGenerateExceptions() {
        try {
            cut.generateKey(null);
            fail("Expected exception");
        } catch (InvalidParameterException ex) {
        }
    }

    @Test
    public void testMultiplexExceptions() {
        try {
            cut.generateKeys(null);
            fail("Expected exception");
        } catch (InvalidParameterException ex) {
        }

        try {
            cut.generateKeys(new ArrayList<Object>());
            fail("Expected exception");
        } catch (InvalidParameterException ex) {
        }

    }

    @Test
    public void testMultiplex() {
        final List<Object> src = new ArrayList<Object>();
        final List<String> expectedResults = new ArrayList<String>();
        final String data = "abcdefghijklmnopqrstuvwxyz";
        for (int ix = 0; ix < 4; ix++) {
            final String val = data.substring(0, 5 + ix);
            src.add(new KeyObject(val));
            expectedResults.add(val);
        }

        final List<String> results = cut.generateKeys(src);
        assertEquals(expectedResults, results);
    }

    private static class KeyObject {
        private String result;

        private KeyObject(String result) {
            this.result = result;
        }

        public String toString() {
            return result;
        }
    }

    @SuppressWarnings("unused")
    private static class KeyObject01 {

        @CacheKeyMethod
        public void doIt(final String nonsense) {
        }

    }

    @SuppressWarnings("unused")
    private static class KeyObject02 {

        @CacheKeyMethod
        public void doIt() {
        }
    }

    @SuppressWarnings("unused")
    private static class KeyObject03 {

        @CacheKeyMethod
        public Long doIt() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class KeyObject04 {

        @CacheKeyMethod
        public String doIt() {
            return null;
        }

        @CacheKeyMethod
        public String doItAgain() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class KeyObject05 {

        public static final String result = "shrimp";

        @CacheKeyMethod
        public String doIt() {
            return result;
        }
    }

    private static class KeyObject06 {

        private String result;

        private KeyObject06(String result) {
            this.result = result;
        }

        public String toString() {
            return result;
        }
    }

}
