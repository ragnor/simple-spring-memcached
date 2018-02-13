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
package com.google.code.ssm.spring;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.providers.CacheException;

/**
 * Wrapper around {@link com.google.code.ssm.Cache} that allow to set default expiration time used in all
 * {@link SSMCache#put(Object, Object)} (
 * {@link com.google.code.ssm.Cache#set(String, int, Object, com.google.code.ssm.api.format.SerializationType)})
 * requests.
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class SSMCache implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSMCache.class);

    @Getter
    private final com.google.code.ssm.Cache cache;

    @Getter
    private final int expiration;

    @Getter
    private final boolean allowClear;
    
    /**
     * If true exceptions thrown by underlying cache are muted and aren't forwarded to Spring Cache Abstraction, 
     * it's behaviour of SSM before 4.0.0. If false then exceptions will be passed to caller and optionally wrapped if not
     * RuntimeException. 
     * 
     * @since 4.0.0
     */
    @Getter
    private final boolean muteExceptions;

    /**
     * 
     * If true then all aliases of the underlying cache will be used to register the cache in Spring, otherwise the
     * cache will be available only by name.
     * 
     * @since 3.3.0
     */
    @Getter
    private final boolean registerAliases;

    public SSMCache(final com.google.code.ssm.Cache cache, final int expiration, final boolean allowClear, final boolean registerAliases,
            final boolean muteExceptions) {
        this.cache = cache;
        this.expiration = expiration;
        this.allowClear = allowClear;
        this.registerAliases = registerAliases;
        this.muteExceptions = muteExceptions;
    }

    public SSMCache(final com.google.code.ssm.Cache cache, final int expiration, final boolean allowClear) {
        this(cache, expiration, allowClear, false, false);
    }

    public SSMCache(final com.google.code.ssm.Cache cache, final int expiration) {
        this(cache, expiration, false);
    }

    public SSMCache(final SSMCache ssmCache, final int expiration) {
        this(ssmCache.cache, expiration, ssmCache.allowClear);
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
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot get {} from cache", cache.getName(), key);
            return null;
        }

        Object value = getValue(key);
        if (value == null) {
            LOGGER.info("Cache miss. Get by key {} from cache {}", key, cache.getName());
            return null;
        }

        LOGGER.info("Cache hit. Get by key {} from cache {} value '{}'", new Object[] { key, cache.getName(), value });
        return value instanceof PertinentNegativeNull ? new SimpleValueWrapper(null) : new SimpleValueWrapper(value);
    }

    /**
     * Required by Spring 4.0
     * 
     * @since 3.4.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final Object key, final Class<T> type) {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot get {} from cache", cache.getName(), key);
            return null;
        }

        Object value = getValue(key);
        if (value == null) {
            LOGGER.info("Cache miss. Get by key {} and type {} from cache {}", new Object[] { key, type, cache.getName() });
            return null;
        }

        if (value instanceof PertinentNegativeNull) {
            return null;
        }

        if (type != null && !type.isInstance(value)) {
            // in such case default Spring back end for EhCache throws IllegalStateException which interrupts
            // intercepted method invocation
            String msg = "Cached value is not of required type [" + type.getName() + "]: " + value;
            LOGGER.error(msg, new IllegalStateException(msg));
            return null;
        }

        LOGGER.info("Cache hit. Get by key {} and type {}  from cache {} value '{}'", new Object[] { key, type, cache.getName(), value });
        return (T) value;
    }
    
    /**
     * Required by Spring 4.3
     * 
     * @since 3.6.1
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(final Object key, final Callable<T> valueLoader) {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot get {} from cache", cache.getName(), key);
            return loadValue(key, valueLoader);
        }
        final ValueWrapper valueWrapper = get(key);
        if (valueWrapper != null) {
            return (T) valueWrapper.get();
        }
        synchronized (key.toString().intern()) {
            final T value = loadValue(key, valueLoader);
            put(key, value);
            return value;
        }
    }

    @Override
    public void put(final Object key, final Object value) {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot put value under key {}", cache.getName(), key);
            return;
        }

        if (key != null) {
            final String cacheKey = getKey(key);

            try {
                LOGGER.info("Put '{}' under key {} to cache {}", new Object[] { value, key, cache.getName() });
                final Object store = toStoreValue(value);
                cache.set(cacheKey, expiration, store, null);
            } catch (TimeoutException | CacheException | RuntimeException e) {
                logOrThrow(e, "An error has ocurred for cache {} and key {}", getName(), cacheKey, e);
            }
        } else {
            LOGGER.info("Cannot put to cache {} because key is null", cache.getName());
        }
    }

    /**
     * Required by Spring 4.1
     * 
     * @since 3.6.0
     */
    @Override
    public ValueWrapper putIfAbsent(final Object key, final Object value) {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot put value under key {}", cache.getName(), key);
            return null;
        }

        if (key != null) {
            final String cacheKey = getKey(key);

            try {
                LOGGER.info("Put '{}' under key {} to cache {}", new Object[] { value, key, cache.getName() });
                final Object store = toStoreValue(value);
                final boolean added = cache.add(cacheKey, expiration, store, null);
                return added ? null : get(key);
            } catch (TimeoutException | CacheException | RuntimeException e) {
                logOrThrow(e, "An error has ocurred for cache {} and key {}", getName(), cacheKey, e);
            }
        } else {
            LOGGER.info("Cannot put to cache {} because key is null", cache.getName());
        }

        return null;
    }

    @Override
    public void evict(final Object key) {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot evict key {}", cache.getName(), key);
            return;
        }

        if (key != null) {
            final String cacheKey = getKey(key);
            try {
                LOGGER.info("Evict {} from cache {}", key, cache.getName());
                cache.delete(cacheKey);
            } catch (TimeoutException | CacheException | RuntimeException e) {
                logOrThrow(e, "An error has ocurred for cache {} and key {}", getName(), cacheKey, e);
            }
        } else {
            LOGGER.info("Cannot evict from cache {} because key is null", cache.getName());
        }
    }

    @Override
    public void clear() {
        if (!cache.isEnabled()) {
            LOGGER.warn("Cache {} is disabled. Cannot clear cache.", cache.getName());
            return;
        }

        if (!allowClear) {
            LOGGER.error("Clearing cache '{}' is not allowed. To enable it set allowClear to true. "
                    + "Make sure that caches don't overlap (one memcached instance isn't used by more than one cache) "
                    + "otherwise clearing one cache will affect another.", getName());
            throw new IllegalStateException("Cannot clear cache " + getName());
        }
        try {
            LOGGER.info("Clear {}", cache.getName());
            cache.flush();
        } catch (TimeoutException | CacheException | RuntimeException e) {
            logOrThrow(e, "An error has ocurred for cache {}", getName(), e);
        }
    }

    private Object getValue(Object key) {
        final String cacheKey = getKey(key);
        Object value = null;

        try {
            value = cache.get(cacheKey, null);
        } catch (TimeoutException | CacheException | RuntimeException e) {
            logOrThrow(e, "An error has ocurred for cache {} and key {}", getName(), cacheKey, e);
        }

        return value;
    }

    private String getKey(final Object key) {
        return key.toString();
    }

    private <T> T loadValue(final Object key, final Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }
    
    private Object toStoreValue(final Object value) {
        return value == null ? PertinentNegativeNull.NULL : value;
    }
    
    private void logOrThrow(final RuntimeException e, final String message, final Object ... args) {
        LOGGER.warn(message, args);
        if (!muteExceptions) {
            throw e;
        }
    }
    
    private void logOrThrow(final Exception e, final String message, final Object ... args) {
        if (RuntimeException.class.isInstance(e)) {
            logOrThrow(RuntimeException.class.cast(e), message, args);
        } else {
            logOrThrow(new WrappedCacheException(e), message, args);
        }
    }
    
    /**
     * A custom exception class because org.springframework.cache.Cache.ValueRetrievalException cannot be used 
     * as it is declared in Spring 4.3 but SSM shouldn't required newest Spring version.
     * 
     * @author Jakub Białek
     * @since 3.6.1
     *
     */
    public static class ValueRetrievalException extends RuntimeException {
        
        private static final long serialVersionUID = 7777933028628648800L;

        public ValueRetrievalException(Object key, Callable<?> loader, Throwable ex) {
            super(String.format("Value for key '%s' could not be loaded using '%s'", key, loader), ex);
        }
        
    }
}
