package com.google.code.ssm.util.jndi;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * Notify about changes on given JNDI key. Method {@link #check} should be invoked periodically by scheduler.
 * 
 * @author Jakub Białek
 * 
 */
public class JNDIChangeNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(JNDIChangeNotifier.class);

    private String key;

    private JNDIChangeListener changeListener;

    private Object value;

    private JndiTemplate jndiTemplate = new JndiTemplate();

    @PostConstruct
    protected void init() {
        try {
            value = jndiTemplate.lookup(key);
        } catch (NamingException e) {
            LOGGER.warn(String.format("Error accessing JNDI key %s", key), e);
        }
    }

    public void setJndiKey(String jndiKey) {
        this.key = jndiKey;
    }

    public String getJndiKey() {
        return key;
    }

    public void setChangeListener(JNDIChangeListener jndiChangeNotifier) {
        this.changeListener = jndiChangeNotifier;
    }

    public JNDIChangeListener getChangeListener() {
        return changeListener;
    }

    /**
     * Check if current value bounded to JNDI key is different than previous, if yes then invoked
     * {@link JNDIChangeListener#handleNotification(String, Object)} method.
     */
    public void check() {
        try {
            Object newValue = jndiTemplate.lookup(key);
            if (newValue != null && !newValue.equals(value) || newValue != value) {

                if (changeListener != null) {
                    value = newValue;
                    changeListener.handleNotification(key, value);
                } else {
                    LOGGER.error("Change listener is null for JNDI key {}, cannot notify about new value {}", key, newValue);
                }

            }
        } catch (NamingException e) {
            LOGGER.warn(String.format("Error checking JNDI key %s", key), e);
        }
    }

}
