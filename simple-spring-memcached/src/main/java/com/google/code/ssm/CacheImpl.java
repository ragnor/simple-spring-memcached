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

package com.google.code.ssm;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.transcoders.LongToStringTranscoder;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
class CacheImpl implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheImpl.class);

    private final String name;

    private final Collection<String> aliases;

    private volatile CacheClient cacheClient;

    private final Map<Class<?>, CacheTranscoder<?>> transcoders = new HashMap<Class<?>, CacheTranscoder<?>>();

    CacheImpl(final String name, final Collection<String> aliases, final CacheClient cacheClient,
            final Map<Class<?>, CacheTranscoder<?>> transcoders) {
        Assert.hasText(name, "'name' must not be null, empty, or blank");
        Assert.notNull(aliases, "'aliases' cannot be null");
        Assert.notNull(cacheClient, "'cacheClient' cannot be null");

        this.name = name;
        this.aliases = aliases;
        this.cacheClient = cacheClient;
        this.transcoders.putAll(transcoders);
        if (this.transcoders.put(Long.class, new LongToStringTranscoder()) != null) {
            LOGGER.warn("In cache '" + name + "' defined transoder for Long class was overwrited. It may cause errors.");
        }
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        return cacheClient.getAvailableServers();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<String> getAliases() {
        return aliases;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final String cacheKey, final Class<T> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = getTranscoder(clazz);
            if (transcoder != null) {
                return (T) cacheClient.get(cacheKey, transcoder);
            } else {
                LOGGER.warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key "
                        + cacheKey);
            }
        }

        return (T) cacheClient.get(cacheKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void set(final String cacheKey, final int expiration, final Object value, final Class<?> clazz) throws TimeoutException,
            CacheException {
        if (clazz != null) {
            CacheTranscoder<?> transcoder = getTranscoder(clazz);
            if (transcoder != null) {
                cacheClient.set(cacheKey, expiration, (T) value, (CacheTranscoder<T>) transcoder);
                return;
            } else {
                LOGGER.warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key "
                        + cacheKey);
            }
        }

        cacheClient.set(cacheKey, expiration, value);
    }

    @Override
    public <T> void setSilently(final String cacheKey, final int expiration, final Object value, final Class<T> clazz) {
        try {
            set(cacheKey, expiration, value, clazz);
        } catch (TimeoutException e) {
            LOGGER.warn("Cannot set on key " + cacheKey, e);
        } catch (CacheException e) {
            LOGGER.warn("Cannot set on key " + cacheKey, e);
        }
    }

    @Override
    public <T> void add(final String cacheKey, final int expiration, final Object value, final Class<T> clazz) throws TimeoutException,
            CacheException {
        if (clazz != null) {
            CacheTranscoder<Object> transcoder = getTranscoder(clazz);
            if (transcoder != null) {
                cacheClient.add(cacheKey, expiration, value, transcoder);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Add nullValue under key: " + cacheKey + ", json: " + clazz);
                }
                return;
            } else {
                LOGGER.warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for key "
                        + cacheKey);
            }
        }

        cacheClient.add(cacheKey, expiration, value);
    }

    @Override
    public <T> void addSilently(final String cacheKey, final int expiration, final Object value, final Class<?> clazz) {
        try {
            add(cacheKey, expiration, value, clazz);
        } catch (TimeoutException e) {
            LOGGER.warn("Cannot add to key " + cacheKey, e);
        } catch (CacheException e) {
            LOGGER.warn("Cannot add to key " + cacheKey, e);
        }
    }

    @Override
    public Map<String, Object> getBulk(final Collection<String> keys, final Class<?> clazz) throws TimeoutException, CacheException {
        if (clazz != null) {
            CacheTranscoder<Object> transcoder = getTranscoder(clazz);
            if (transcoder != null) {
                return cacheClient.getBulk(keys, transcoder);
            } else {
                LOGGER.warn("There's no json transcoder for class " + clazz.getName() + ", standard transcoder will be used for keys "
                        + keys);
            }
        }

        return cacheClient.getBulk(keys);
    }

    @Override
    public long decr(final String key, final int by) throws TimeoutException, CacheException {
        return cacheClient.decr(key, by);
    }

    @Override
    public boolean delete(final String key) throws TimeoutException, CacheException {
        return cacheClient.delete(key);
    }

    @Override
    public void delete(final Collection<String> keys) throws TimeoutException, CacheException {
        cacheClient.delete(keys);
    }

    @Override
    public void flush() throws TimeoutException, CacheException {
        cacheClient.flush();
    }

    @Override
    public long incr(final String key, final int by, final long def) throws TimeoutException, CacheException {
        return cacheClient.incr(key, by, def);
    }

    @Override
    public long incr(final String key, final int by, final long def, final int exp) throws TimeoutException, CacheException {
        return cacheClient.incr(key, by, def, exp);
    }

    @Override
    @PreDestroy
    public void shutdown() {
        cacheClient.shutdown();
    }

    void changeCacheClient(final CacheClient newCacheClient) {
        if (newCacheClient != null) {
            LOGGER.info("Replacing the cache client");
            CacheClient oldCacheClient = cacheClient;
            cacheClient = newCacheClient;
            LOGGER.info("Cache client replaced");
            LOGGER.info("Closing old cache client");
            oldCacheClient.shutdown();
            LOGGER.info("Old cache client closed");
        }
    }

    @SuppressWarnings("unchecked")
    private CacheTranscoder<Object> getTranscoder(final Class<?> clazz) {
        return (CacheTranscoder<Object>) transcoders.get(clazz);
    }

}
