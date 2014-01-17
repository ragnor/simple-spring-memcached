/*
 * Copyright (c) 2012-2014 Jakub Białek
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

import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public interface Cache {

    /**
     * Gets available cache servers.
     * 
     * @return collection of cache servers
     */
    Collection<SocketAddress> getAvailableServers(); // NO_UCD

    /**
     * Gets name of the cache.
     * 
     * @return the name of cache
     */
    String getName();

    /**
     * Gets optional cache's aliases.
     * 
     * @return the optional aliases of cache
     */
    Collection<String> getAliases();

    /**
     * @since 3.3.0
     * @return the properties of cache
     */
    CacheProperties getProperties();

    <T> void add(final String key, final int exp, final Object value, final SerializationType serializationType) throws TimeoutException,
            CacheException;

    <T> void addSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType);

    long decr(final String key, final int by) throws TimeoutException, CacheException;

    /**
     * Deletes value under given key.
     * 
     * @param key
     *            the key
     * @throws TimeoutException
     * @throws CacheException
     */
    boolean delete(final String key) throws TimeoutException, CacheException;

    /**
     * Deletes values under given keys.
     * 
     * @param keys
     * @throws TimeoutException
     * @throws CacheException
     */
    void delete(final Collection<String> keys) throws TimeoutException, CacheException;

    /**
     * Flushes all data.
     * 
     * @throws TimeoutException
     * @throws CacheException
     */
    void flush() throws TimeoutException, CacheException;

    /**
     * Get value by key.
     * 
     * @param <T>
     * @param key
     *            the key
     * @param serializationType
     *            the type of serialisation to use
     * @return value associated with given key or null
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> T get(final String key, final SerializationType serializationType) throws TimeoutException, CacheException;

    Map<String, Object> getBulk(final Collection<String> keys, final SerializationType serializationType) throws TimeoutException,
            CacheException;

    long incr(final String key, final int by, final long def) throws TimeoutException, CacheException;

    long incr(final String key, final int by, final long def, final int exp) throws TimeoutException, CacheException;

    /**
     * Store key-value item to memcached.
     * 
     * @param <T>
     * @param key
     *            stored key
     * @param exp
     *            expire time
     * @param value
     *            stored data
     * @param serializationType
     *            the type of serialisation to use
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> void set(final String key, final int exp, final Object value, final SerializationType serializationType) throws TimeoutException,
            CacheException;

    <T> void setSilently(final String cacheKey, final int expiration, final Object value, final SerializationType serializationType);

    /**
     * Gets counter from cache without incrementing.
     * 
     * @param cacheKey
     * @return the value of counter
     * @throws CacheException
     * @throws TimeoutException
     */
    Long getCounter(final String cacheKey) throws TimeoutException, CacheException;

    /**
     * Sets initial value of counter.
     * 
     * @param cacheKey
     * @param expiration
     * @param value
     * @throws CacheException
     * @throws TimeoutException
     */
    void setCounter(final String cacheKey, final int expiration, final long value) throws TimeoutException, CacheException;

    /**
     * Shutdowns cache.
     */
    void shutdown();

}
