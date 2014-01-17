/*
 * Copyright (c) 2013-2014 Jakub Białek
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * @since 3.3.0
 * 
 */
public class PrefixedCacheImpl implements Cache {

    private final Cache cache;

    private final String name;

    private final String namePrefixSeparator;

    public PrefixedCacheImpl(final Cache cache, final String requestedName, final String namePrefixSeparator) {
        this.cache = cache;
        this.name = requestedName;
        this.namePrefixSeparator = namePrefixSeparator;
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        return cache.getAvailableServers();
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Collection<String> getAliases() {
        return cache.getAliases();
    }

    @Override
    public CacheProperties getProperties() {
        return cache.getProperties();
    }

    @Override
    public <T> void add(final String key, final int exp, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException {
        cache.add(alterKey(key), exp, value, serializationType);
    }

    @Override
    public <T> void addSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType) {
        cache.addSilently(alterKey(cacheKey), expiration, value, serializationType);
    }

    @Override
    public long decr(final String key, final int by) throws TimeoutException, CacheException {
        return cache.decr(alterKey(key), by);
    }

    @Override
    public boolean delete(final String key) throws TimeoutException, CacheException {
        return cache.delete(alterKey(key));
    }

    @Override
    public void delete(final Collection<String> keys) throws TimeoutException, CacheException {
        cache.delete(alterKeys(keys));
    }

    @Override
    public void flush() throws TimeoutException, CacheException {
        cache.flush();
    }

    @Override
    public <T> T get(final String key, final SerializationType serializationType) throws TimeoutException, CacheException {
        return cache.get(alterKey(key), serializationType);
    }

    @Override
    public Map<String, Object> getBulk(final Collection<String> keys, final SerializationType serializationType) throws TimeoutException,
            CacheException {
        return cache.getBulk(alterKeys(keys), serializationType);
    }

    @Override
    public long incr(final String key, final int by, final long def) throws TimeoutException, CacheException {
        return cache.incr(alterKey(key), by, def);
    }

    @Override
    public long incr(final String key, final int by, final long def, final int exp) throws TimeoutException, CacheException {
        return cache.incr(alterKey(key), by, def, exp);
    }

    @Override
    public <T> void set(final String key, final int exp, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException {
        cache.set(alterKey(key), exp, value, serializationType);
    }

    @Override
    public <T> void setSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType) {
        cache.setSilently(alterKey(cacheKey), expiration, value, serializationType);
    }

    @Override
    public Long getCounter(final String cacheKey) throws TimeoutException, CacheException {
        return cache.getCounter(alterKey(cacheKey));
    }

    @Override
    public void setCounter(final String cacheKey, final int expiration, final long value) throws TimeoutException, CacheException {
        cache.setCounter(alterKey(cacheKey), expiration, value);
    }

    @Override
    public void shutdown() {
        cache.shutdown();
    }

    private String alterKey(final String cacheKey) {
        return name + namePrefixSeparator + cacheKey;
    }

    private Collection<String> alterKeys(final Collection<String> keys) {
        List<String> alteredKeys = new ArrayList<String>(keys.size());
        for (String key : keys) {
            alteredKeys.add(alterKey(key));
        }

        return alteredKeys;
    }
}
