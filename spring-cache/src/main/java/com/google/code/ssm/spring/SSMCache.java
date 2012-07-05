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
package com.google.code.ssm.spring;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class SSMCache implements Cache {

    private final static Logger LOGGER = LoggerFactory.getLogger(SSMCache.class);

    private final com.google.code.ssm.Cache cache;

    private final int expiration;

    private final boolean allowClear;

    public SSMCache(final com.google.code.ssm.Cache cache, final int expiration, final boolean allowClear) {
        this.cache = cache;
        this.expiration = expiration;
        this.allowClear = allowClear;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(final Object key) {
        Object value = null;
        try {
            value = cache.get(getKey(key), null);
        } catch (TimeoutException e) {
            LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
        } catch (CacheException e) {
            LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
        }

        if (value == null) {
            return null;
        }

        return value instanceof PertinentNegativeNull ? new SimpleValueWrapper(null) : new SimpleValueWrapper(value);
    }

    @Override
    public void put(final Object key, final Object value) {
        if (key != null) {
            try {
                Object store = value;
                if (value == null) {
                    store = PertinentNegativeNull.NULL;
                }

                cache.set(getKey(key), expiration, store, null);
            } catch (TimeoutException e) {
                LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
            } catch (CacheException e) {
                LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
            }
        }
    }

    @Override
    public void evict(final Object key) {
        if (key != null) {
            try {
                cache.delete(getKey(key));
            } catch (TimeoutException e) {
                LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
            } catch (CacheException e) {
                LOGGER.warn("An error has ocurred for cache " + getName() + " and key " + getKey(key), e);
            }
        }
    }

    @Override
    public void clear() {
        if (!allowClear) {
            LOGGER.error("Clearing cache '" + getName() + "' is not allowed. To enable it set allowClear to true. "
                    + "Make sure that caches don't overlap (one memcached instance isn't used by more than one cache) "
                    + "otherwise clearing one cache will affect another.");
            throw new IllegalStateException("Cannot clear cache " + getName());
        }
        try {
            cache.flush();
        } catch (TimeoutException e) {
            LOGGER.warn("An error has ocurred for cache " + getName(), e);
        } catch (CacheException e) {
            LOGGER.warn("An error has ocurred for cache " + getName(), e);
        }
    }

    private String getKey(final Object key) {
        return key.toString();
    }

}
