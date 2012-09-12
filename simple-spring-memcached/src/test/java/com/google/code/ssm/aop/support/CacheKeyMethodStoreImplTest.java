/*
 * Copyright (c) 2012 Jakub Białek
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

import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.api.CacheKeyMethod;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class CacheKeyMethodStoreImplTest {

    private static CacheKeyMethodStoreImpl cacheKeyMethodStoreImpl;

    @BeforeClass
    public static void beforeClass() {
        cacheKeyMethodStoreImpl = new CacheKeyMethodStoreImpl();
    }

    @Test
    public void testNoInheritance() throws NoSuchMethodException {
        Method cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(Object.class);
        assertNotNull(cacheKeyMethod);
        assertEquals("toString", cacheKeyMethod.getName());

        cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(Point.class);
        assertNotNull(cacheKeyMethod);
        assertEquals("getKey", cacheKeyMethod.getName());
    }

    @Test
    public void testInheritance() throws NoSuchMethodException {
        Method cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(AClass.class);
        assertEquals("getAKey", cacheKeyMethod.getName());

        cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(BClass.class);
        assertEquals("getBKey", cacheKeyMethod.getName());

        cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(CClass.class);
        assertEquals("getCKey", cacheKeyMethod.getName());

        cacheKeyMethod = cacheKeyMethodStoreImpl.getKeyMethod(DClass.class);
        assertEquals("getBKey", cacheKeyMethod.getName());
    }

    @Test(expected = InvalidAnnotationException.class)
    public void invalidTwoCacheKeyMethods() throws NoSuchMethodException {
        cacheKeyMethodStoreImpl.getKeyMethod(TwoCacheKeyMethodsModel.class);
    }

    @Test(expected = InvalidAnnotationException.class)
    public void invalidMethodSignature() throws NoSuchMethodException {
        cacheKeyMethodStoreImpl.getKeyMethod(InvalidCacheKeyMethodSignatureModel.class);
    }

    @Test(expected = InvalidAnnotationException.class)
    public void invalidMethodSignature2() throws NoSuchMethodException {
        cacheKeyMethodStoreImpl.getKeyMethod(InvalidCacheKeyMethodSignatureModel2.class);
    }

    public static class TwoCacheKeyMethodsModel {

        @CacheKeyMethod
        public String getKey() {
            return null;
        }

        @CacheKeyMethod
        public String getCacheKey() {
            return null;
        }

    }

    public static class InvalidCacheKeyMethodSignatureModel {

        @CacheKeyMethod
        public String getKey(final String param) {
            return null;
        }

    }

    public static class InvalidCacheKeyMethodSignatureModel2 {

        @CacheKeyMethod
        public Integer getKey() {
            return null;
        }

    }

    public static class AClass {
        @CacheKeyMethod
        public String getAKey() {
            return "A";
        }
    }

    public static class BClass extends AClass {
        @CacheKeyMethod
        public String getBKey() {
            return "B";
        }
    }

    public static class CClass extends BClass {
        @CacheKeyMethod
        public String getCKey() {
            return "C";
        }
    }

    public static class DClass extends BClass {
        @Override
        public String toString() {
            return "D";
        }
    }

}
