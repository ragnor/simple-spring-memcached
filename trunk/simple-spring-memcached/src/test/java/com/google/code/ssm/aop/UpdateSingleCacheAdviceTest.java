/* Copyright (c) 2012 Jakub Białek
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.UpdateSingleCache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class UpdateSingleCacheAdviceTest extends AbstractCacheTest<UpdateSingleCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { int.class }, new Object[] { 1 }, 2, 2, null }, //
                        { true, "method2", new Class[] { int.class }, new Object[] { 2 }, "3", "3", null }, //
                        { true, "method3", new Class[] { int.class, int.class }, new Object[] { 3, 44 }, 4, 4, null }, //
                        { true, "method4", new Class[] { String.class, int.class }, new Object[] { "some text", 4 }, "return text",
                                "some text", NS + ":4" }, //
                        { true, "method5", new Class[] { int.class }, new Object[] { 4 }, 5, 4, null }, //
                        { true, "method6", new Class[] { int.class, Point.class }, new Object[] { 6, new Point(1, 2) }, "7", "7",
                                NS + ":(1,2)/6" }, //
                        { true, "method7", new Class[] { int.class, String.class, Point.class }, new Object[] { 7, "8", new Point(1, 2) },
                                new Point(1, 1), new Point(1, 2), NS + ":8/7" }, //
                        { true, "method8", new Class[] { int.class }, new Object[] { 8 }, 9, 9, null }, //

                        { false, "method50", new Class[] { int.class }, new Object[] { 50 }, 50, null, null }, //
                        { false, "method51", new Class[] { int.class }, new Object[] { 51 }, 51, null, null }, //
                        { false, "method52", new Class[] { int.class }, new Object[] { 52 }, null, null, null }, //
                });
    }

    private static final String NS = "XYZABC";

    private static final int EXPIRATION = 222;

    private final Object returnValue;

    private final Object expectedValue;

    public UpdateSingleCacheAdviceTest(final boolean isValid, final String methodName, final Class<?>[] paramTypes, final Object[] params,
            final Object returnValue, final Object expectedValue, final String cacheKey) {
        super(isValid, methodName, paramTypes, params, cacheKey);
        this.expectedValue = expectedValue;
        this.returnValue = returnValue;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void valid() throws Throwable {
        Assume.assumeTrue(isValid);

        advice.cacheUpdateSingle(pjp, returnValue);

        verify(cache).set(eq(cacheKey), eq(EXPIRATION), eq(expectedValue), any(SerializationType.class));
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        advice.cacheUpdateSingle(pjp, returnValue);

        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
    }

    @Override
    protected UpdateSingleCacheAdvice createAdvice() {
        return new UpdateSingleCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method1(@ParameterValueKeyProvider final int id1) {
            return 1;
        }

        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public String method2(@ParameterValueKeyProvider final int id1) {
            return "2";
        }

        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method3(@ParameterValueKeyProvider(order = 1) final int id1, @ParameterValueKeyProvider(order = 2) final int id2) {
            return 3;
        }

        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method4(@ParameterDataUpdateContent final String s, @ParameterValueKeyProvider final int id1) {
            return 4;
        }

        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method5(@ParameterDataUpdateContent @ParameterValueKeyProvider final int id1) {
            return 5;
        }

        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public String method6(@ParameterValueKeyProvider(order = 2) final int id1, @ParameterValueKeyProvider(order = 1) final Point p) {
            return "6";
        }

        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public String method7(@ParameterValueKeyProvider(order = 2) final int id1, @ParameterValueKeyProvider(order = 1) final String s,
                @ParameterDataUpdateContent final Point p) {
            return "7";
        }

        // currently it's valid to use both @ParameterDataUpdateContent and @ReturnDataUpdateContent
        // in such case @ReturnDataUpdateContent takes precedence
        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method8(@ParameterDataUpdateContent @ParameterValueKeyProvider final int id1) {
            return 8;
        }

        // no @ParameterValueKeyProvider
        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method50(final int id1) {
            return 50;
        }

        // no @ParameterDataUpdateContent or @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public int method51(@ParameterValueKeyProvider final int id1) {
            return 51;
        }

        // @ReturnDataUpdateContent but void return type
        @ReturnDataUpdateContent
        @UpdateSingleCache(namespace = NS, expiration = EXPIRATION)
        public void method52(@ParameterValueKeyProvider final int id1) {

        }

    }

}
