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
        Assert.notEmpty(caches, "A collection of caches is required and cannot be empty");
        this.cacheMap.clear();

        // preserve the initial order of the cache names
        for (SSMCache cache : caches) {
            addCache(cache.getName(), cache);

            // use aliases if enabled
            if (cache.isRegisterAliases() && !CollectionUtils.isEmpty(cache.getCache().getAliases())) {
                for (String alias : cache.getCache().getAliases()) {
                    addCache(alias, cache);
                }
            }
        }
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

    protected void addCache(final String name, final Cache cache) {
        this.cacheMap.put(name, cache);
        this.cacheNames.add(name);
    }

}
