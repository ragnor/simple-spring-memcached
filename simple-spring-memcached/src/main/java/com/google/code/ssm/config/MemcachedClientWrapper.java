package com.google.code.ssm.config;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.code.ssm.providers.MemcacheClient;
import com.google.code.ssm.providers.MemcacheException;
import com.google.code.ssm.providers.MemcacheTranscoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class MemcachedClientWrapper implements MemcacheClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedClientWrapper.class);

    private volatile MemcacheClient memcachedClient;

    public MemcachedClientWrapper(MemcacheClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public boolean add(String key, int exp, Object value) throws TimeoutException, MemcacheException {
        return memcachedClient.add(key, exp, value);
    }

    public <T> boolean add(String key, int exp, T value, MemcacheTranscoder<T> transcoder) throws TimeoutException, MemcacheException {
        return memcachedClient.add(key, exp, value, transcoder);
    }

    public void changeMemcachedClient(MemcacheClient newMemcachedClient) {
        if (newMemcachedClient != null) {
            LOGGER.info("Replacing the memcached client");
            MemcacheClient oldMemcachedClient = memcachedClient;
            memcachedClient = newMemcachedClient;
            LOGGER.info("Memcached client replaced");
            LOGGER.info("Closing old memcached client");
            oldMemcachedClient.shutdown();
            LOGGER.info("Old memcached client closed");
        }
    }

    public long decr(String key, int by) throws TimeoutException, MemcacheException {
        return memcachedClient.decr(key, by);
    }

    public long decr(String key, int by, long def) throws TimeoutException, MemcacheException {
        return memcachedClient.decr(key, by, def);
    }

    public boolean delete(String key) throws TimeoutException, MemcacheException {
        return memcachedClient.delete(key);
    }

    @Override
    public void flush() throws TimeoutException, MemcacheException {
        memcachedClient.flush();
    }

    public Object get(String key) throws TimeoutException, MemcacheException {
        return memcachedClient.get(key);
    }

    public <T> T get(String key, MemcacheTranscoder<T> transcoder) throws TimeoutException, MemcacheException {
        return memcachedClient.get(key, transcoder);
    }

    public <T> T get(String key, MemcacheTranscoder<T> transcoder, long timeout) throws TimeoutException, MemcacheException {
        return memcachedClient.get(key, transcoder, timeout);
    }

    public Collection<SocketAddress> getAvailableServers() {
        return memcachedClient.getAvailableServers();
    }

    public Map<String, Object> getBulk(Collection<String> keys) throws TimeoutException, MemcacheException {
        return memcachedClient.getBulk(keys);
    }

    public <T> Map<String, T> getBulk(Collection<String> keys, MemcacheTranscoder<T> transcoder) throws TimeoutException, MemcacheException {
        return memcachedClient.getBulk(keys, transcoder);
    }

    @Override
    public MemcacheTranscoder<?> getTranscoder() {
        return memcachedClient.getTranscoder();
    }

    public long incr(String key, int by) throws TimeoutException, MemcacheException {
        return memcachedClient.incr(key, by);
    }

    public long incr(String key, int by, long def) throws TimeoutException, MemcacheException {
        return memcachedClient.incr(key, by, def);
    }

    public long incr(String key, int by, long def, int exp) throws TimeoutException, MemcacheException {
        return memcachedClient.incr(key, by, def, exp);
    }

    public boolean set(String key, int exp, Object value) throws TimeoutException, MemcacheException {
        return memcachedClient.set(key, exp, value);
    }

    public <T> boolean set(String key, int exp, T value, MemcacheTranscoder<T> transcoder) throws TimeoutException, MemcacheException {
        return memcachedClient.set(key, exp, value, transcoder);
    }

    @Override
    public void shutdown() {
        memcachedClient.shutdown();
    }

}
