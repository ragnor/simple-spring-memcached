/*
 * Copyright (c) 2010-2011 Jakub Białek
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

package com.google.code.ssm.test.counter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import com.google.code.ssm.test.svc.TestSvc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * 
 * @author Jakub Białek
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class IncrementCounterInCacheTest {

    @Autowired
    private TestSvc test;

    @Test
    public void test() throws InterruptedException {
        String key = "my-counter-key";
        long initialValue = test.getCounter(key);
        int count = 55;

        for (int i = 0; i < count; i++) {
            test.increment(key);
        }

        Thread.sleep(100);
        assertEquals(count, test.getCounter(key) - initialValue);
        assertEquals(count, test.getCounter(key) - initialValue);

        String key2 = "my-counter-key2";
        long initialValue2 = test.getCounter(key2);
        int count2 = 33;

        for (int i = 0; i < count2; i++) {
            test.increment(key2);
        }

        Thread.sleep(100);
        assertEquals(count2, test.getCounter(key2) - initialValue2);
        assertEquals(count, test.getCounter(key) - initialValue);
        assertEquals(count2, test.getCounter(key2) - initialValue2);

        key = "my-counter-keyxyz";
        for (int i = 0; i < count; i++) {
            test.increment(key);
        }
        assertTrue(test.getCounter(key) % count == 0);
    }

}
