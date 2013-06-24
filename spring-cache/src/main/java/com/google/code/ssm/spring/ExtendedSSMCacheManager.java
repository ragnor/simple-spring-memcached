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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

/**
 * CacheManager backed by a Simple Spring Memcached (SSM) {@link com.google.code.ssm.Cache}. Spring Cache and
 * CacheManager doesn't support configuring expiration time per method (there is no dedicated parameter in cache
 * annotation to pass expiration). This extension of {@link SSMCacheManager} overcomes this limitation and allow to pass
 * expiration time as a part of cache name. To define custom expiration on method as a cache name use concatenation of
 * specific cache name, separator and expiration e.g.
 * 
 * <pre>
 * public class UserDAO {
 * 
 *     // cache name: userCache, expiration: 300s
 *     &#064;Cacheable(&quot;userCache#300&quot;)
 *     public User getUser(String name) {
 * 
 *     }
 * }
 * </pre>
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class ExtendedSSMCacheManager extends SSMCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedSSMCacheManager.class);

    private char separator = '#';

    @Override
    public Cache getCache(final String name) {
        // try to get cache by name
        Cache cache = super.getCache(name);
        if (cache != null) {
            return cache;
        }

        // there's no cache which has given name
        // find separator in cache name
        int index = name.lastIndexOf(getSeparator());
        if (index < 0) {
            return null;
        }

        // split name by the separator
        String cacheName = name.substring(0, index);
        cache = super.getCache(cacheName);
        if (cache == null) {
            return null;
        }

        // get expiration from name
        Integer expiration = getExpiration(name, index);
        if (expiration == null || expiration < 0) {
            LOGGER.warn("Default expiration time will be used for cache '{}' because cannot parse '{}'", cacheName, name);
            return cache;
        }

        return new SSMCache((SSMCache) cache, expiration);
    }

    public char getSeparator() {
        return separator;
    }

    /**
     * Char that separates cache name and expiration time, default: #.
     * 
     * @param separator
     */
    public void setSeparator(final char separator) {
        this.separator = separator;
    }

    private Integer getExpiration(final String name, final int separatorIndex) {
        Integer expiration = null;
        String expirationAsString = name.substring(separatorIndex + 1);
        try {
            expiration = Integer.parseInt(expirationAsString);
        } catch (NumberFormatException ex) {
            LOGGER.error(String.format("Cannnot separate expiration time from cache: '%s'", name), ex);
        }

        return expiration;
    }

}
