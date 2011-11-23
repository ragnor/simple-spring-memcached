/*
 * Copyright (c) 2010-2011 Jakub Białek
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

package com.google.code.ssm.providers.xmemcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.transcoders.CachedData;
import net.rubyeye.xmemcached.transcoders.Transcoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class MemcacheClientWrapper implements CacheClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcacheClientWrapper.class);

    private MemcachedClient memcachedClient;

    private Map<CacheTranscoder<?>, Transcoder<?>> adapters = new HashMap<CacheTranscoder<?>, Transcoder<?>>();

    public MemcacheClientWrapper(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    @Override
    public boolean add(String key, int exp, Object value) throws TimeoutException, CacheException {
        try {
            return memcachedClient.add(key, exp, value);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> boolean add(String key, int exp, T value, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        try {
            return memcachedClient.add(key, exp, value, getTranscoder(transcoder));
        } catch (MemcachedException e) {
            throw new CacheException(e);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long decr(String key, int by) throws TimeoutException, CacheException {
        try {
            return memcachedClient.decr(key, by);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long decr(String key, int by, long def) throws TimeoutException, CacheException {
        try {
            return memcachedClient.decr(key, by, def);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public boolean delete(String key) throws TimeoutException, CacheException {
        try {
            return memcachedClient.delete(key);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void delete(Collection<String> keys) throws TimeoutException, CacheException {
        if (keys != null && keys.size() > 0) {
            for (final String key : keys) {
                if (key != null) {
                    delete(key);
                }
            }
        }
    }

    @Override
    public void flush() throws TimeoutException, CacheException {
        try {
            memcachedClient.flushAll();
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Object get(String key) throws TimeoutException, CacheException {
        try {
            return memcachedClient.get(key);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> T get(String key, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        try {
            return memcachedClient.get(key, getTranscoder(transcoder));
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> T get(String key, CacheTranscoder<T> transcoder, long timeout) throws TimeoutException, CacheException {
        try {
            return memcachedClient.get(key, timeout, getTranscoder(transcoder));
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        List<SocketAddress> servers = new ArrayList<SocketAddress>();
        Collection<InetSocketAddress> inetSocketAddresses = memcachedClient.getAvaliableServers();

        if (inetSocketAddresses != null && inetSocketAddresses.size() > 0) {
            servers.addAll(memcachedClient.getAvaliableServers());
        }

        return servers;
    }

    @Override
    public Map<String, Object> getBulk(Collection<String> keys) throws TimeoutException, CacheException {
        Map<String, Object> result = null;
        try {
            result = memcachedClient.get(keys);
            return result == null ? Collections.<String, Object> emptyMap() : result;
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> Map<String, T> getBulk(Collection<String> keys, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        Map<String, T> result = null;
        try {
            result = memcachedClient.get(keys, getTranscoder(transcoder));
            return result == null ? Collections.<String, T> emptyMap() : result;
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long incr(String key, int by) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long incr(String key, int by, long def) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by, def);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long incr(String key, int by, long def, int expiration) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by, def, memcachedClient.getOpTimeout(), expiration);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public boolean set(String key, int exp, Object value) throws TimeoutException, CacheException {
        try {
            return memcachedClient.set(key, exp, value);
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    };

    @Override
    public <T> boolean set(String key, int exp, T value, CacheTranscoder<T> transcoder) throws TimeoutException, CacheException {
        try {
            return memcachedClient.set(key, exp, value, getTranscoder(transcoder));
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (MemcachedException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            memcachedClient.shutdown();
        } catch (IOException e) {
            LOGGER.error("An error occurred when closing memcache", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CacheTranscoder<?> getTranscoder() {
        return new TranscoderWrapper(memcachedClient.getTranscoder());
    }

    @SuppressWarnings("unchecked")
    private <T> Transcoder<T> getTranscoder(CacheTranscoder<T> transcoder) {
        Transcoder<T> transcoderAdapter = (Transcoder<T>) adapters.get(transcoder);
        if (transcoderAdapter == null) {
            transcoderAdapter = new TranscoderAdapter<T>(transcoder);
            adapters.put(transcoder, transcoderAdapter);
        }

        return transcoderAdapter;
    }

    private static class TranscoderWrapper implements CacheTranscoder<Object> {

        private Transcoder<Object> transcoder;

        public TranscoderWrapper(Transcoder<Object> transcoder) {
            this.transcoder = transcoder;
        }

        @Override
        public Object decode(CachedObject data) {
            return transcoder.decode(new CachedData(data.getFlags(), data.getData()));
        }

        @Override
        public CachedObject encode(Object o) {
            CachedData cachedData = transcoder.encode(o);
            return new CachedObjectImpl(cachedData.getFlag(), cachedData.getData());
        }
    }

}
