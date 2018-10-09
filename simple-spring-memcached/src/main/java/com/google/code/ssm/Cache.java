/*
 * Copyright (c) 2012-2018 Jakub Białek
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

    /**
     * 
     * @since 3.5.0
     * @return true if cache is enabled
     */
    boolean isEnabled();

    /**
     * Add object to cache if it doesn't exist.
     * 
     * @param key
     *            the key
     * @param expiration
     *            expiration time in seconds as defined in memcached specification
     * @param value
     *            the value to add
     * @param serializationType
     *            type of serialization
     * @return true if a mutation has occurred (object didn't exist in cache)
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> boolean add(final String key, final int expiration, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException;

    /**
     * Add object to cache if it doesn't exist. Some exceptions are muted by this method.
     * 
     * @param key
     *            the cache key
     * @param expiration
     *            the expiration time in seconds as defined in memcached specification
     * @param value
     *            the value to add
     * @param serializationType
     *            the type of serialization
     * @return
     */
    <T> boolean addSilently(final String key, final int expiration, final Object value, final SerializationType serializationType);

    /**
     * Decrement counter in cache by given value.
     * 
     * @param key
     *            the key
     * @param by
     *            decrement value
     * @return current counter's value
     * @throws TimeoutException
     * @throws CacheException
     */
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
     *            the type of serialization to use
     * @return value associated with given key or null
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> T get(final String key, final SerializationType serializationType) throws TimeoutException, CacheException;

    Map<String, Object> getBulk(final Collection<String> keys, final SerializationType serializationType)
            throws TimeoutException, CacheException;

    /**
     * Increments counter in cache by given value.
     * 
     * @param key
     *            the key
     * @param by
     *            increment value
     * @param def
     *            initial value
     * @return
     * @throws TimeoutException
     * @throws CacheException
     */
    long incr(final String key, final int by, final long def) throws TimeoutException, CacheException;

    /**
     * Increments counter in cache by given value.
     * 
     * @param key
     *            the key
     * @param by
     *            increment value
     * @param def
     *            initial value
     * @param expiration
     *            expiration time in seconds as defined in memcached specification
     * @return
     * @throws TimeoutException
     * @throws CacheException
     */
    long incr(final String key, final int by, final long def, final int expiration) throws TimeoutException, CacheException;

    /**
     * Store key-value item to memcached.
     * 
     * @param <T>
     * @param key
     *            stored key
     * @param expiration
     *            expiration time in seconds as defined in memcached specification
     * @param value
     *            stored data
     * @param serializationType
     *            the type of serialization to use
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> void set(final String key, final int expiration, final Object value, final SerializationType serializationType)
            throws TimeoutException, CacheException;

    /**
     * Store key-value item to memcached. Mute some exceptions.
     * 
     * @param <T>
     * @param key
     *            stored key
     * @param expiration
     *            expiration time in seconds as defined in memcached specification
     * @param value
     *            stored data
     * @param serializationType
     *            the type of serialization to use
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> void setSilently(final String key, final int expiration, final Object value, final SerializationType serializationType);

    /**
     * Gets counter from cache without incrementing.
     * 
     * @param key
     * @return the value of counter
     * @throws CacheException
     * @throws TimeoutException
     */
    Long getCounter(final String key) throws TimeoutException, CacheException;

    /**
     * Sets initial value of counter.
     * 
     * @param key
     *            the key
     * @param expiration
     *            expiration time in seconds as defined in memcached specification
     * @param value
     *            the value
     * @throws CacheException
     * @throws TimeoutException
     */
    void setCounter(final String key, final int expiration, final long value) throws TimeoutException, CacheException;

    /**
     * Shutdowns cache.
     */
    void shutdown();

    /**
     * Expose native memcached client. Do not store reference to this client because it can change in runtime when using
     * some feature (like runtime memcached nodes switching).
     * 
     * @since 3.5.0
     * @return current instance of native memcached client
     */
    Object getNativeClient();

}
