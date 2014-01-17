/*
 * Copyright (c) 2012-2014 Jakub Białek
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheProperties;

/**
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class ExtendedSSMCacheManagerTest {

    private ExtendedSSMCacheManager ssmCacheManager;

    private Set<SSMCache> caches;

    private Cache cache1;

    private Cache cache2;

    @Before
    public void setUp() {
        cache1 = Mockito.mock(Cache.class);
        cache2 = Mockito.mock(Cache.class);

        Mockito.when(cache1.getName()).thenReturn("cache1");
        Mockito.when(cache2.getName()).thenReturn("cache2");
        Mockito.when(cache1.getProperties()).thenReturn(new CacheProperties());
        Mockito.when(cache2.getProperties()).thenReturn(new CacheProperties());

        caches = new HashSet<SSMCache>(Arrays.asList(new SSMCache(cache1, 60, false), new SSMCache(cache2, 60, false)));

        ssmCacheManager = new ExtendedSSMCacheManager();
        ssmCacheManager.setCaches(caches);

        ssmCacheManager.afterPropertiesSet();
    }

    @Test
    public void getCacheWithoutExpiration() {
        org.springframework.cache.Cache cache = ssmCacheManager.getCache("cache2");
        assertNotNull(cache);
        assertSame(cache2, cache.getNativeCache());
        assertEquals(60, ((SSMCache) cache).getExpiration());
    }

    @Test
    public void getCacheWithExpiration() {
        org.springframework.cache.Cache cache = ssmCacheManager.getCache("cache2#120");
        assertNotNull(cache);
        assertSame(cache2, cache.getNativeCache());
        assertEquals(120, ((SSMCache) cache).getExpiration());

        cache = ssmCacheManager.getCache("cache1#300");
        assertNotNull(cache);
        assertSame(cache1, cache.getNativeCache());
        assertEquals(300, ((SSMCache) cache).getExpiration());
    }

    @Test
    public void getCacheWithInvalidExpiration() {
        org.springframework.cache.Cache cache = ssmCacheManager.getCache("cache2#abc");
        assertNotNull(cache);
        assertSame(cache2, cache.getNativeCache());
        // use default expiration
        assertEquals(60, ((SSMCache) cache).getExpiration());

        cache = ssmCacheManager.getCache("cache2#-60");
        assertNotNull(cache);
        assertSame(cache2, cache.getNativeCache());
        // use default expiration
        assertEquals(60, ((SSMCache) cache).getExpiration());
    }

}
