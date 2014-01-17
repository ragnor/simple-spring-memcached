/* Copyright (c) 2012-2014 Jakub Białek
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

package com.google.code.ssm.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.List;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class JndiAddressProviderTest {

    private final String jndiKey = "someKey";

    private JndiAddressProvider jndiAdressProvider;

    @Before
    public void setUp() {
        jndiAdressProvider = new JndiAddressProvider();
        jndiAdressProvider.setAddress("127.0.0.1:11211");
        jndiAdressProvider.setJndiKey(jndiKey);
    }

    @Test
    public void getAddresses() throws NamingException {
        JndiAddressProvider spy = Mockito.spy(jndiAdressProvider);
        Mockito.doReturn("127.0.0.1:11311").when(spy).lookup(jndiKey);

        List<InetSocketAddress> result = spy.getAddresses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("127.0.0.1", result.get(0).getAddress().getHostAddress());
        assertEquals(11311, result.get(0).getPort());

    }

    @Test
    public void getAddressesNullKey() {
        jndiAdressProvider.setJndiKey(null);
        List<InetSocketAddress> result = jndiAdressProvider.getAddresses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("127.0.0.1", result.get(0).getAddress().getHostAddress());
        assertEquals(11211, result.get(0).getPort());

        jndiAdressProvider = new JndiAddressProvider(null, "127.0.0.1:11211");
        result = jndiAdressProvider.getAddresses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("127.0.0.1", result.get(0).getAddress().getHostAddress());
        assertEquals(11211, result.get(0).getPort());
    }

    @Test
    public void getAddressesNullLookup() throws NamingException {
        JndiAddressProvider spy = Mockito.spy(jndiAdressProvider);
        Mockito.doReturn(null).when(spy).lookup(jndiKey);

        List<InetSocketAddress> result = spy.getAddresses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("127.0.0.1", result.get(0).getAddress().getHostAddress());
        assertEquals(11211, result.get(0).getPort());
    }

    @Test
    public void getAddressesNamingExcpetion() throws NamingException {
        JndiAddressProvider spy = Mockito.spy(jndiAdressProvider);
        Mockito.doThrow(NamingException.class).when(spy).lookup(jndiKey);

        List<InetSocketAddress> result = spy.getAddresses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("127.0.0.1", result.get(0).getAddress().getHostAddress());
        assertEquals(11211, result.get(0).getPort());
    }

}
