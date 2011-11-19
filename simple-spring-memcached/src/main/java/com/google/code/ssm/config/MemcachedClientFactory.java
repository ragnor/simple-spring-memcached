package com.google.code.ssm.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.util.jndi.JNDIChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
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
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
public class MemcachedClientFactory implements JNDIChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedClientFactory.class);

    private JndiTemplate jndiTemplate = new JndiTemplate();

    private MemcachedConnectionBean bean;

    private MemcachedClientWrapper memcachedClientWrapper;

    private String currentAddrs;

    private CacheClientFactory memcacheClientFactory;

    public void setBean(MemcachedConnectionBean bean) {
        this.bean = bean;
    }

    public void setClientFactory(CacheClientFactory memcacheClientFactory) {
        this.memcacheClientFactory = memcacheClientFactory;
    }

    /**
     * Only one memcached client is created.
     * 
     * @return memcached client
     * @throws IOException
     * @throws NamingException
     */
    public CacheClient createMemcachedClient() throws IOException, NamingException {
        // this factory creates only one single memcached client and return it if someone invoked this method twice or
        // more
        if (memcachedClientWrapper != null) {
            throw new IllegalStateException("This factory has already created memcached client");
        }

        if (this.bean == null) {
            throw new RuntimeException("The MemcachedConnectionBean must be defined!");
        }

        Object ips = null;
        List<InetSocketAddress> addrs = null;
        try {
            if (bean.getJndiKey() != null && (ips = jndiTemplate.lookup(bean.getJndiKey())) != null) {
                LOGGER.info("IPs from JNDI will be used to connect to memcached servers. IPs: " + ips);
                currentAddrs = (String) ips;
                addrs = getAddresses((String) ips);
            } else {
                addrs = getAddrsFromProperty();
            }
        } catch (NamingException ex) {
            LOGGER.warn("Name of the JNDI key with memcached IPs is set but no value is bound to this key: " + bean.getJndiKey(), ex);
            addrs = getAddrsFromProperty();
        }
        memcachedClientWrapper = new MemcachedClientWrapper(createClient(addrs));

        return memcachedClientWrapper;
    }

    private List<InetSocketAddress> getAddrsFromProperty() {
        LOGGER.warn("JNDI doesn't contain IPs of memcached servers. Fallback IPs from xml will be used: " + this.bean.getNodeList());
        currentAddrs = this.bean.getNodeList();
        return getAddresses(this.bean.getNodeList());
    }

    @Override
    public void handleNotification(String jndiKey, Object newValue) {
        if (!jndiKey.equals(bean.getJndiKey())) {
            return;
        }

        if (newValue == null || newValue.equals(currentAddrs)) {
            LOGGER.debug("New value of memcached/ips JNDI won't be set because it is null or the same as previous: " + newValue);
            return;
        }

        CacheClient memcacheClient = null;
        try {
            LOGGER.info("Creating new memcached client for new jndi value: " + newValue);
            memcacheClient = createClient(getAddresses((String) newValue));
            LOGGER.info("New memcached client created with ips: " + newValue);
            memcachedClientWrapper.changeMemcachedClient(memcacheClient);
            currentAddrs = (String) newValue;
        } catch (IOException e) {
            LOGGER.error("Cannot change memcached client to new one with IPs " + newValue, e);
        }
    }

    /**
     * Split a string containing whitespace or comma separated host or IP addresses and port numbers of the form
     * "host:port host2:port" or "host:port, host2:port" into a List of InetSocketAddress instances suitable for
     * instantiating a MemcachedClient.
     * 
     * Note that colon-delimited IPv6 is also supported. For example: ::1:11211
     */
    private static List<InetSocketAddress> getAddresses(String s) {
        if (s == null) {
            throw new NullPointerException("Null host list");
        }
        if (s.trim().equals("")) {
            throw new IllegalArgumentException("No hosts in list:  ``" + s + "''");
        }
        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();

        for (String hoststuff : s.split("(?:\\s|,)+")) {
            if (hoststuff.equals("")) {
                continue;
            }

            int finalColon = hoststuff.lastIndexOf(':');
            if (finalColon < 1) {
                throw new IllegalArgumentException("Invalid server ``" + hoststuff + "'' in list:  " + s);

            }
            String hostPart = hoststuff.substring(0, finalColon);
            String portNum = hoststuff.substring(finalColon + 1);

            addrs.add(new InetSocketAddress(hostPart, Integer.parseInt(portNum)));
        }
        return addrs;
    }

    private CacheClient createClient(List<InetSocketAddress> addrs) throws IOException {
        return memcacheClientFactory.create(addrs, bean);
    }

}
