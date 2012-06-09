/*
 * Copyright (c) 2010-2012 Jakub Białek
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

package com.google.code.ssm.util.jndi;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.code.ssm.config.AddressChangeListener;
import com.google.code.ssm.config.AddressChangeNotifier;
import com.google.code.ssm.config.JndiAddressProvider;

/**
 * 
 * Notify about changes on given JNDI key. Method {@link #check} should be invoked periodically by scheduler (i.e.
 * quartz).
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class JndiChangeNotifier extends JndiAddressProvider implements AddressChangeNotifier, InitializingBean { // NO_UCD

    private static final Logger LOGGER = LoggerFactory.getLogger(JndiChangeNotifier.class);

    private AddressChangeListener addressChangeListener;

    private List<InetSocketAddress> currentAddrs;

    @Override
    public void afterPropertiesSet() throws Exception {
        currentAddrs = getAddresses();
    }

    @Override
    public void setAddressChangeListener(final AddressChangeListener addressChangeListener) {
        this.addressChangeListener = addressChangeListener;
    }

    public AddressChangeListener getAddressChangeListener() {
        return addressChangeListener;
    }

    /**
     * Check if current value bounded to JNDI key is different than previous, if yes then invoked
     * {@link AddressChangeListener#changeAddresses(List)} method.
     */
    public void check() {
        List<InetSocketAddress> newAddrs = getAddresses();
        if (newAddrs != null && !newAddrs.equals(currentAddrs)) {

            if (addressChangeListener != null) {
                currentAddrs = newAddrs;
                addressChangeListener.changeAddresses(newAddrs);
            } else {
                LOGGER.error("Address change listener is null for JNDI key {}, cannot notify about new value {}", getJndiKey(), newAddrs);
            }
        }
    }

}
