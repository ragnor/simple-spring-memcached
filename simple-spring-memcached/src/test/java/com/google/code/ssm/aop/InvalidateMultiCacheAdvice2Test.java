package com.google.code.ssm.aop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.InvalidateMultiCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.test.Point;

/**
 * Copyright (c) 2011 Jakub Białek
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
 * @author Jakub Białek
 * 
 */
public class InvalidateMultiCacheAdvice2Test extends AbstractCacheTest<InvalidateMultiCacheAdvice> {

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
                        { true, "method4", new Class[] { int.class }, new Object[] { 4 }, Arrays.asList(1, 2, 3, 4),
                                new String[] { NS + ":1", NS + ":2", NS + ":3", NS + ":4" } }, //
                        { true, "method5", new Class[] { int.class }, new Object[] { 5 },
                                Arrays.asList(new Point(1, 2), new Point(2, 3), new Point(3, 4), new Point(4, 5)),
                                new String[] { NS + ":(1,2)", NS + ":(2,3)", NS + ":(3,4)", NS + ":(4,5)" } }, //

                        { false, "method50", new Class[] { List.class }, new Object[] { Arrays.asList(1, 2, 3, 4) },
                                Arrays.asList(1, 2, 3, 4), null }, //
                        { false, "method51", new Class[] { int.class }, new Object[] { 51 }, null, null }, //
                        { false, "method52", new Class[] { int.class }, new Object[] { 52 }, "xyz", null }, //
                        { false, "method53", new Class[] { int.class }, new Object[] { 53 }, Arrays.asList(1, 2, 3, 4), null }, //
                });
    }

    private static final String NS = "ABC_SOME_NS";

    private Object expectedValue;

    private String[] cacheKeys;

    public InvalidateMultiCacheAdvice2Test(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params,
            Object expectedValue, String[] cacheKeys) {
        super(isValid, methodName, paramTypes, params, null);
        this.expectedValue = expectedValue;
        this.cacheKeys = cacheKeys;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void valid() throws Throwable {
        Assume.assumeTrue(isValid);

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheInvalidateMulti(pjp));

        verify(pjp).proceed();
        verify(client).delete(argThat(new BaseMatcher<Set<String>>() {

            @Override
            public boolean matches(Object arg0) {
                @SuppressWarnings("unchecked")
                Collection<String> set = (Collection<String>) arg0;
                Set<String> target = new HashSet<String>(Arrays.asList(cacheKeys));
                return set.containsAll(target) && target.containsAll(set);
            }

            @Override
            public void describeTo(Description arg0) {

            }
        }));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheInvalidateMulti(pjp));

        verify(pjp).proceed();
        verify(client, never()).delete(any(Collection.class));
        verify(client, never()).delete(anyString());
    }

    @Override
    protected InvalidateMultiCacheAdvice createAdvice() {
        return new InvalidateMultiCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method1(@ParameterValueKeyProvider List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method2(@ParameterValueKeyProvider(order = 2) String id1,
                @ParameterValueKeyProvider(order = 1) List<Integer> id2) {
            return Collections.<Integer> emptyList();
        }

        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method3(@ParameterValueKeyProvider(order = 1) List<Point> id1, @ParameterValueKeyProvider(order = 2) Point id2) {
            return Collections.<Integer> emptyList();
        }

        @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method4(int id1) {
            return Collections.<Integer> emptyList();
        }

        @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public List<Point> method5(int id1) {
            return Collections.<Point> emptyList();
        }

        // both @ParameterValueKeyProvider and @ReturnValueKeyProvider currently is valid
        @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method6(@ParameterValueKeyProvider List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        // no @ParameterValueKeyProvider or @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method50(List<Integer> id1) {
            return Collections.<Integer> emptyList();
        }

        // @ReturnValueKeyProvider but void return type
        @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public void method51(int id1) {

        }

        // @ReturnValueKeyProvider but return type is not List
        @ReturnValueKeyProvider
        @InvalidateMultiCache(namespace = NS)
        public String method52(int id1) {
            return null;
        }

        // no List parameter
        @InvalidateMultiCache(namespace = NS)
        public List<Integer> method53(@ParameterValueKeyProvider int id1) {
            return Collections.<Integer> emptyList();
        }
    }

}
