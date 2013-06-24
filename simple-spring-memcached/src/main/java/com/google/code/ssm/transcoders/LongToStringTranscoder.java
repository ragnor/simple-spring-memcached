/*
 * Copyright (c) 2010-2013 Jakub Białek
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

import java.io.UnsupportedEncodingException;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;

/**
 * 
 * Converts Long to simple String (byte) representation. Should by used in set operation to correct initialize counter
 * value.
 * 
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@ToString
@EqualsAndHashCode
public class LongToStringTranscoder implements CacheTranscoder {

    @Override
    public Object decode(final CachedObject data) {
        byte[] value = data.getData();
        if (value == null || value.length == 0) {
            return null;
        }

        try {
            return Long.parseLong(new String(value, "UTF-8").trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CachedObject encode(final Object o) {
        if (!(o instanceof Long)) {
            throw new IllegalArgumentException("Only Long objects are supported by this transcoder");
        }

        try {
            return new CachedObjectImpl(0, String.valueOf(o).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
