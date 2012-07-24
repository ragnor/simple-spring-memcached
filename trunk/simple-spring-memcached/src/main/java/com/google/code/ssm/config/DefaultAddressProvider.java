/*
 * Copyright (c) 2012 Nelson Carpentier, Jakub Białek
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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Białek
 * @author Nelson Carpentier
 * @since 2.0.0
 * 
 */
public class DefaultAddressProvider implements AddressProvider {

    private String address;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultAddressProvider() {

    }

    /**
     * 
     * @param address
     *            comma or whitespace separated list of servers' addresses
     */
    public DefaultAddressProvider(final String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    /**
     * Comma or whitespace separated list of servers' addresses.
     * <p>
     * Examples:
     * 
     * <pre>
     * setAddress(&quot;10.0.2.1:11211&quot;);
     * setAddress(&quot;myhost.com:11210,10.0.2.1:11211&quot;);
     * </pre>
     * 
     * @param addresses
     *            servers' addresses
     */
    public void setAddress(final String addresses) {
        this.address = addresses;
    }

    @Override
    public List<InetSocketAddress> getAddresses() {
        logger.info(String.format("Defined values %s will be used as memcached addresses", getAddress()));
        return getAddresses(address);
    }

    /**
     * Split a string containing whitespace or comma separated host or IP addresses and port numbers of the form
     * "host:port host2:port" or "host:port, host2:port" into a List of InetSocketAddress instances suitable for
     * instantiating a MemcachedClient.
     * 
     * Note that colon-delimited IPv6 is also supported. For example: ::1:11211
     */
    protected List<InetSocketAddress> getAddresses(final String s) {
        if (s == null) {
            throw new NullPointerException("Null host list");
        }
        if (s.trim().isEmpty()) {
            throw new IllegalArgumentException("No hosts in list: '" + s + "'");
        }

        List<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        for (String hoststuff : s.split("(?:\\s|,)+")) {
            if (hoststuff.equals("")) {
                continue;
            }

            int finalColon = hoststuff.lastIndexOf(':');
            if (finalColon < 1) {
                throw new IllegalArgumentException("Invalid server '" + hoststuff + "' in list:  " + s);

            }
            String hostPart = hoststuff.substring(0, finalColon);
            String portNum = hoststuff.substring(finalColon + 1);

            addrs.add(new InetSocketAddress(hostPart, Integer.parseInt(portNum)));
        }
        return addrs;
    }

}
