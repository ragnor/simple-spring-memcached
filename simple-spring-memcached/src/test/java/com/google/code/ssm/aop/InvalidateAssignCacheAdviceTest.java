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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class InvalidateAssignCacheAdviceTest extends AbstractCacheTest<InvalidateAssignCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                { true, "method1", new Class[] { int.class }, new Object[] { 11 }, 11, NS + ":1" }, //
                        { true, "method2", new Class[] { int.class }, new Object[] { 22 }, "22", NS + ":2" }, //
                });
    }

    private static final String NS = "TEST_NAMESPACE";

    private Object expectedValue;

    public InvalidateAssignCacheAdviceTest(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params,
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

        assertEquals(expectedValue, advice.cacheInvalidateAssign(pjp));

        verify(cache).delete(cacheKey);
        verify(pjp).proceed();
    }

    @Override
    protected InvalidateAssignCacheAdvice createAdvice() {
        return new InvalidateAssignCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    @SuppressWarnings("unused")
    private static class TestService {

        @InvalidateAssignCache(namespace = NS, assignedKey = "1")
        public int method1(int id1) {
            return 1;
        }

        @InvalidateAssignCache(namespace = NS, assignedKey = "2")
        public String method2(@ParameterValueKeyProvider int id1) {
            return "2";
        }
    }

}
