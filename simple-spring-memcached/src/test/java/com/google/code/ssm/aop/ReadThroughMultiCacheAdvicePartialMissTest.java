/* Copyright (c) 2011 Jakub Białek
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOption;
import com.google.code.ssm.impl.PertinentNegativeNull;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class ReadThroughMultiCacheAdvicePartialMissTest extends AbstractCacheTest<ReadThroughMultiCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" },
                                new Object[] { Arrays.asList(2, 4) }, of(NS + ":1", 2, NS + ":2", null, NS + ":3", 6, NS + ":4", null),
                                new int[] { 1, 3 } }, //
                        { true, "method2", new Class[] { String.class, List.class }, new Object[] { "xyz", Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { "xyz", Arrays.asList(2, 3) },
                                of(NS + ":1/xyz", 2, NS + ":2/xyz", null, NS + ":3/xyz", null, NS + ":4/xyz", 8), new int[] { 1, 2 } }, //
                        {
                                true,
                                "method3",
                                new Class[] { List.class, Point.class },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                        new Point(6, 7) }, Arrays.asList(2, 4, 6, 8),
                                new String[] { NS + ":(1,2)/(6,7)", NS + ":(2,3)/(6,7)", NS + ":(3,4)/(6,7)", NS + ":(4,5)/(6,7)" },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(3, 4)), new Point(6, 7) },
                                of(NS + ":(1,2)/(6,7)", null, NS + ":(2,3)/(6,7)", 4, NS + ":(3,4)/(6,7)", null, NS + ":(4,5)/(6,7)", 8),
                                new int[] { 0, 2 } }, //
                        { true, "method4", new Class[] { int.class, String.class, List.class },
                                new Object[] { 4, "xyz", Arrays.asList(1, 2, 3, 4) }, Arrays.asList(1, 2, 3, 4),
                                new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { 4, "xyz", Arrays.asList(1, 4) },
                                of(NS + ":1/xyz", null, NS + ":2/xyz", 2, NS + ":3/xyz", 3, NS + ":4/xyz", null), new int[] { 0, 3 } }, //
                        { true, "method4", new Class[] { int.class, String.class, List.class },
                                new Object[] { 4, "xyz", Arrays.asList(1, 2, 3, 4) }, Arrays.asList(null, 2, 3, 4),
                                new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { 4, "xyz", Arrays.asList(2) },
                                of(NS + ":1/xyz", PertinentNegativeNull.NULL, NS + ":2/xyz", null, NS + ":3/xyz", 3, NS + ":4/xyz", 4),
                                new int[] { 1 } }, //
                        { false, "method6", new Class[] { String.class, List.class }, new Object[] { "xyz", Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(null, 2, 3, 4), new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { "xyz", Arrays.asList(2) },
                                of(NS + ":1/xyz", PertinentNegativeNull.NULL, NS + ":2/xyz", null, NS + ":3/xyz", 3, NS + ":4/xyz", 4),
                                new int[] { 1 } }, //
                });
    }

    private static final String NS = "ABC_NS";

    private static final int EXPIRATION = 321;

    private List<?> expectedValue;

    private String[] cacheKeys;

    private Object[] missParams;

    private Map<String, Object> cacheHits;

    private int[] missedIndex;

    private boolean allowNullsInResults;

    public ReadThroughMultiCacheAdvicePartialMissTest(boolean allowNullsInResults, String methodName, Class<?>[] paramTypes,
            Object[] params, List<?> expectedValue, String[] cacheKeys, Object[] missParams, Map<String, Object> cacheHits,
            int[] missedIndex) {
        super(true, methodName, paramTypes, params, null);
        this.expectedValue = expectedValue;
        this.cacheKeys = cacheKeys;
        this.missParams = missParams;
        this.cacheHits = cacheHits;
        this.missedIndex = missedIndex;
        this.allowNullsInResults = allowNullsInResults;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void validCachePartialMiss() throws Throwable {
        Assume.assumeTrue(isValid);

        List<Object> missValues = new ArrayList<Object>();
        for (int i = 0; i < missedIndex.length; i++) {
            missValues.add(expectedValue.get(missedIndex[i]));
        }

        when(client.getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))))).thenReturn(cacheHits);
        when(pjp.proceed(missParams)).thenReturn(missValues);

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) expectedValue;
        if (!allowNullsInResults) {
            result = new ArrayList<Object>();
            for (Object o : expectedValue) {
                if (o != null) {
                    result.add(o);
                }
            }
        }
        assertEquals(result, advice.cacheMulti(pjp));

        verify(client).getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))));
        for (int i = 0; i < missedIndex.length; i++) {
            verify(client).set(eq(cacheKeys[missedIndex[i]]), eq(EXPIRATION), eq(expectedValue.get(missedIndex[i])));
        }
        verify(pjp).proceed(params);
    }

    @Override
    protected ReadThroughMultiCacheAdvice createAdvice() {
        return new ReadThroughMultiCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    protected static Map<String, Object> of(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);

        return map;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method1(@ParameterValueKeyProvider List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method2(@ParameterValueKeyProvider(order = 2) String id1,
                @ParameterValueKeyProvider(order = 1) List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method3(@ParameterValueKeyProvider(order = 1) List<Point> id1, @ParameterValueKeyProvider(order = 2) Point id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method4(int id, @ParameterValueKeyProvider(order = 2) String id1,
                @ParameterValueKeyProvider(order = 1) List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(addNullsToCache = true))
        public List<Integer> method5(@ParameterValueKeyProvider(order = 2) String id1,
                @ParameterValueKeyProvider(order = 1) List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(skipNullsInResult = true))
        public List<Integer> method6(@ParameterValueKeyProvider(order = 2) String id1,
                @ParameterValueKeyProvider(order = 1) List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(addNullsToCache = true, generateKeysFromResult = true))
        public List<Point> method7(@ParameterValueKeyProvider(order = 1) List<String> id2) {
            return Collections.<Point> emptyList();
        }

    }

}
