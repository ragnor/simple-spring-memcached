/*
 * Copyright (c) 2008-2012 Nelson Carpentier, Jakub Białek
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
 */

package com.google.code.ssm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.google.code.ssm.api.AnnotationConstants;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.config.AddressChangeListener;
import com.google.code.ssm.config.AddressChangeNotifier;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.mapper.JsonObjectMapper;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoder;

/**
 * Creates cache using provider factory and connection configuration.
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
public class CacheFactory implements AddressChangeListener, FactoryBean<Cache>, InitializingBean {

    public static final String DISABLE_CACHE_PROPERTY = "ssm.cache.disable";

    public static final String DISABLE_CACHE_PROPERTY_VALUE = "true";

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFactory.class);

    private CacheConfiguration configuration = new CacheConfiguration();

    private AddressProvider addressProvider;

    private CacheClientFactory cacheClientFactory;

    private String cacheName = AnnotationConstants.DEFAULT_CACHE_NAME;

    private Collection<String> cacheAliases = Collections.emptyList();

    private CacheImpl cache;

    private AddressChangeNotifier addressChangeNotifier;

    private SerializationType defaultSerializationType = SerializationType.PROVIDER;

    private JsonTranscoder jsonTranscoder = new JsonTranscoder(new JsonObjectMapper());

    private CacheTranscoder customTranscoder;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(configuration, "'configuration' cannot be null");
        Assert.notNull(addressProvider, "'addressProvider' is required and cannot be null");
        Assert.notNull(cacheClientFactory, "'cacheClientFactory' is required and cannot be null");
        Assert.notNull(cacheName, "'cacheName' cannot be null");
        Assert.notNull(defaultSerializationType, "'defaultSerializationType' cannot be null");
        Assert.notNull(jsonTranscoder, "'jsonTranscoder' cannot be null");

        if (addressChangeNotifier != null) {
            addressChangeNotifier.setAddressChangeListener(this);
        }

        if (defaultSerializationType == SerializationType.CUSTOM) {
            Assert.notNull(customTranscoder, "'customTranscoder' cannot be null if default serialization type is set to CUSTOM");
        }
    }

    public void setConfiguration(final CacheConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setAddressProvider(final AddressProvider addressProvider) {
        this.addressProvider = addressProvider;
    }

    public void setCacheClientFactory(final CacheClientFactory cacheClientFactory) {
        this.cacheClientFactory = cacheClientFactory;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public void setCacheAliases(final Collection<String> cacheAliases) {
        this.cacheAliases = cacheAliases;
    }

    public void setAddressChangeNotifier(final AddressChangeNotifier addressChangeNotifier) {
        this.addressChangeNotifier = addressChangeNotifier;
    }

    public void setDefaultSerializationType(final SerializationType serializationType) {
        this.defaultSerializationType = serializationType;
    }

    public void setJsonTranscoder(final JsonTranscoder jsonTranscoder) {
        this.jsonTranscoder = jsonTranscoder;
    }

    public void setCustomTranscoder(final CacheTranscoder transcoder) {
        this.customTranscoder = transcoder;
    }

    @Override
    public Cache getObject() throws Exception {
        return createCache();
    }

    @Override
    public Class<?> getObjectType() {
        return Cache.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void changeAddresses(final List<InetSocketAddress> addresses) {
        if (DISABLE_CACHE_PROPERTY_VALUE.equals(System.getProperty(DISABLE_CACHE_PROPERTY))) {
            LOGGER.warn("Cache disabled");
            return;
        }

        try {
            LOGGER.info("Creating new memcached client for new addresses: {}", addresses);
            CacheClient memcacheClient = createClient(addresses);
            LOGGER.info("New memcached client created with addresses: {}", addresses);
            cache.changeCacheClient(memcacheClient);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Cannot change memcached client to new one with addresses " + addresses, e);
            }
        }
    }

    /**
     * Only one cache is created.
     * 
     * @return cache
     * @throws IOException
     */
    protected Cache createCache() throws IOException {
        // this factory creates only one single cache and return it if someone invoked this method twice or
        // more

        if (DISABLE_CACHE_PROPERTY_VALUE.equals(System.getProperty(DISABLE_CACHE_PROPERTY))) {
            LOGGER.warn("Cache disabled");
            return null;
        }

        if (cache != null) {
            throw new IllegalStateException("This factory has already created memcached client");
        }

        if (this.configuration == null) {
            throw new RuntimeException("The MemcachedConnectionBean must be defined!");
        }

        List<InetSocketAddress> addrs = addressProvider.getAddresses();
        cache = new CacheImpl(cacheName, cacheAliases, createClient(addrs), defaultSerializationType, jsonTranscoder, customTranscoder);

        return cache;
    }

    private CacheClient createClient(final List<InetSocketAddress> addrs) throws IOException {
        if (addrs == null || addrs.isEmpty()) {
            throw new IllegalArgumentException("No memcached addresses specified");
        }

        return cacheClientFactory.create(addrs, configuration);
    }

}
