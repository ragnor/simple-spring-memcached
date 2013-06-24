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

package com.google.code.ssm.transcoders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class JavaTranscoderTest {

    private JavaTranscoder transcoder;

    @Test
    public void testtestEncodeAndDecodeWithoutCompression() {
        transcoder = new JavaTranscoder();

        Point p = new Point(40, 50);

        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());

        Point p2 = (Point) transcoder.decode(co);
        assertNotNull(p2);
        assertEquals(p, p2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testtestEncodeAndDecodeWithCompression() {
        transcoder = new JavaTranscoder();
        transcoder.setCompressionThreshold(1);

        List<Point> list = new ArrayList<Point>();
        list.add(new Point(40, 50));
        list.add(new Point(40, 50));
        list.add(new Point(13333, 5655599));

        CachedObject co = transcoder.encode(list);
        assertNotNull(co);
        assertNotNull(co.getData());

        List<Point> list2 = (List<Point>) transcoder.decode(co);
        assertNotNull(list2);
        assertEquals(list, list2);
    }

}
