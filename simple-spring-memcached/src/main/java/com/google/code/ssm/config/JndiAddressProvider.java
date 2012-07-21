/*
 * Copyright (c) 2012 Jakub Białek
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
import java.util.List;

import javax.naming.NamingException;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.Assert;

/**
 * Gets servers' addresses from JNDI defined under key {@link #getJndiKey()}. If value under given JNDI key is not
 * defined or cannot be converted then value defined by {@link JndiAddressProvider#getAddress()} is used. The correct
 * value binded to JNDI key should be of type {@link String} with comma or whitespace separated list of servers'
 * addresses.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class JndiAddressProvider extends DefaultAddressProvider implements InitializingBean {

    private final JndiTemplate jndiTemplate = new JndiTemplate();

    @Getter
    @Setter
    private String jndiKey;

    public JndiAddressProvider() {

    }

    public JndiAddressProvider(final String jndiKey, final String address) { // NO_UCD
        super(address);
        this.jndiKey = jndiKey;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(jndiKey, "'jndiKey' is required and cannot be null");
    }

    @Override
    public List<InetSocketAddress> getAddresses() {
        Object ips = null;
        List<InetSocketAddress> addrs = null;
        try {
            if (jndiKey != null && (ips = lookup(jndiKey)) != null) {
                logger.info("Addresses from JNDI will be used to connect to memcached servers. Addresses: {}", ips);
                addrs = getAddresses((String) ips);
            } else {
                addrs = super.getAddresses();
            }
        } catch (NamingException ex) {
            logger.warn(
                    String.format("Name of the JNDI key with memcached addresses is set but wrong value is bound to this key: %s", jndiKey),
                    ex);
            addrs = super.getAddresses();
        }

        return addrs;
    }

    protected Object lookup(final String key) throws NamingException {
        return jndiTemplate.lookup(key);
    }

}
