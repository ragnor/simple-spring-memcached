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

import lombok.Getter;
import lombok.Setter;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

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
public class SSMCacheManager extends AbstractCacheManager {

    @Getter
    @Setter
    private Collection<SSMCache> caches;

    @Override
    protected Collection<? extends Cache> loadCaches() {
        Assert.notEmpty(caches, "A collection of caches is required and cannot be empty");

        return caches;
    }

}
