package com.google.code.ssm.aop.counter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.counter.IncrementCounterInCache;

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
public class IncrementCounterInCacheAdviceTest extends AbstractCounterTest<IncrementCounterInCacheAdvice> {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { //
                { true, "incrCounter1", new Class[] { int.class }, new Object[] { 1 }}, //
                        { true, "incrCounter2", new Class[] { int.class, int.class }, new Object[] { 2 , 5 }}, //
                        { false, "incrCounter20", new Class[] { }, new Object[] { }}, //

                });
    }
    
    public IncrementCounterInCacheAdviceTest(boolean isValid, String methodName, Class<?>[] paramTypes, Object[] params) {
        super(isValid, methodName, paramTypes, params, null);
    }

    @Before
    public void setUp() {
        super.setUp(new TestService());
    }
    

    @Test
    public void validIncrementCounterInCache() throws Throwable {
        Assume.assumeTrue(isValid);
        
        advice.incrementSingle(pjp);
        
        verify(client, only()).incr(cacheKey, 1, 1L);
    }
    
    @Test
    public void invalidIncrementCounterInCache() throws Throwable {
        Assume.assumeThat(isValid, CoreMatchers.is(false));
        
        advice.incrementSingle(pjp);
        
        verify(client, never()).incr(cacheKey, 1, 1L);
    }
    
    
    @Override
    protected IncrementCounterInCacheAdvice createAdvice() {
        return new IncrementCounterInCacheAdvice();
    }

    
    @SuppressWarnings("unused")
    private static class TestService {
        
        @IncrementCounterInCache(namespace = NS)
        public void incrCounter1(@ParameterValueKeyProvider int id1) {
            
        }
        
        @IncrementCounterInCache(namespace = NS)
        public int incrCounter2(@ParameterValueKeyProvider(order = 2) int id1, @ParameterValueKeyProvider(order = 5) int id2) {
            return 1;
        }
        
        // no @ParameterValueKeyProvider
        @IncrementCounterInCache(namespace = NS)
        public void incrCounter20() {
            
        }
        
    }
    
}
