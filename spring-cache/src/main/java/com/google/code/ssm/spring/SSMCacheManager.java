/*
 * Copyright (c) 2012 Jakub Białek
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
import java.util.LinkedHashSet;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

/**
 * 
 * @author Jakub Białek
 * @since 2.1.0
 * 
 */
public class SSMCacheManager extends AbstractCacheManager {

    private Collection<com.google.code.ssm.Cache> internalCaches;

    private Integer expiration;

    private boolean allowClear = false;

    public void setCaches(final Collection<com.google.code.ssm.Cache> caches) {
        this.internalCaches = caches;
    }

    public void setExpiration(final int expiration) {
        this.expiration = expiration;
    }

    public void setAllowClear(final boolean allowClear) {
        this.allowClear = allowClear;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        Assert.notNull(expiration, "A expiration is required");
        Assert.notEmpty(internalCaches, "A collection caches is required and cannot be empty");

        Collection<Cache> caches = new LinkedHashSet<Cache>(internalCaches.size());
        for (com.google.code.ssm.Cache internalCache : internalCaches) {
            caches.add(new SSMCache(internalCache, expiration, allowClear));
        }
        return caches;
    }

}
