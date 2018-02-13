/*
 * Copyright (c) 2017-2018 Jakub Białek
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
package com.google.code.ssm.spring;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.Cache;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * @since 4.0.0
 * 
 */
public class SSMCacheNoMuteExceptionsTests {
    
    private SSMCache ssmCache;

    private Cache cache;

    private final int expiration = 60 * 60;

    @Before
    public void setUp() {
        cache = mock(Cache.class);
        ssmCache = new SSMCache(cache, expiration, true, false, false);
        when(cache.isEnabled()).thenReturn(true);
    }

    @Test(expected = WrappedCacheException.class)
    public void clearShouldThrowWrappedCacheException() throws TimeoutException, CacheException {
        doThrow(CacheException.class).when(cache).flush();

        ssmCache.clear();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void clearShouldThrowRuntimeException() throws TimeoutException, CacheException {
        doThrow(IllegalArgumentException.class).when(cache).flush();

        ssmCache.clear();
    }

    @Test(expected = WrappedCacheException.class)
    public void evictShouldThrowWrappedCacheException() throws TimeoutException, CacheException {
        String key = "someKey";
        when(cache.delete(key)).thenThrow(CacheException.class);    

        ssmCache.evict(key);
    }

    @Test(expected = RuntimeException.class)
    public void evictShouldThrowRuntimeException() throws TimeoutException, CacheException {
        String key = "someKey";
        when(cache.delete(key)).thenThrow(RuntimeException.class);    

        ssmCache.evict(key);
    }

    @Test(expected = WrappedCacheException.class)
    public void getShouldThrowWrappedCacheException() throws TimeoutException, CacheException {
        String key = "someCacheKey";
        when(cache.get(key, null)).thenThrow(CacheException.class);
        
        ssmCache.get(key);
    }

    @Test(expected = RuntimeException.class)
    public void getShouldThrowRuntimeException() throws TimeoutException, CacheException {
        String key = "someCacheKey";
        when(cache.get(key, null)).thenThrow(IllegalArgumentException.class);
        
        ssmCache.get(key);
    }
    
    @Test(expected = WrappedCacheException.class)
    public void putShouldThrowWrappedCacheException() throws TimeoutException, CacheException {
        Object key = "cackeKey";
        Object value = new Object();
        doThrow(TimeoutException.class).when(cache).set(key.toString(), expiration, value, null);

        ssmCache.put(key, value);
    }
    
    @Test(expected = RuntimeException.class)
    public void putShouldThrowRuntimeException() throws TimeoutException, CacheException {
        Object key = "cackeKey";
        Object value = new Object();
        doThrow(RuntimeException.class).when(cache).set(key.toString(), expiration, value, null);

        ssmCache.put(key, value);
    }
}
