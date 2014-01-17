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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class ReadThroughMultiCacheAdviceTest extends AbstractCacheTest<ReadThroughMultiCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method2", new Class[] { String.class, List.class }, new Object[] { "xyz", Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" } }, //
                        {
                                true,
                                "method3",
                                new Class[] { List.class, Point.class },
                                new Object[] { Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                        new Point(6, 7) }, Arrays.asList(2, 4, 6, 8),
                                new String[] { NS + ":(1,2)/(6,7)", NS + ":(2,3)/(6,7)", NS + ":(3,4)/(6,7)", NS + ":(4,5)/(6,7)" } }, //
                        { true, "method4", new Class[] { int.class, String.class, List.class },
                                new Object[] { 4, "xyz", Arrays.asList(1, 2, 3, 4) }, Arrays.asList(1, 2, 3, 4),
                                new String[] { NS + ":1/xyz", NS + ":2/xyz", NS + ":3/xyz", NS + ":4/xyz" } }, //

                        { false, "method50", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(1, 2, 3, 4), null }, //
                        { false, "method51", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) }, null, null }, //
                        { false, "method52", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) }, null, null }, //
                        { false, "method53", new Class[] { int.class }, new Object[] { 53 }, Arrays.asList(1, 2, 3, 4), null }, //
                        { false, "method54", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(2, 4, 6, 8), null }, //

                });
    }

    private static final String NS = "ABC_NS";

    private static final int EXPIRATION = 321;

    private final List<?> expectedValue;

    private final String[] cacheKeys;

    public ReadThroughMultiCacheAdviceTest(final boolean isValid, final String methodName, final Class<?>[] paramTypes,
            final Object[] params, final List<?> expectedValue, final String[] cacheKeys) {
        super(isValid, methodName, paramTypes, params, null);
        this.expectedValue = expectedValue;
        this.cacheKeys = cacheKeys;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void validCacheAllMiss() throws Throwable {
        Assume.assumeTrue(isValid);

        when(pjp.proceed(params)).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheMulti(pjp));

        verify(cache).getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))), any(SerializationType.class));
        for (int i = 0; i < cacheKeys.length; i++) {
            verify(cache).setSilently(eq(cacheKeys[i]), eq(EXPIRATION), eq(expectedValue.get(i)), any(SerializationType.class));
        }
        verify(pjp).proceed(params);
    }

    @Test
    public void validCacheHit() throws Throwable {
        Assume.assumeTrue(isValid);
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < cacheKeys.length; i++) {
            map.put(cacheKeys[i], expectedValue.get(i));
        }

        when(cache.getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))), any(SerializationType.class))).thenReturn(map);

        assertEquals(expectedValue, advice.cacheMulti(pjp));

        verify(cache).getBulk(eq(new HashSet<String>(Arrays.asList(cacheKeys))), any(SerializationType.class));
        verify(cache, never()).setSilently(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(pjp, never()).proceed(params);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheMulti(pjp));

        verify(cache, never()).getBulk(any(Collection.class), any(SerializationType.class));
        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(pjp).proceed();
    }

    @Override
    protected ReadThroughMultiCacheAdvice createAdvice() {
        return new ReadThroughMultiCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
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

        // no @ParameterValueKeyProvider
        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method50(final List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        // void return type
        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public void method51(@ParameterValueKeyProvider final List<Integer> id1) {

        }

        // return type is not List
        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public String method52(@ParameterValueKeyProvider final List<Integer> id1) {
            return null;
        }

        // no List parameter
        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method53(@ParameterValueKeyProvider final int id1) {
            return Collections.<Integer> emptyList();
        }

        // ReturnValueKeyProvider is not supported by ReadThroughMultiCache
        @ReturnValueKeyProvider
        @ReadThroughMultiCache(namespace = NS, expiration = EXPIRATION)
        public List<Integer> method54(final List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

    }

}
