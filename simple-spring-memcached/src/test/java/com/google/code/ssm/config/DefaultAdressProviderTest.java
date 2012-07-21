/* Copyright (c) 2012 Jakub Białek
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
import static org.junit.Assert.assertNull;

import java.net.InetSocketAddress;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class DefaultAdressProviderTest {

    private DefaultAddressProvider defaultAddressProvider;

    @Before
    public void setUp() {
        defaultAddressProvider = new DefaultAddressProvider();
    }

    @Test
    public void getAddressesSingle() {
        String address = "127.0.0.1:11211";
        defaultAddressProvider.setAddress(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        List<InetSocketAddress> list = defaultAddressProvider.getAddresses();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("127.0.0.1", list.get(0).getHostName());
        assertEquals(11211, list.get(0).getPort());

        defaultAddressProvider = new DefaultAddressProvider(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        list = defaultAddressProvider.getAddresses();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("127.0.0.1", list.get(0).getHostName());
        assertEquals(11211, list.get(0).getPort());
    }

    @Test
    public void getAddressesMulti() {
        String address = "127.0.0.1:11211, 127.0.0.1:11311";
        defaultAddressProvider.setAddress(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        List<InetSocketAddress> list = defaultAddressProvider.getAddresses();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("127.0.0.1", list.get(0).getHostName());
        assertEquals(11211, list.get(0).getPort());
        assertEquals("127.0.0.1", list.get(1).getHostName());
        assertEquals(11311, list.get(1).getPort());
    }

    @Test(expected = NullPointerException.class)
    public void getAddressesNull() {
        String address = null;
        defaultAddressProvider.setAddress(address);

        assertNull(defaultAddressProvider.getAddress());

        defaultAddressProvider.getAddresses();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAddressesEmtpy() {
        String address = "";
        defaultAddressProvider.setAddress(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        defaultAddressProvider.getAddresses();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAddressesOnlyWhitespaces() {
        String address = "";
        defaultAddressProvider.setAddress(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        defaultAddressProvider.getAddresses();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAddressesNoPort() {
        String address = "127.0.0.1";
        defaultAddressProvider.setAddress(address);

        assertEquals(address, defaultAddressProvider.getAddress());

        defaultAddressProvider.getAddresses();
    }

}
