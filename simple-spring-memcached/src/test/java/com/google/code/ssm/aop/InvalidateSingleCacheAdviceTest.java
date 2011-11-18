package com.google.code.ssm.aop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.test.Point;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

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
public class InvalidateSingleCacheAdviceTest extends AbstractCacheTest<InvalidateSingleCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                { true, "method1", new Class[] { int.class }, new Object[] { 1 }, 1, null }, //
                        { true, "method2", new Class[] { int.class }, new Object[] { 2 }, "2", null }, //
                        { true, "method3", new Class[] { int.class, int.class }, new Object[] { 3, 44 }, 3, null }, //
                        { true, "method4", new Class[] { int.class }, new Object[] { 4 }, 4, null }, //
                        { true, "method5", new Class[] { int.class, Point.class }, new Object[] { 5, new Point(1, 2) }, "5", NS + ":(1,2)/5" }, //

                        { false, "method50", new Class[] { int.class }, new Object[] { 50 }, 50, null }, //
                        { false, "method51", new Class[] { int.class }, new Object[] { 51 }, 51, null }, //
                        { false, "method52", new Class[] { int.class }, new Object[] { 52 }, null, null }, //
                        { false, "method53", new Class[] { int.class }, new Object[] { 53 }, 53, null }, //
                });
    }

    private static final String NS = "SOME_NS";

    private Object expectedValue;

    public InvalidateSingleCacheAdviceTest(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params,
            Object expectedValue, String cacheKey) {
        super(isValid, methodName, paramTypes, params, cacheKey);
        this.expectedValue = expectedValue;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void valid() throws Throwable {
        Assume.assumeTrue(isValid);

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheInvalidateSingle(pjp));

        verify(client).delete(cacheKey);
        verify(pjp).proceed();
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheInvalidateSingle(pjp));

        verify(pjp).proceed();
    }

    @Override
    protected InvalidateSingleCacheAdvice createAdvice() {
        return new InvalidateSingleCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @InvalidateSingleCache(namespace = NS)
        public int method1(@ParameterValueKeyProvider int id1) {
            return 1;
        }

        @ReturnDataUpdateContent
        @InvalidateSingleCache(namespace = NS)
        public String method2(@ParameterValueKeyProvider int id1) {
            return "2";
        }

        @ReturnDataUpdateContent
        @InvalidateSingleCache(namespace = NS)
        public int method3(@ParameterValueKeyProvider(order = 1) int id1, @ParameterValueKeyProvider(order = 2) int id2) {
            return 3;
        }

        @InvalidateSingleCache(namespace = NS)
        public int method4(@ParameterDataUpdateContent @ParameterValueKeyProvider int id1) {
            return 4;
        }

        @InvalidateSingleCache(namespace = NS)
        public String method5(@ParameterValueKeyProvider(order = 2) int id1, @ParameterDataUpdateContent @ParameterValueKeyProvider(order = 1) Point p) {
            return "5";
        }

        // no @ParameterValueKeyProvider or @ReturnValueKeyProvider
        @InvalidateSingleCache(namespace = NS)
        public int method50(int id1) {
            return 50;
        }

        // no @ParameterValueKeyProvider or @ReturnValueKeyProvider
        @InvalidateSingleCache(namespace = NS)
        public int method51(int id1) {
            return 51;
        }

        // @ReturnValueKeyProvider but void return type
        @ReturnValueKeyProvider
        @InvalidateSingleCache(namespace = NS)
        public void method52(int id1) {

        }

        // both @ParameterValueKeyProvider and @ReturnValueKeyProvider
        @ReturnValueKeyProvider
        @InvalidateSingleCache(namespace = NS)
        public int method53(@ParameterValueKeyProvider int id1) {
            return 53;
        }

    }

}
