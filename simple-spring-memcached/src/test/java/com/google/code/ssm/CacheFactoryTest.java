/*
 * Copyright (c) 2008-2014 Nelson Carpentier, Jakub Białek
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.code.ssm.aop.CacheBase;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.config.JndiAddressProvider;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheFactoryTest {

    @InjectMocks
    private final CacheFactory factory = new CacheFactory();

    @Mock
    private CacheBase cacheBase;

    @Mock
    private CacheClientFactory cacheClientFactory;

    @Mock
    private CacheClient cacheClient;

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws IOException {
        when(cacheBase.isCacheDisabled()).thenReturn(false);
        when(cacheClientFactory.create(any(List.class), any(CacheConfiguration.class))).thenAnswer(new Answer<CacheClient>() {

            @Override
            public CacheClient answer(InvocationOnMock invocation) throws Throwable {
                List<InetSocketAddress> address = (List<InetSocketAddress>) invocation.getArguments()[0];
                List<SocketAddress> socketAddress = new ArrayList<SocketAddress>(address);
                when(cacheClient.getAvailableServers()).thenReturn(socketAddress);
                return cacheClient;
            }
        });

        factory.setCacheClientFactory(cacheClientFactory);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateClientException() throws IOException, NamingException {
        factory.createCache();
    }

    @Test
    public void shouldCreateClientWhenDefaultAddrProvider() throws Exception {
        CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        factory.setConfiguration(conf);
        factory.setAddressProvider(addrsProvider);
        factory.afterPropertiesSet();

        Cache cache = factory.createCache();

        assertNotNull(cache);
        verify(cacheClientFactory).create(addrsProvider.getAddresses(), conf);
    }

    @Test
    public void shouldCreateClientWhenJndiAddrProvider() throws Exception {
        CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(true);
        AddressProvider addrsProvider = new JndiAddressProvider("memcached/ips", "127.0.0.1:11211");
        factory.setConfiguration(conf);
        factory.setAddressProvider(addrsProvider);
        factory.afterPropertiesSet();

        Cache cache = factory.createCache();

        assertNotNull(cache);
        verify(cacheClientFactory).create(addrsProvider.getAddresses(), conf);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfFactoryInvokedTwice() throws Exception {
        CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        factory.setConfiguration(conf);
        factory.setAddressProvider(addrsProvider);
        factory.afterPropertiesSet();

        factory.createCache();
        factory.createCache();
    }

    @Test
    public void changeAddresses() throws IOException, NamingException {
        final CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        factory.setConfiguration(conf);
        factory.setAddressProvider(addrsProvider);

        Cache cache = factory.createCache();

        List<InetSocketAddress> newAddrs = Arrays.asList(new InetSocketAddress("127.0.0.2", 11221));
        factory.changeAddresses(newAddrs);
        Collection<SocketAddress> c = cache.getAvailableServers();

        assertEquals(1, c.size());
        assertEquals(newAddrs, c);
        verify(cacheClientFactory).create(addrsProvider.getAddresses(), conf);
    }

}
