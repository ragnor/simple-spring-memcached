/* Copyright (c) 2014-2019 Jakub Białek
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

package com.google.code.ssm;

import org.junit.Before;
import org.mockito.Mockito;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.transcoders.JavaTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoder;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class PrefixedCacheImplTest extends CacheImplTest {

    private final String namePrefix = "samplePrefix";
    
    private final String prefix = "#";
    
    private Cache wrappedCache;
    
    @Before
    @Override
    public void setUp() {
        cacheClient = Mockito.mock(CacheClient.class);
        jsonTranscoder = Mockito.mock(JsonTranscoder.class);
        javaTranscoder = Mockito.mock(JavaTranscoder.class);
        wrappedCache = new CacheImpl(super.name, aliases, cacheClient, defaultSerializationType, jsonTranscoder, javaTranscoder, null,
                new CacheProperties());
        cache = new PrefixedCacheImpl(wrappedCache, namePrefix, prefix);
    }
    
    @Override
    protected String getKey(String key) {
        return namePrefix + prefix + key;
    }
    
}
