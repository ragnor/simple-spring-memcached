/* Copyright (c) 2010-2012 Jakub Białek
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


package com.google.code.ssm.providers.xmemcached;

import static org.junit.Assert.assertEquals;
import net.rubyeye.xmemcached.transcoders.CachedData;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class CachedObjectWrapperTest {

    private CachedObjectWrapper cachedObjectWrapper;

    private CachedData cachedData;

    private int flags = 7;

    private byte[] data = new byte[] { 1, 1, 1, 1, 0, 0 };

    @Before
    public void setUp() {
        cachedData = new CachedData(flags, data);
        cachedObjectWrapper = new CachedObjectWrapper(cachedData);
    }

    @Test
    public void getData() {
        assertEquals(data, cachedObjectWrapper.getData());
    }

    @Test
    public void getFlags() {
        assertEquals(flags, cachedObjectWrapper.getFlags());
    }
}
