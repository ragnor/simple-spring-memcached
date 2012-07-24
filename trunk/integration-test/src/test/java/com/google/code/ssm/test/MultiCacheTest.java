/*
 * Copyright (c) 2012 Jakub Białek
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

package com.google.code.ssm.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.code.ssm.test.dao.MultiCacheDAO;

/**
 * 
 * @author Jakub Białek
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class MultiCacheTest {

    @Autowired
    private MultiCacheDAO multiCacheDAO;

    @Test
    public void multiCacheTest() {
        String key = "key1";
        multiCacheDAO.removeFromDefault(key);
        multiCacheDAO.removeFromDedicated(key);

        String value = "value1" + System.currentTimeMillis();
        multiCacheDAO.storeToDefault(key, value);
        assertEquals(value, multiCacheDAO.getFromDefault(key));
        assertEquals(value, multiCacheDAO.getFromDedicated(key));

        String otherValue = "othervalue" + System.currentTimeMillis();
        multiCacheDAO.storeToDedicated(key, otherValue);
        assertEquals(otherValue, multiCacheDAO.getFromDedicated(key));
        // default cache should still keep old previous cached value
        assertEquals(value, multiCacheDAO.getFromDefault(key));
    }

}
