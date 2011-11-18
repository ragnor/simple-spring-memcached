package com.google.code.ssm.test;

import static org.junit.Assert.*;
import com.google.code.ssm.test.svc.TestSvc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


/**
Copyright (c) 2008, 2009  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class InvalidateSingleCacheTest {
	@Autowired
	private TestSvc test;

    @Test
    public void test() {
        final Long key1 = System.currentTimeMillis();
        final Long key2 = System.currentTimeMillis() + 10000;

        //final TestSvc test = (TestSvc) context.getBean("testSvc");

        final String f1 = test.getRandomString(key1);
        final String f2 = test.getRandomString(key2);
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));

        test.updateRandomString(key1);
        test.updateRandomString(key2);

        final String s1 = test.getRandomString(key1);
        final String s2 = test.getRandomString(key2);

        assertFalse(f1.equals(s1));
        assertFalse(f2.equals(s2));

        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));
        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));
        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));

        test.updateRandomStringAgain(key1);
        test.updateRandomStringAgain(key2);

        final String t1 = test.getRandomString(key1);
        final String t2 = test.getRandomString(key2);

        assertFalse(s1.equals(t1));
        assertFalse(s2.equals(t2));

        assertEquals(t1, test.getRandomString(key1));
        assertEquals(t2, test.getRandomString(key2));        
    }
}
