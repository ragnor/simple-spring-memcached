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

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughAssignCache;

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
public class ReadThroughAssignCacheAdviceTest extends AbstractCacheTest<ReadThroughAssignCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                { true, "method1", new Class[] { int.class }, new Object[] { 22 }, 11, NS + ":1" }, //
                        { true, "method2", new Class[] { int.class }, new Object[] { 22 }, "33", NS + ":2" }, //
                        { true, "method3", new Class[] { int.class, int.class }, new Object[] { 33, 44 }, 33, NS + ":3" }, //

                        { false, "method51", new Class[] { int.class }, new Object[] { 51 }, null, null }, //
                });
    }

    private static final String NS = "TEST-NS";

    private static final int EXPIRATION = 220;

    private Object expectedValue;

    public ReadThroughAssignCacheAdviceTest(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params,
            Object expectedValue, String cacheKey) {
        super(isValid, methodName, paramTypes, params, cacheKey);
        this.expectedValue = expectedValue;
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }

    @Test
    public void validCacheMiss() throws Throwable {
        Assume.assumeTrue(isValid);

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheSingleAssign(pjp));

        verify(client).get(eq(cacheKey));
        verify(client).set(eq(cacheKey), eq(EXPIRATION), eq(expectedValue));
        verify(pjp).proceed();
    }

    @Test
    public void validCacheHit() throws Throwable {
        Assume.assumeTrue(isValid);

        when(client.get(eq(cacheKey))).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheSingleAssign(pjp));

        verify(client).get(eq(cacheKey));
        verify(client, never()).set(anyString(), anyInt(), any());
        verify(pjp, never()).proceed();
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheSingleAssign(pjp));

        verify(client, never()).get(anyString());
        verify(client, never()).set(anyString(), anyInt(), any());
        verify(pjp).proceed();
    }

    @Override
    protected ReadThroughAssignCacheAdvice createAdvice() {
        return new ReadThroughAssignCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @ReadThroughAssignCache(namespace = NS, assignedKey = "1", expiration = EXPIRATION)
        public int method1(int id1) {
            return 1;
        }

        @ReadThroughAssignCache(namespace = NS, assignedKey = "2", expiration = EXPIRATION)
        public String method2(@ParameterValueKeyProvider int id1) {
            return "2";
        }

        @ReadThroughAssignCache(namespace = NS, assignedKey = "3", expiration = EXPIRATION)
        public int method3(@ParameterValueKeyProvider(order = 1) int id1, @ParameterValueKeyProvider(order = 2) int id2) {
            return 3;
        }

        // void method
        @ReadThroughAssignCache(namespace = NS, assignedKey = "51", expiration = EXPIRATION)
        public void method51(@ParameterValueKeyProvider int id1) {

        }

    }

}
