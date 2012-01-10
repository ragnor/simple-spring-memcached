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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.ReadThroughMultiCache;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class ReadThroughMultiCacheTest {
    private static ReadThroughMultiCacheAdvice cut;
    private ReadThroughMultiCacheAdvice.MultiCacheCoordinator coord;

    @BeforeClass
    public static void beforeClass() {
        cut = new ReadThroughMultiCacheAdvice();
    }

    @Before
    public void beforeMethod() {
        coord = new ReadThroughMultiCacheAdvice.MultiCacheCoordinator(null, null);
    }

    @Test
    public void testConvertIdObject() throws Exception {
        final AnnotationData data = new AnnotationData();
        data.setNamespace(RandomStringUtils.randomAlphanumeric(6));
        final Map<String, Object> expectedString2Object = new HashMap<String, Object>();
        final Map<Object, String> expectedObject2String = new HashMap<Object, String>();
        final List<Object> idObjects = new ArrayList<Object>();
        final int length = 10;
        for (int ix = 0; ix < length; ix++) {
            final String object = RandomStringUtils.randomAlphanumeric(2 + ix);
            final String key = cut.cacheKeyBuilder.getCacheKey(object, data.getNamespace());
            idObjects.add(object);
            expectedObject2String.put(object, key);
            expectedString2Object.put(key, object);
        }

        /*final List<Object> exceptionObjects = new ArrayList<Object>(idObjects);
        Object[] objs = new Object[] { exceptionObjects };
        exceptionObjects.add(null);
        try {
            cut.convertIdObjectsToKeyMap(exceptionObjects, objs, 0, data);
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
        }

        for (int ix = 0; ix < length; ix++) {
            if (ix % 2 == 0) {
                idObjects.add(idObjects.get(ix));
            }
        }
        assertTrue(idObjects.size() > length);

        final ReadThroughMultiCacheAdvice.MapHolder holder = cut.convertIdObjectsToKeyMap(idObjects, new Object[] { idObjects }, 0, data);

        assertEquals(length, holder.getKey2Obj().size());
        assertEquals(length, holder.getObj2Key().size());

        assertEquals(expectedObject2String, holder.getObj2Key());
        assertEquals(expectedString2Object, holder.getKey2Obj());

        coord.setHolder(holder);

        assertEquals(expectedObject2String, coord.getObj2Key());
        assertEquals(expectedString2Object, coord.getKey2Obj());
*/
    }

    @Test
    public void testInitialKey2Result() {
        final AnnotationData annotation = new AnnotationData();
        annotation.setNamespace(RandomStringUtils.randomAlphanumeric(6));
        final Map<String, Object> expectedString2Object = new HashMap<String, Object>();
        final Map<String, Object> key2Result = new HashMap<String, Object>();
        final Set<Object> missObjects = new HashSet<Object>();
        final int length = 15;
        for (int ix = 0; ix < length; ix++) {

            final String object = RandomStringUtils.randomAlphanumeric(2 + ix);
            final String key = cut.cacheKeyBuilder.getCacheKey(object, annotation.getNamespace());
            expectedString2Object.put(key, object);

            // There are 3 possible outcomes when fetching by key from memcached:
            // 0) You hit, and the key & result are in the map
            // 1) You hit, but the result is null, which counts as a miss.
            // 2) You miss, and the key doesn't even get into the result map.
            final int option = RandomUtils.nextInt(3);
            if (option == 0) {
                key2Result.put(key, key + RandomStringUtils.randomNumeric(5));
            }
            if (option == 1) {
                key2Result.put(key, null);
                missObjects.add(object);
            }
            if (option == 2) {
                missObjects.add(object);
            }
        }

        try {
            coord.setInitialKey2Result(null);
            fail("Expected Exception.");
        } catch (RuntimeException ex) {
        }

        coord.getKey2Obj().putAll(expectedString2Object);
        coord.setInitialKey2Result(key2Result);

        assertTrue(coord.getMissedObjects().containsAll(missObjects));
        assertTrue(missObjects.containsAll(coord.getMissedObjects()));

    }

    @Test
    public void testGenerateResultsException() {
        final List<Object> keyObjects = new ArrayList<Object>();
        final Map<Object, String> obj2key = new HashMap<Object, String>();
        final String keyObject = RandomStringUtils.randomAlphanumeric(8);
        final String key = keyObject + "-" + RandomStringUtils.randomAlphanumeric(4);
        keyObjects.add(keyObject);
        obj2key.put(keyObject, key);
        AnnotationData data = new AnnotationData();
        data.setListIndexInKeys(0);
        coord = new ReadThroughMultiCacheAdvice.MultiCacheCoordinator(null, data);
        coord.setListKeyObjects(keyObjects);
        coord.getObj2Key().putAll(obj2key);

        try {
            coord.generateResultList();
            fail("Expected Exception");
        } catch (RuntimeException ex) {
        }

    }

    @Test
    public void testGenerateResults() {
        final List<Object> keyObjects = new ArrayList<Object>();
        final Map<Object, String> obj2key = new HashMap<Object, String>();
        final Map<String, Object> key2result = new HashMap<String, Object>();
        final List<Object> expectedResults = new ArrayList<Object>();
        final int length = 10;
        for (int ix = 0; ix < length; ix++) {
            final String keyObject = RandomStringUtils.randomAlphanumeric(8);
            final String key = keyObject + "-" + RandomStringUtils.randomAlphanumeric(4);

            keyObjects.add(keyObject);
            obj2key.put(keyObject, key);

            if (RandomUtils.nextBoolean()) {
                final String result = RandomStringUtils.randomAlphanumeric(15);
                key2result.put(key, result);
                expectedResults.add(result);
            } else {
                key2result.put(key, new PertinentNegativeNull());
                expectedResults.add(null);
            }
        }

        AnnotationData data = new AnnotationData();
        data.setListIndexInKeys(0);
        coord = new ReadThroughMultiCacheAdvice.MultiCacheCoordinator(null, data);
        coord.setListKeyObjects(keyObjects);
        coord.getObj2Key().putAll(obj2key);
        coord.getKey2Result().putAll(key2result);

        final List<Object> results = coord.generateResultList();

        assertEquals(length, results.size());
        assertEquals(expectedResults, results);
    }

    @SuppressWarnings("unused")
    private static class AnnotationValidator {

        @ReadThroughMultiCache(namespace = "bubba")
        public String cacheMe1() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "")
        public String cacheMe2() {
            return null;
        }

        @ReadThroughMultiCache()
        public String cacheMe3() {
            return null;
        }

        @ReadThroughMultiCache(namespace = "bubba", expiration = -1)
        public String cacheMe4() {
            return null;
        }
    }

}
