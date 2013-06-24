/*
 * Copyright (c) 2010-2013 Jakub Białek
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

package com.google.code.ssm.providers.spymemcached;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.CachedData;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.transcoders.Transcoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.providers.AbstractMemcacheClientWrapper;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
class MemcacheClientWrapper extends AbstractMemcacheClientWrapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(MemcacheClientWrapper.class);

    private final Map<CacheTranscoder, Object> adapters = new HashMap<CacheTranscoder, Object>();

    private final MemcachedClientIF memcachedClient;

    MemcacheClientWrapper(final MemcachedClientIF memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    @Override
    public boolean add(final String key, final int exp, final Object value) throws TimeoutException, CacheException {
        Future<Boolean> f = null;
        try {
            f = memcachedClient.add(key, exp, value);
            return f.get();
        } catch (InterruptedException e) {
            cancel(f);
            throw new CacheException(e);
        } catch (ExecutionException e) {
            cancel(f);
            throw new CacheException(e);
        }
    }

    @Override
    public <T> boolean add(final String key, final int exp, final T value, final CacheTranscoder transcoder) throws TimeoutException,
            CacheException {
        Future<Boolean> f = null;
        try {
            f = memcachedClient.add(key, exp, value, getTranscoder(transcoder));
            return f.get();
        } catch (InterruptedException e) {
            cancel(f);
            throw new CacheException(e);
        } catch (ExecutionException e) {
            cancel(f);
            throw new CacheException(e);
        }
    }

    @Override
    public long decr(final String key, final int by) throws TimeoutException, CacheException {
        try {
            return memcachedClient.decr(key, by);
        } catch (OperationTimeoutException e) {
            LOGGER.warn("Operation timeout while decr " + key, e);
            throw new TimeoutException(e.getMessage());
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public long decr(final String key, final int by, final long def) throws TimeoutException, CacheException {
        try {
            return memcachedClient.decr(key, by, def);
        } catch (OperationTimeoutException e) {
            LOGGER.warn("Operation timeout while decr " + key, e);
            throw new TimeoutException(e.getMessage());
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public boolean delete(final String key) throws TimeoutException, CacheException {
        try {
            return memcachedClient.delete(key).get();
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (ExecutionException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void flush() throws CacheException {
        try {
            memcachedClient.flush().get();
        } catch (InterruptedException e) {
            throw new CacheException(e);
        } catch (ExecutionException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Object get(final String key) throws TimeoutException, CacheException {
        try {
            return memcachedClient.get(key);
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            } else if (e.getCause() instanceof TimeoutException) {
                throw (TimeoutException) e.getCause();
            }

            throw e;
        }
    }

    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder) throws CacheException, TimeoutException {
        try {
            return memcachedClient.get(key, this.<T> getTranscoder(transcoder));
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            } else if (e.getCause() instanceof TimeoutException) {
                throw (TimeoutException) e.getCause();
            }

            throw e;
        }
    }

    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder, final long timeout) throws TimeoutException, CacheException {
        Future<T> f = null;
        try {
            f = memcachedClient.asyncGet(key, this.<T> getTranscoder(transcoder));
            return f.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            cancel(f);
            throw new CacheException(e);
        } catch (ExecutionException e) {
            cancel(f);
            throw new CacheException(e);
        }
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        return memcachedClient.getAvailableServers();
    }

    @Override
    public Map<String, Object> getBulk(final Collection<String> keys) throws TimeoutException, CacheException {
        try {
            return memcachedClient.getBulk(keys);
        } catch (OperationTimeoutException e) {
            throw (TimeoutException) e.getCause();
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public <T> Map<String, T> getBulk(final Collection<String> keys, final CacheTranscoder transcoder) throws TimeoutException,
            CacheException {
        try {
            return memcachedClient.getBulk(keys, this.<T> getTranscoder(transcoder));
        } catch (OperationTimeoutException e) {
            throw (TimeoutException) e.getCause();
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public long incr(final String key, final int by) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by);
        } catch (OperationTimeoutException e) {
            LOGGER.warn("Operation timeout while incr " + key, e);
            throw new TimeoutException(e.getMessage());
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public long incr(final String key, final int by, final long def) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by, def);
        } catch (OperationTimeoutException e) {
            LOGGER.warn("Operation timeout while incr " + key, e);
            throw new TimeoutException(e.getMessage());
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public long incr(final String key, final int by, final long def, final int expiration) throws TimeoutException, CacheException {
        try {
            return memcachedClient.incr(key, by, def, expiration);
        } catch (OperationTimeoutException e) {
            LOGGER.warn("Operation timeout while incr " + key, e);
            throw new TimeoutException(e.getMessage());
        } catch (RuntimeException e) {
            if (translateException(e)) {
                throw new CacheException(e);
            }
            throw e;
        }
    }

    @Override
    public boolean set(final String key, final int exp, final Object value) throws TimeoutException, CacheException {
        Future<Boolean> f = null;
        try {
            f = memcachedClient.set(key, exp, value);
            return f.get();
        } catch (InterruptedException e) {
            cancel(f);
            throw new CacheException(e);
        } catch (ExecutionException e) {
            cancel(f);
            throw new CacheException(e);
        }
    }

    @Override
    public <T> boolean set(final String key, final int exp, final T value, final CacheTranscoder transcoder) throws TimeoutException,
            CacheException {
        Future<Boolean> f = null;
        try {
            f = memcachedClient.set(key, exp, value, getTranscoder(transcoder));
            return f.get();
        } catch (InterruptedException e) {
            cancel(f);
            throw new CacheException(e);
        } catch (ExecutionException e) {
            cancel(f);
            throw new CacheException(e);
        }
    }

    @Override
    public void shutdown() {
        memcachedClient.shutdown();
    }

    @Override
    public CacheTranscoder getTranscoder() {
        return new TranscoderWrapper(memcachedClient.getTranscoder());
    }

    @SuppressWarnings("unchecked")
    private <T> Transcoder<T> getTranscoder(final CacheTranscoder transcoder) {
        Transcoder<T> transcoderAdapter = (Transcoder<T>) adapters.get(transcoder);
        if (transcoderAdapter == null) {
            transcoderAdapter = (Transcoder<T>) new TranscoderAdapter(transcoder);
            adapters.put(transcoder, transcoderAdapter);
        }

        return transcoderAdapter;
    }

    private void cancel(final Future<?> f) {
        if (f != null) {
            f.cancel(true);
        }
    }

    private boolean translateException(final RuntimeException e) {
        return e.getCause() instanceof InterruptedException || e.getCause() instanceof ExecutionException;
    }

    private static class TranscoderWrapper implements CacheTranscoder {

        private final Transcoder<Object> transcoder;

        public TranscoderWrapper(final Transcoder<Object> transcoder) {
            this.transcoder = transcoder;
        }

        @Override
        public Object decode(final CachedObject data) {
            return transcoder.decode(new CachedData(data.getFlags(), data.getData(), CachedObject.MAX_SIZE));
        }

        @Override
        public CachedObject encode(final Object o) {
            CachedData cachedData = transcoder.encode(o);
            return new CachedObjectImpl(cachedData.getFlags(), cachedData.getData());
        }
    }

}
