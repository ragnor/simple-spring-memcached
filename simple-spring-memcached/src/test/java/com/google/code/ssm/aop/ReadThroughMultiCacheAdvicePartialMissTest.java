/* Copyright (c) 2012-2014 Jakub Białek
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
import static org.mockito.Matchers.any;
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

import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOption;
import com.google.code.ssm.api.format.SerializationType;
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
                                new int[] { 1, 3 }, null }, //
                        { true, "method2", new Class[] { String.class, List.class }, new Object[] { "xyz", Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { "xyz", Arrays.asList(2, 3) },
                                of(NS + ":1/xyz", 2, NS + ":2/xyz", null, NS + ":3/xyz", null, NS + ":4/xyz", 8), new int[] { 1, 2 }, null }, //
                        {
                                true,
                                "method3",
                                new Class[] { List.class, Point.class },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                        new Point(6, 7) }, Arrays.asList(2, 4, 6, 8),
                                new String[] { NS + ":(1,2)/(6,7)", NS + ":(2,3)/(6,7)", NS + ":(3,4)/(6,7)", NS + ":(4,5)/(6,7)" },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(3, 4)), new Point(6, 7) },
                                of(NS + ":(1,2)/(6,7)", null, NS + ":(2,3)/(6,7)", 4, NS + ":(3,4)/(6,7)", null, NS + ":(4,5)/(6,7)", 8),
                                new int[] { 0, 2 }, null }, //
                        { true, "method4", new Class[] { int.class, String.class, List.class },
                                new Object[] { 4, "xyz", Arrays.asList(1, 2, 3, 4) }, Arrays.asList(1, 2, 3, 4),
                                new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { 4, "xyz", Arrays.asList(1, 4) },
                                of(NS + ":1/xyz", null, NS + ":2/xyz", 2, NS + ":3/xyz", 3, NS + ":4/xyz", null), new int[] { 0, 3 }, null }, //
                        { true, "method4", new Class[] { int.class, String.class, List.class },
                                new Object[] { 4, "xyz", Arrays.asList(1, 2, 3, 4) }, Arrays.asList(null, 2, 3, 4),
                                new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { 4, "xyz", Arrays.asList(2) },
                                of(NS + ":1/xyz", PertinentNegativeNull.NULL, NS + ":2/xyz", null, NS + ":3/xyz", 3, NS + ":4/xyz", 4),
                                new int[] { 1 }, null }, //
                        { false, "method6", new Class[] { String.class, List.class }, new Object[] { "xyz", Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(null, 2, 3, 4), new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" },
                                new Object[] { "xyz", Arrays.asList(2) },
                                of(NS + ":1/xyz", PertinentNegativeNull.NULL, NS + ":2/xyz", null, NS + ":3/xyz", 3, NS + ":4/xyz", 4),
                                new int[] { 1 }, null }, //
                        { true, "method7", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(null, 2, 3, 4), new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" },
                                new Object[] { Arrays.asList(2) },
                                of(NS + ":1", PertinentNegativeNull.NULL, NS + ":2", null, NS + ":3", 3, NS + ":4", 4), new int[] { 1 },
                                null },//
                        { false, "method8", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) }, Arrays.asList(2, 4),
                                new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" }, new Object[] { Arrays.asList(2, 3) },
                                of(NS + ":1", PertinentNegativeNull.NULL, NS + ":2", null, NS + ":3", null, NS + ":4", 4),
                                new int[] { 1, 2 }, Arrays.asList(2) },//
                });
    }

    private static final String NS = "ABC_NS";

    private static final int EXPIRATION = 321;

    private final List<?> expectedValue;

    private final String[] cacheKeys;

    private final Object[] missParams;

    private final Map<String, Object> cacheHits;

    private final int[] missedIndex;

    private final boolean allowNullsInResults;

    private final List<Object> missValues;

    public ReadThroughMultiCacheAdvicePartialMissTest(final boolean allowNullsInResults, final String methodName,
            final Class<?>[] paramTypes, final Object[] params, final List<?> expectedValue, final String[] cacheKeys,
            final Object[] missParams, final Map<String, Object> cacheHits, final int[] missedIndex, final List<Object> missValues) {
        super(true, methodName, paramTypes, params, null);
        this.expectedValue = expectedValue;
        this.cacheKeys = cacheKeys;
        this.missParams = missParams;
        this.cacheHits = cacheHits;
        this.missedIndex = missedIndex;
        this.allowNullsInResults = allowNullsInResults;
        if (missValues != null) {
            this.missValues = missValues;
        } else {
            this.missValues = new ArrayList<Object>();
            for (int element : missedIndex) {
                this.missValues.add(expectedValue.get(element));
            }
        }
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validCachePartialMiss() throws Throwable {
        Assume.assumeTrue(isValid);

        when(cache.getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))), any(SerializationType.class))).thenReturn(cacheHits);
        when(pjp.proceed(missParams)).thenReturn(missValues);

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

        verify(cache).getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))), any(SerializationType.class));
        if (expectedValue.size() == cacheKeys.length) {
            for (int element : missedIndex) {
                verify(cache).setSilently(eq(cacheKeys[element]), eq(EXPIRATION), eq(expectedValue.get(element)),
                        any(SerializationType.class));
            }
        }
        verify(pjp).proceed(missParams);
    }

    @Override
    protected ReadThroughMultiCacheAdvice createAdvice() {
        return new ReadThroughMultiCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    protected static Map<String, Object> of(final String k1, final Object v1, final String k2, final Object v2, final String k3,
            final Object v3, final String k4, final Object v4) {
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
        public List<Integer> method1(@ParameterValueKeyProvider final List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method2(@ParameterValueKeyProvider(order = 2) final String id1,
                @ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method3(@ParameterValueKeyProvider(order = 1) final List<Point> id1,
                @ParameterValueKeyProvider(order = 2) final Point id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method4(final int id, @ParameterValueKeyProvider(order = 2) final String id1,
                @ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(addNullsToCache = true))
        public List<Integer> method5(@ParameterValueKeyProvider(order = 2) final String id1,
                @ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(skipNullsInResult = true))
        public List<Integer> method6(@ParameterValueKeyProvider(order = 2) final String id1,
                @ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(addNullsToCache = true, generateKeysFromResult = true))
        public List<Integer> method7(@ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION, option = @ReadThroughMultiCacheOption(skipNullsInResult = true, generateKeysFromResult = true))
        public List<Integer> method8(@ParameterValueKeyProvider(order = 1) final List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

    }

}
