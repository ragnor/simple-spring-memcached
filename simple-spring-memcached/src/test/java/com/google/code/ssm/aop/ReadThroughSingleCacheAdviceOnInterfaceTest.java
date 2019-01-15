/* Copyright (c) 2018-2019 Jakub Białek
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

import static com.google.code.ssm.test.Matcher.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.google.code.ssm.api.BridgeMethodMapping;
import com.google.code.ssm.api.BridgeMethodMappings;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.format.SerializationType;

/**
 * 
 * Checks if caching works when annotations are defined on methods in generic interface and implemented by some class. Due to type erasure in java a {@link BridgeMethodMappings}
 * has to be used. 
 * 
 * @author Jakub Białek
 *
 */
public class ReadThroughSingleCacheAdviceOnInterfaceTest extends AbstractCacheTest<ReadThroughSingleCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                        { true, "method1", new Class[] { Integer.class }, new Object[] { 1 }, 1 }, //
                        { true, "method3", new Class[] { Object.class, Object.class }, new Object[] { 3, 44 }, 3 }, //
                });
    }

    private static final String NS = "TEST_NS";

    private static final int EXPIRATION = 110;

    private final Object expectedValue;

    public ReadThroughSingleCacheAdviceOnInterfaceTest(final boolean isValid, final String methodName, final Class<?>[] paramTypes,
            final Object[] params, final Object expectedValue) {
        super(isValid, methodName, paramTypes, params, null);
        this.expectedValue = expectedValue;
    }

    @Before
    public void setUp() {
        super.setUp(new TestServiceImpl());
        advice.getCacheBase().getSettings().setEnableAnnotationsInInterface(true);
    }

    @Test
    public void validCacheMiss() throws Throwable {
        Assume.assumeTrue(isValid);

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheGetSingle(pjp));

        verify(cache).get(eq(cacheKey), any(SerializationType.class));
        verify(cache).set(eq(cacheKey), eq(EXPIRATION), eq(expectedValue), any(SerializationType.class));
        verify(pjp).proceed();
    }

    @Test
    public void validCacheHit() throws Throwable {
        Assume.assumeTrue(isValid);

        when(cache.get(eq(cacheKey), any(SerializationType.class))).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheGetSingle(pjp));

        verify(cache).get(eq(cacheKey), any(SerializationType.class));
        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(pjp, never()).proceed();
    }

    @Test
    public void invalid() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));

        when(pjp.proceed()).thenReturn(expectedValue);

        assertEquals(expectedValue, advice.cacheGetSingle(pjp));

        verify(cache, never()).get(anyString(), any(SerializationType.class));
        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(pjp).proceed();
    }

    @Override
    protected ReadThroughSingleCacheAdvice createAdvice() {
        return new ReadThroughSingleCacheAdvice();
    }

    @Override
    protected String getNamespace() {
        return NS;
    }

    private interface TestService<T> {
        @ReadThroughSingleCache(namespace = NS, expiration = EXPIRATION)
        int method1(@ParameterValueKeyProvider Integer id);

        @ReadThroughSingleCache(namespace = NS, expiration = EXPIRATION)
        int method3(@ParameterValueKeyProvider(order = 1) T id1, @ParameterValueKeyProvider(order = 2) T id2);
    }
    
    @BridgeMethodMappings({ @BridgeMethodMapping(methodName = "method3", erasedParamTypes = { Object.class, Object.class }, targetParamTypes = { Integer.class, Integer.class }) })
    private static class TestServiceImpl implements Serializable, TestService<Integer> {

        private static final long serialVersionUID = 1L;


        public int method1(final Integer id) {
            return 1;
        }

 
        public int method3(final Integer id1, final Integer id2) {
            return 3;
        }

    }

}
