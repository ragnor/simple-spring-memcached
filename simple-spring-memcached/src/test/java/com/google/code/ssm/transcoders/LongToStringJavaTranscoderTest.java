/* Copyright (c) 2012-2019 Jakub Białek
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

package com.google.code.ssm.transcoders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class LongToStringJavaTranscoderTest {

    private LongToStringTranscoder transcoder;

    @Test
    public void testEncodeAndDecode() {
        transcoder = new LongToStringTranscoder();

        Long v = 5699L;

        CachedObject co = transcoder.encode(v);
        assertNotNull(co);
        assertNotNull(co.getData());

        Long v2 = (Long) transcoder.decode(co);
        assertNotNull(v2);
        assertEquals(v, v2);

        long vv = 5588;
        co = transcoder.encode(vv);
        assertNotNull(co);
        assertNotNull(co.getData());

        Long vv2 = (Long) transcoder.decode(co);
        assertNotNull(vv2);
        assertEquals(vv, vv2.longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void notLong() {
        transcoder = new LongToStringTranscoder();

        transcoder.encode(new Point(3, 7));
    }

}
