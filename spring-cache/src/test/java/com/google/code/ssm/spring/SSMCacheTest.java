/*
 * Copyright (c) 2012-2017 Jakub Białek
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.Cache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class SSMCacheTest {

    private SSMCache ssmCache;

    private Cache cache;

    private final int expiration = 60 * 60;

    @Before
    public void setUp() {
        cache = mock(Cache.class);
        ssmCache = new SSMCache(cache, expiration, false);
    }

    @Test(expected = IllegalStateException.class)
    public void clearShouldThrowExceptionWhenNotAllowed() throws TimeoutException, CacheException {
        when(cache.isEnabled()).thenReturn(true);

        ssmCache.clear();
    }

    @Test
    public void clearShouldExecuteWhenCacheEnabled() throws TimeoutException, CacheException {
        ssmCache = new SSMCache(cache, expiration, true);
        when(cache.isEnabled()).thenReturn(true);

        ssmCache.clear();

        verify(cache).flush();
    }

    @Test
    public void evictShouldExecuteWhenCacheEnabled() throws TimeoutException, CacheException {
        String key = "someKey";
        when(cache.isEnabled()).thenReturn(true);

        ssmCache.evict(key);

        verify(cache).delete(key);
    }

    @Test
    public void getShouldExecuteWhenCacheEnabled() throws TimeoutException, CacheException {
        when(cache.isEnabled()).thenReturn(true);
        String key = "someCacheKey";

        ssmCache.get(key);

        verify(cache).get(key, null);
    }

    @Test
    public void getNameShouldExecuteWhenCacheEnabled() {
        when(cache.isEnabled()).thenReturn(true);

        ssmCache.getName();

        verify(cache).getName();
    }

    @Test
    public void getNativeCacheShouldExecuteWhenCacheEnabled() {
        when(cache.isEnabled()).thenReturn(true);

        final Object nativeCache = ssmCache.getNativeCache();

        assertSame(cache, nativeCache);
    }

    @Test
    public void putShouldExecuteWhenCacheEnabled() throws TimeoutException, CacheException {
        Object key = "cackeKey";
        Object value = new Object();
        when(cache.isEnabled()).thenReturn(true);

        ssmCache.put(key, value);

        verify(cache).set(key.toString(), expiration, value, null);
    }

    @Test
    public void clearNotExecutedWhenCacheDisabled() throws TimeoutException, CacheException {
        ssmCache = new SSMCache(cache, expiration, true);
        when(cache.isEnabled()).thenReturn(false);

        ssmCache.clear();

        verify(cache, never()).flush();
    }

    @Test
    public void evictNotExecutedWhenCacheDisabled() throws TimeoutException, CacheException {
        String key = "someKey";
        when(cache.isEnabled()).thenReturn(false);

        ssmCache.evict(key);

        verify(cache, never()).delete(key);
    }

    @Test
    public void getNotExecutedWhenCacheDisabled() throws TimeoutException, CacheException {
        String key = "someCacheKey";
        when(cache.isEnabled()).thenReturn(false);

        final Object result = ssmCache.get(key);

        verify(cache, never()).get(key, null);
        assertNull(result);
    }
    
    @Test
    public void getWithValueLoaderNotExecutedWhenCacheDisabled() throws TimeoutException, CacheException {
        String key = "someCacheKey";
        when(cache.isEnabled()).thenReturn(false);
        final Object expectedResult = new Object();

        final Object result = ssmCache.get(key, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return expectedResult;
            }
        });

        verify(cache, never()).get(anyString(), any(SerializationType.class));
        verify(cache, never()).set(anyString(), anyInt(), any(), any(SerializationType.class));
        verify(cache, never()).setSilently(anyString(), anyInt(), any(), any(SerializationType.class));
        assertEquals(expectedResult, result);
    }

    @Test(expected = SSMCache.ValueRetrievalException.class)
    public void getWithValueLoaderShouldWrapException() throws TimeoutException, CacheException {
        String key = "someCacheKey";
        when(cache.isEnabled()).thenReturn(false);
       
        ssmCache.get(key, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw new Exception("some exception");
            }
        });
    }
    
    @Test
    public void getNameExecutedWhenCacheDisabled() {
        when(cache.isEnabled()).thenReturn(false);

        ssmCache.getName();

        verify(cache).getName();
    }

    @Test
    public void getNativeCacheExecutedWhenCacheDisabled() {
        when(cache.isEnabled()).thenReturn(false);

        final Object nativeCache = ssmCache.getNativeCache();

        assertSame(cache, nativeCache);
    }

    @Test
    public void putNotExecutedWhenCacheDisabled() throws TimeoutException, CacheException {
        Object key = "cackeKey";
        Object value = new Object();
        when(cache.isEnabled()).thenReturn(false);

        ssmCache.put(key, value);

        verify(cache, never()).set(key.toString(), expiration, value, null);
    }
}
