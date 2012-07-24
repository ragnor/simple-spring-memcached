/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

package com.google.code.ssm.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.ssm.Cache;
import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.InvalidAnnotationException;
import com.google.code.ssm.api.CacheKeyMethod;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.format.Serialization;
import com.google.code.ssm.api.format.SerializationType;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class CacheBaseTest {

    private CacheBase cut;

    @Before
    public void setUp() {
        cut = new CacheBase();
    }

    /*
     * @Test public void testKeyMethodArgs() throws Exception { try { cut.getKeyMethod(new KeyObject01());
     * fail("Expected exception."); } catch (InvalidAnnotationException ex) {
     * assertTrue(ex.getMessage().indexOf("0 arguments") != -1); System.out.println(ex.getMessage()); }
     * 
     * try { cut.getKeyMethod(new KeyObject02()); fail("Expected exception."); } catch (InvalidAnnotationException ex) {
     * assertTrue(ex.getMessage().indexOf("String") != -1); System.out.println(ex.getMessage()); }
     * 
     * try { cut.getKeyMethod(new KeyObject03()); fail("Expected exception."); } catch (InvalidAnnotationException ex) {
     * assertTrue(ex.getMessage().indexOf("String") != -1); System.out.println(ex.getMessage()); }
     * 
     * try { cut.getKeyMethod(new KeyObject04()); fail("Expected exception."); } catch (InvalidAnnotationException ex) {
     * assertTrue(ex.getMessage().indexOf("only one method") != -1); System.out.println(ex.getMessage()); }
     * 
     * assertEquals("doIt", cut.getKeyMethod(new KeyObject05()).getName()); assertEquals("toString",
     * cut.getKeyMethod(new KeyObject06(null)).getName()); }
     */
    /*
     * @Test public void testGenerateCacheKey() throws Exception { final Method method =
     * KeyObject.class.getMethod("toString", (Class<?>[]) null);
     * 
     * try { cut.generateObjectId(method, new KeyObject(null)); fail("Expected Exception."); } catch (RuntimeException
     * ex) { assertTrue(ex.getMessage().indexOf("empty key value") != -1); }
     * 
     * try { cut.generateObjectId(method, new KeyObject("")); fail("Expected Exception."); } catch (RuntimeException ex)
     * { assertTrue(ex.getMessage().indexOf("empty key value") != -1); }
     * 
     * final String result = "momma"; assertEquals(result, cut.generateObjectId(method, new KeyObject(result))); }
     */

    @Test
    public void testReturnTypeChecking() throws Exception {
        Method method = null;

        method = ReturnTypeCheck.class.getMethod("checkA", (Class<?>[]) null);
        cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

        method = ReturnTypeCheck.class.getMethod("checkB", (Class<?>[]) null);
        cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

        method = ReturnTypeCheck.class.getMethod("checkC", (Class<?>[]) null);
        cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

        method = ReturnTypeCheck.class.getMethod("checkD", (Class<?>[]) null);
        cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

        try {
            method = ReturnTypeCheck.class.getMethod("checkE", (Class<?>[]) null);
            cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);
            fail("Expected Exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().indexOf("requirement") != -1);
        }
    }

    @Test
    public void getSerializationType() throws Exception {
        Method method = null;
        SerializationType serializationType = null;

        method = SerializationTypeTestObject.class.getMethod("methodA", (Class<?>[]) null);
        serializationType = cut.getSerializationType(method);
        assertEquals(SerializationType.JAVA, serializationType);

        method = SerializationTypeTestObject.class.getMethod("methodB", (Class<?>[]) null);
        serializationType = cut.getSerializationType(method);
        assertEquals(SerializationType.JSON, serializationType);
    }

    @Test
    public void addCache() {
        String cacheName = "cache1";
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cache.getName()).thenReturn(cacheName);
        Mockito.when(cache.getAliases()).thenReturn(Arrays.asList("cacheA1", "cacheB1"));

        cut.addCache(cache);

        AnnotationData annotationData = new AnnotationData();
        annotationData.setCacheName(cacheName);
        assertSame(cache, cut.getCache(annotationData));

        annotationData.setCacheName("cacheA1");
        assertSame(cache, cut.getCache(annotationData));
    }

    @Test(expected = IllegalStateException.class)
    public void invalidAddCacheDuplicateName() {
        String cacheName = "cache1";
        Cache cache1 = Mockito.mock(Cache.class);
        Cache cache2 = Mockito.mock(Cache.class);

        Mockito.when(cache1.getName()).thenReturn(cacheName);
        Mockito.when(cache2.getName()).thenReturn(cacheName);

        cut.addCache(cache1);
        cut.addCache(cache2);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidAddCacheDuplicateAlias() {
        Cache cache1 = Mockito.mock(Cache.class);
        Cache cache2 = Mockito.mock(Cache.class);

        Mockito.when(cache1.getName()).thenReturn("cache1");
        Mockito.when(cache2.getName()).thenReturn("cache2");
        Mockito.when(cache2.getAliases()).thenReturn(Arrays.asList("cache1"));

        cut.addCache(cache1);
        cut.addCache(cache2);
    }

    @Test
    public void verifyTypeIsList() {
        assertTrue(cut.verifyTypeIsList(List.class));
        assertTrue(cut.verifyTypeIsList(ArrayList.class));
        assertTrue(cut.verifyTypeIsList(LinkedList.class));
        assertTrue(cut.verifyTypeIsList(ArrayList.class));

        assertFalse(cut.verifyTypeIsList(Integer.class));
        assertFalse(cut.verifyTypeIsList(Collection.class));
        assertFalse(cut.verifyTypeIsList(Map.class));
        assertFalse(cut.verifyTypeIsList(Set.class));
    }

    @Test
    public void getUpdateData() throws Exception {
        AnnotationData annotationData = new AnnotationData();
        annotationData.setDataIndex(-1);
        Object entity = new Object();

        Object result = cut.getUpdateData(annotationData, null, null, entity);
        assertSame(entity, result);

        annotationData = new AnnotationData();
        annotationData.setReturnDataIndex(true);
        result = cut.getUpdateData(annotationData, null, null, entity);
        assertSame(entity, result);

        annotationData = new AnnotationData();
        annotationData.setDataIndex(1);
        Method method = UpdateData.class.getMethod("update", new Class[] { int.class, Object.class });
        result = cut.getUpdateData(annotationData, method, new Object[] { 144, entity }, entity);
        assertSame(entity, result);

    }

    @SuppressWarnings("unused")
    @Serialization(SerializationType.JAVA)
    private static class SerializationTypeTestObject {

        public int methodA() {
            return 1;
        }

        @Serialization(SerializationType.JSON)
        public int methodB() {
            return 1;
        }

    }

    @SuppressWarnings("unused")
    private static class UpdateData {

        public void update(final int id, final Object entity) {

        }
    }

    @SuppressWarnings("unused")
    private static class ReturnTypeCheck {

        @ReadThroughMultiCache(namespace = "bubba", expiration = 10)
        public List<?> checkA() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "bubba", expiration = 10)
        public List<String> checkB() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "bubba", expiration = 10)
        public ArrayList<?> checkC() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "bubba", expiration = 10)
        public ArrayList<String> checkD() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "bubba", expiration = 10)
        public String checkE() {
            return null;
        }

    }

    @SuppressWarnings("unused")
    private static class KeyObject {
        private final String result;

        private KeyObject(final String result) {
            this.result = result;
        }

        @Override
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

    @SuppressWarnings("unused")
    private static class KeyObject06 {
        private final String result;

        private KeyObject06(final String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return result;
        }
    }
}
