/*
 * Copyright (c) 2014-2017 Jakub Białek
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Matchers.eq;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.code.ssm.aop.CacheBase;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheFactoryDisabledTest {

    @InjectMocks
    private final CacheFactory factory = new CacheFactory();

    @Mock
    private CacheBase cacheBase;

    @Mock
    private CacheClientFactory cacheClientFactory;

    @Mock
    private CacheClient cacheClient;

    private Cache cache;

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        when(cacheBase.isCacheDisabled()).thenReturn(true);
        final CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        factory.setConfiguration(conf);
        factory.setAddressProvider(addrsProvider);
        factory.setCacheClientFactory(cacheClientFactory);
        factory.afterPropertiesSet();

        cache = factory.createCache();
        assertNotNull(cache);
        verify(cacheClientFactory, never()).create(any(List.class), eq(conf));
    }

    @Test
    public void getNameShouldBeAllowed() {
        cache.getName();
    }

    @Test
    public void getAliasesShouldBeAllowed() {
        cache.getAliases();
    }

    @Test
    public void shutdownShouldBeAllowed() {
        cache.shutdown();
    }

    @Test
    public void isEnabledShouldBeAllowed() {
        cache.isEnabled();
    }

    @Test(expected = IllegalStateException.class)
    public void incrShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.incr("key1", 1, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void decrShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.decr("key1", 1);
    }

    @Test(expected = IllegalStateException.class)
    public void getCounterShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.getCounter("key1");
    }

    @Test(expected = IllegalStateException.class)
    public void addShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.add("key1", 1, "value1", null);
    }

    @Test(expected = IllegalStateException.class)
    public void getShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.get("key1", null);
    }

    @Test(expected = IllegalStateException.class)
    public void deleteShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.delete("key1");
    }

    @Test(expected = IllegalStateException.class)
    public void setShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.set("key1", 100, "value1", null);
    }

    @Test(expected = IllegalStateException.class)
    public void flushShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.flush();
    }

    @Test(expected = IllegalStateException.class)
    public void getAvailableServersShouldNotBeAllowed() throws TimeoutException, CacheException {
        cache.getAvailableServers();
    }

}
