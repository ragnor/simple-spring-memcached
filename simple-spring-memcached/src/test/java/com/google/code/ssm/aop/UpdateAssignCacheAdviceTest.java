/* Copyright (c) 2012-2013 Jakub Białek
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
import static org.mockito.Mockito.when;

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
import com.google.code.ssm.api.UpdateAssignCache;
import com.google.code.ssm.api.format.SerializationType;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class UpdateAssignCacheAdviceTest extends AbstractCacheTest<UpdateAssignCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { int.class }, new Object[] { 11 }, 11, NS + ":1" }, //
                        { true, "method2", new Class[] { int.class }, new Object[] { 22 }, "22", NS + ":2" }, //
                        { true, "method3", new Class[] { int.class, int.class }, new Object[] { 33, 44 }, 33, NS + ":3" }, //
                        { true, "method4", new Class[] { String.class, int.class }, new Object[] { "some text", 44 }, "some text",
                                NS + ":4" }, //
                        { true, "method5", new Class[] { int.class }, new Object[] { 55 }, 55, NS + ":5" }, //

                        { false, "method51", new Class[] { int.class }, new Object[] { 51 }, 51, null }, //
                        { false, "method52", new Class[] { int.class }, new Object[] { 52 }, null, null }, //
                });
    }

    private static final String NS = "123XYZABC";

    private static final int EXPIRATION = 222;

    private final Object expectedValue;

    public UpdateAssignCacheAdviceTest(final boolean isValid, final String methodName, final Class<?>[] paramTypes, final Object[] params,
            final Object expectedValue, final String cacheKey) {
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

        advice.cacheUpdateAssign(pjp, expectedValue);

        verify(cache).set(eq(cacheKey), eq(EXPIRATION), eq(expectedValue), any(SerializationType.class));
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        advice.cacheUpdateAssign(pjp, expectedValue);

        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
    }

    @Override
    protected UpdateAssignCacheAdvice createAdvice() {
        return new UpdateAssignCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @ReturnDataUpdateContent
        @UpdateAssignCache(namespace = NS, assignedKey = "1", expiration = EXPIRATION)
        public int method1(final int id1) {
            return 1;
        }

        @ReturnDataUpdateContent
        @UpdateAssignCache(namespace = NS, assignedKey = "2", expiration = EXPIRATION)
        public String method2(@ParameterValueKeyProvider final int id1) {
            return "2";
        }

        @ReturnDataUpdateContent
        @UpdateAssignCache(namespace = NS, assignedKey = "3", expiration = EXPIRATION)
        public int method3(@ParameterValueKeyProvider(order = 1) final int id1, @ParameterValueKeyProvider(order = 2) final int id2) {
            return 3;
        }

        @UpdateAssignCache(namespace = NS, assignedKey = "4", expiration = EXPIRATION)
        public int method4(@ParameterDataUpdateContent final String s, @ParameterValueKeyProvider final int id1) {
            return 4;
        }

        @UpdateAssignCache(namespace = NS, assignedKey = "5", expiration = EXPIRATION)
        public int method5(@ParameterDataUpdateContent final int id1) {
            return 5;
        }

        // no @ParameterDataUpdateContent or @ReturnDataUpdateContent
        @UpdateAssignCache(namespace = NS, assignedKey = "51", expiration = EXPIRATION)
        public int method51(@ParameterValueKeyProvider final int id1) {
            return 51;
        }

        // @ReturnDataUpdateContent but void return type
        @ReturnDataUpdateContent
        @UpdateAssignCache(namespace = NS, assignedKey = "52", expiration = EXPIRATION)
        public void method52(@ParameterValueKeyProvider final int id1) {

        }

    }

}
