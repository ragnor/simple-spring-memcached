package com.google.code.ssm.test.counter;

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
 * Copyright (c) 2010, 2011 Jakub Białek
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
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class UpdateCounterInCacheTest {

    @Autowired
    private TestSvc test;

    @Test
    public void test() throws InterruptedException {        
        String key = "my2-counter-key";
        test.increment(key);
        test.getCounter(key);
        long value = 777L;
        test.update(key, value);
        Thread.sleep(100);
        assertEquals(value, test.getCounter(key));
        assertEquals(value, test.getCounter(key));
        assertEquals(value, test.getCounter(key));

        test.increment(key);
        test.increment(key);
        test.increment(key);
        assertEquals((long) value + 3, (long) test.getCounter(key));

        String key2 = "my3-counter-key2";
        value = 123L;
        test.update(key2, value);
        test.increment(key2);
        test.getCounter(key2);
        Thread.sleep(100);
        assertEquals((long) value + 1, (long) test.getCounter(key2));
        assertEquals((long) value + 1, (long) test.getCounter(key2));
        assertEquals((long) value + 1, (long) test.getCounter(key2));

        String key3 = "my3-counter-key3";
        value = 333L;
        test.increment(key3);
        test.getCounter(key3);
        Thread.sleep(100);
        test.update(key3, value);
        assertEquals(value, test.getCounter(key3));
        assertEquals(value, test.getCounter(key3));
        assertEquals(value, test.getCounter(key3));
    }

}
