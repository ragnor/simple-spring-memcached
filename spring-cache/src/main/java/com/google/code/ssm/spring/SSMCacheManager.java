/*
 * Copyright (c) 2012-2013 Jakub Białek
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.code.ssm.PrefixedCacheImpl;

/**
 * 
 * CacheManager backed by a Simple Spring Memcached (SSM) {@link com.google.code.ssm.Cache}. Because using Spring Cache
 * and CacheManager it is not possible to pass expiration time to backend cache, default expiration time has to be set
 * in {@link SSMCache#SSMCache(com.google.code.ssm.Cache, int, boolean)}. This expiration time is used for all store
 * requests.
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class SSMCacheManager implements CacheManager, InitializingBean {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();

    private final Set<String> cacheNames = new LinkedHashSet<String>();

    @Getter
    @Setter
    private Collection<SSMCache> caches;

    @Override
    public void afterPropertiesSet() {
        Collection<SSMCache> caches = loadCaches();
        Assert.notEmpty(caches, "loadCaches must not return an empty Collection");
        this.cacheMap.clear();

        // preserve the initial order of the cache names
        for (SSMCache cache : caches) {
            this.cacheMap.put(cache.getName(), cache);
            this.cacheNames.add(cache.getName());

            // use aliases if enabled
            if (cache.isRegisterAliases() && !CollectionUtils.isEmpty(cache.getCache().getAliases())) {
                for (String alias : cache.getCache().getAliases()) {
                    this.cacheMap.put(alias, cache);
                    this.cacheNames.add(alias);
                }
            }
        }
    }

    protected final void addCache(final Cache cache) {
        this.cacheMap.put(cache.getName(), cache);
        this.cacheNames.add(cache.getName());
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheNames);
    }

    @Override
    public SSMCache getCache(final String name) {
        SSMCache cache = (SSMCache) this.cacheMap.get(name);
        if (cache == null) {
            return null;
        }

        if (cache.getCache().getProperties().isUseNameAsKeyPrefix()) {
            return new SSMCache(new PrefixedCacheImpl(cache.getCache(), name, cache.getCache().getProperties().getKeyPrefixSeparator()),
                    cache.getExpiration(), cache.isAllowClear());
        }

        return cache;
    }

    /**
     * Load the caches for this cache manager. Occurs at startup. The returned collection must not be null.
     */
    protected Collection<SSMCache> loadCaches() {
        Assert.notEmpty(caches, "A collection of caches is required and cannot be empty");

        return caches;
    }

}
