/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.config.JndiAddressProvider;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class MemcachedClientFactoryTest {

    @Test
    public void testCreateClientException() throws IOException, NamingException {
        final CacheFactory factory = new CacheFactory();
        try {
            factory.createCache();
            fail("Expected Exception.");
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateClient() throws IOException, NamingException {
        CacheConfiguration bean = new CacheConfiguration();
        bean.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        CacheFactory factory = new CacheFactory();
        factory.setConfiguration(bean);
        factory.setAddressProvider(addrsProvider);
        CacheClientFactory clientFactory = getClientFactoryMock(bean);
        factory.setCacheClientFactory(clientFactory);

        Cache cache = factory.createCache();

        assertNotNull(cache);
        EasyMock.verify(clientFactory);

        factory = new CacheFactory();
        factory.setConfiguration(bean);
        factory.setAddressProvider(addrsProvider);
        bean.setConsistentHashing(true);
        clientFactory = getClientFactoryMock(bean);

        factory.setCacheClientFactory(clientFactory);

        cache = factory.createCache();
        assertNotNull(cache);
        EasyMock.verify(clientFactory);

        bean = new CacheConfiguration();
        bean.setConsistentHashing(false);
        addrsProvider = new JndiAddressProvider("memcached/ips", "127.0.0.1:11211");
        factory = new CacheFactory();
        factory.setConfiguration(bean);
        factory.setAddressProvider(addrsProvider);
        clientFactory = getClientFactoryMock(bean);
        factory.setCacheClientFactory(clientFactory);

        cache = factory.createCache();

        assertNotNull(cache);
        EasyMock.verify(clientFactory);
        try {
            clientFactory = getClientFactoryMock(bean);
            factory.setCacheClientFactory(clientFactory);
            factory.createCache();
            fail();
        } catch (IllegalStateException ex) {
            // ok
        }

    }

    public void changeAddresses() throws IOException, NamingException {
        final CacheConfiguration bean = new CacheConfiguration();
        bean.setConsistentHashing(false);
        AddressProvider addrsProvider = new DefaultAddressProvider("127.0.0.1:11211");
        final CacheFactory factory = new CacheFactory();
        factory.setConfiguration(bean);
        factory.setAddressProvider(addrsProvider);
        CacheClientFactory clientFactory = getClientFactoryMock(bean);
        factory.setCacheClientFactory(clientFactory);

        Cache cache = factory.createCache();

        List<InetSocketAddress> newAddrs = Arrays.asList(new InetSocketAddress("127.0.0.2", 11221));
        factory.changeAddresses(newAddrs);
        Collection<SocketAddress> c = cache.getAvailableServers();

        assertEquals(1, c.size());
        assertEquals(newAddrs, c);
    }

    @SuppressWarnings("unchecked")
    private CacheClientFactory getClientFactoryMock(final CacheConfiguration bean) throws IOException {
        CacheClientFactory clientFactory = EasyMock.createMock(CacheClientFactory.class);

        EasyMock.expect(clientFactory.create(EasyMock.anyObject(List.class), EasyMock.eq(bean))).andAnswer(new IAnswer<CacheClient>() {

            @Override
            public CacheClient answer() throws Throwable {
                List<InetSocketAddress> address = (List<InetSocketAddress>) EasyMock.getCurrentArguments()[0];
                CacheClient client = EasyMock.createMock(CacheClient.class);

                List<SocketAddress> socketAddress = new ArrayList<SocketAddress>();
                socketAddress.addAll(address);
                EasyMock.expect(client.getAvailableServers()).andReturn(socketAddress);
                EasyMock.replay(client);

                return client;
            }
        });
        EasyMock.replay(clientFactory);

        return clientFactory;
    }
}
