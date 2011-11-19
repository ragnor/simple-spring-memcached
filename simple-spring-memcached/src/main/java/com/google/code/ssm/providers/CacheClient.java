package com.google.code.ssm.providers;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
 * @author Jakub Białek
 * 
 */
public interface CacheClient {

    boolean add(String key, int exp, Object value) throws TimeoutException, CacheException;

    <T> boolean add(final String key, final int exp, final T value, final CacheTranscoder<T> transcoder) throws TimeoutException,
            CacheException;

    long decr(String key, int by) throws TimeoutException, CacheException;

    long decr(String key, int by, long def) throws TimeoutException, CacheException;

    /**
     * Deletes value under given key.
     * 
     * @param key
     *            the key
     * @return
     * @throws TimeoutException
     * @throws CacheException
     */
    boolean delete(String key) throws TimeoutException, CacheException;

    /**
     * Deletes values under given keys.
     * 
     * @param keys
     * @throws TimeoutException
     * @throws CacheException
     */
    void delete(Collection<String> keys) throws TimeoutException, CacheException;

    /**
     * Flushes all data.
     * 
     * @throws TimeoutException
     * @throws CacheException
     */
    void flush() throws TimeoutException, CacheException;

    Object get(final String key) throws TimeoutException, CacheException;

    <T> T get(final String key, final CacheTranscoder<T> transcoder) throws TimeoutException, CacheException;

    /**
     * Get value by key.
     * 
     * @param <T>
     * @param key
     *            the key
     * @param transcoder
     *            the transcoder to use
     * @param timeout
     *            the timeout, if the method is not returned in this time, throws TimeoutException
     * @return value associated with given key or null
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> T get(String key, CacheTranscoder<T> transcoder, final long timeout) throws TimeoutException, CacheException;

    /**
     * Gets available memcached servers.
     * 
     * @return collection of memcached servers
     */
    Collection<SocketAddress> getAvailableServers();

    Map<String, Object> getBulk(Collection<String> keys) throws TimeoutException, CacheException;

    <T> Map<String, T> getBulk(Collection<String> keys, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException;

    /**
     * Gets default transcoder.
     * 
     * @return default transcoder
     */
    CacheTranscoder<?> getTranscoder();

    long incr(String key, int by) throws TimeoutException, CacheException;

    long incr(String key, int by, long def) throws TimeoutException, CacheException;

    long incr(String key, int by, long def, int exp) throws TimeoutException, CacheException;

    boolean set(String key, int exp, Object value) throws TimeoutException, CacheException;

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
     * @param transcoder
     *            the transcoder to use
     * @return boolean result
     * @throws TimeoutException
     * @throws CacheException
     */
    <T> boolean set(final String key, final int exp, final T value, final CacheTranscoder<T> transcoder) throws TimeoutException,
            CacheException;

    /**
     * Shutdowns memcached client.
     */
    void shutdown();

}
