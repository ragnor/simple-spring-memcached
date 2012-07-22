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
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.transcoders.JavaTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoder;
import com.google.code.ssm.transcoders.LongToStringTranscoder;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
class CacheImpl implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheImpl.class);

    @Getter
    private final String name;

    @Getter
    private final Collection<String> aliases;

    private final SerializationType defaultSerializationType;

    private final JsonTranscoder jsonTranscoder;

    private final JavaTranscoder javaTranscoder;

    private final LongToStringTranscoder longToStringTranscoder = new LongToStringTranscoder();

    private final CacheTranscoder customTranscoder;

    private volatile CacheClient cacheClient;

    CacheImpl(final String name, final Collection<String> aliases, final CacheClient cacheClient,
            final SerializationType defaultSerializationType, final JsonTranscoder jsonTranscoder, final JavaTranscoder javaTranscoder,
            final CacheTranscoder customTranscoder) {
        Assert.hasText(name, "'name' must not be null, empty, or blank");
        Assert.notNull(aliases, "'aliases' cannot be null");
        Assert.notNull(cacheClient, "'cacheClient' cannot be null");
        Assert.notNull(defaultSerializationType, "'defaultSerializationType' cannot be null");
        Assert.notNull(jsonTranscoder, "'jsonTranscoder' cannot be null");
        Assert.notNull(javaTranscoder, "'javaTranscoder' cannot be null");

        this.name = name;
        this.aliases = aliases;
        this.cacheClient = cacheClient;
        this.defaultSerializationType = defaultSerializationType;
        this.jsonTranscoder = jsonTranscoder;
        this.javaTranscoder = javaTranscoder;
        this.customTranscoder = customTranscoder;
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        return cacheClient.getAvailableServers();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final String cacheKey, final SerializationType serializationType) throws TimeoutException, CacheException {

        switch (getSerializationType(serializationType)) {
        case JAVA:
            return (T) cacheClient.get(cacheKey, javaTranscoder);
        case JSON:
            return (T) cacheClient.get(cacheKey, jsonTranscoder);
        case PROVIDER:
            return (T) cacheClient.get(cacheKey);
        case CUSTOM:
            return (T) cacheClient.get(cacheKey, customTranscoder);
        default:
            throw new IllegalArgumentException(String.format("Serialization type %s is not supported", serializationType));
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void set(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException {

        switch (getSerializationType(serializationType)) {
        case JAVA:
            cacheClient.set(cacheKey, expiration, (T) value, javaTranscoder);
            break;
        case JSON:
            cacheClient.set(cacheKey, expiration, (T) value, jsonTranscoder);
            break;
        case PROVIDER:
            cacheClient.set(cacheKey, expiration, value);
            break;
        case CUSTOM:
            cacheClient.set(cacheKey, expiration, (T) value, customTranscoder);
            break;
        default:
            throw new IllegalArgumentException(String.format("Serialization type %s is not supported", serializationType));
        }
    }

    @Override
    public <T> void setSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType) {
        try {
            set(cacheKey, expiration, value, serializationType);
        } catch (TimeoutException e) {
            warn(e, "Cannot set on key %s", cacheKey);
        } catch (CacheException e) {
            warn(e, "Cannot set on key %s", cacheKey);
        }
    }

    @Override
    public <T> void add(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException {

        switch (getSerializationType(serializationType)) {
        case JAVA:
            cacheClient.add(cacheKey, expiration, value, javaTranscoder);
            break;
        case JSON:
            cacheClient.add(cacheKey, expiration, value, jsonTranscoder);
            break;
        case PROVIDER:
            cacheClient.add(cacheKey, expiration, value);
            break;
        case CUSTOM:
            cacheClient.add(cacheKey, expiration, value, customTranscoder);
            break;
        default:
            throw new IllegalArgumentException(String.format("Serialization type %s is not supported", serializationType));
        }
    }

    @Override
    public <T> void addSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType) {
        try {
            add(cacheKey, expiration, value, serializationType);
        } catch (TimeoutException e) {
            warn(e, "Cannot add to key %s", cacheKey);
        } catch (CacheException e) {
            warn(e, "Cannot add to key %s", cacheKey);
        }
    }

    @Override
    public Map<String, Object> getBulk(final Collection<String> keys, final SerializationType serializationType) throws TimeoutException,
            CacheException {

        switch (getSerializationType(serializationType)) {
        case JAVA:
            return cacheClient.getBulk(keys, javaTranscoder);
        case JSON:
            return cacheClient.getBulk(keys, jsonTranscoder);
        case PROVIDER:
            return cacheClient.getBulk(keys);
        case CUSTOM:
            return cacheClient.getBulk(keys, customTranscoder);
        default:
            throw new IllegalArgumentException(String.format("Serialization type %s is not supported", serializationType));
        }
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
    public Long getCounter(final String cacheKey) throws TimeoutException, CacheException {
        return cacheClient.get(cacheKey, longToStringTranscoder);
    }

    @Override
    public void setCounter(final String cacheKey, final int expiration, final long value) throws TimeoutException, CacheException {
        cacheClient.set(cacheKey, expiration, value, longToStringTranscoder);
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

    private SerializationType getSerializationType(final SerializationType serializationType) {
        return (serializationType != null) ? serializationType : defaultSerializationType;
    }

    private void warn(final Exception e, final String format, final Object... args) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format(format, args), e);
        }
    }

}
