/*
 * Copyright (c) 2008-2014 Nelson Carpentier, Jakub Białek
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

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
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
import com.google.code.ssm.transcoders.JavaTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoder;

/**
 * Creates cache using provider factory and connection configuration.
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Getter
public class CacheFactory implements AddressChangeListener, FactoryBean<Cache>, InitializingBean, DisposableBean {

    public static final String DISABLE_CACHE_PROPERTY = "ssm.cache.disable";

    public static final String DISABLE_CACHE_PROPERTY_VALUE = "true";

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFactory.class);

    @Setter
    private CacheConfiguration configuration = new CacheConfiguration();

    @Setter
    private AddressProvider addressProvider;

    @Setter
    private CacheClientFactory cacheClientFactory;

    @Setter
    private String cacheName = AnnotationConstants.DEFAULT_CACHE_NAME;

    @Setter
    private Collection<String> cacheAliases = Collections.emptyList();

    private CacheImpl cache;

    @Setter
    private AddressChangeNotifier addressChangeNotifier;

    @Setter
    private SerializationType defaultSerializationType = SerializationType.PROVIDER;

    @Setter
    private JsonTranscoder jsonTranscoder;

    @Setter
    private JavaTranscoder javaTranscoder;

    @Setter
    private CacheTranscoder customTranscoder;

    @Setter
    private boolean initializeTranscoders = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(configuration, "'configuration' cannot be null");
        Assert.notNull(addressProvider, "'addressProvider' is required and cannot be null");
        Assert.notNull(cacheClientFactory, "'cacheClientFactory' is required and cannot be null");
        Assert.notNull(cacheName, "'cacheName' cannot be null");
        Assert.notNull(defaultSerializationType, "'defaultSerializationType' cannot be null");

        if (initializeTranscoders) {
            if (jsonTranscoder == null) {
                jsonTranscoder = new JsonTranscoder(new JsonObjectMapper());
            }
            if (javaTranscoder == null) {
                javaTranscoder = new JavaTranscoder();
            }
        }

        validateTranscoder(SerializationType.JSON, jsonTranscoder, "jsonTranscoder");
        validateTranscoder(SerializationType.JAVA, javaTranscoder, "javaTranscoder");
        validateTranscoder(SerializationType.CUSTOM, customTranscoder, "customTranscoder");

        if (addressChangeNotifier != null) {
            addressChangeNotifier.setAddressChangeListener(this);
        }
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
    public void destroy() throws Exception {
        if (cache != null) {
            LOGGER.info("Shutdowning cache {}", cacheName);
            cache.shutdown();
        }
    }

    @Override
    public void changeAddresses(final List<InetSocketAddress> addresses) {
        if (isCacheDisabled()) {
            LOGGER.warn("Cache {} is disabled", cacheName);
            return;
        }

        try {
            LOGGER.info("Creating new memcached client for cache {} with new addresses: {}", cacheName, addresses);
            CacheClient memcacheClient = createClient(addresses);
            LOGGER.info("New memcached client for cache {} was created with addresses: {}", cacheName, addresses);
            cache.changeCacheClient(memcacheClient);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Cannot change memcached client to new one with addresses %s", addresses), e);
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

        if (isCacheDisabled()) {
            LOGGER.warn("Cache {} is disabled", cacheName);
            return null;
        }

        if (cache != null) {
            throw new IllegalStateException(String.format("This factory has already created memcached client fro cache %s", cacheName));
        }

        if (this.configuration == null) {
            throw new RuntimeException(String.format("The MemcachedConnectionBean for cache %s must be defined!", cacheName));
        }

        List<InetSocketAddress> addrs = addressProvider.getAddresses();
        cache = new CacheImpl(cacheName, cacheAliases, createClient(addrs), defaultSerializationType, jsonTranscoder, javaTranscoder,
                customTranscoder, new CacheProperties(configuration.isUseNameAsKeyPrefix(), configuration.getKeyPrefixSeparator()));

        return cache;
    }

    private CacheClient createClient(final List<InetSocketAddress> addrs) throws IOException {
        if (addrs == null || addrs.isEmpty()) {
            throw new IllegalArgumentException(String.format("No memcached addresses specified for cache %s", cacheName));
        }

        return cacheClientFactory.create(addrs, configuration);
    }

    private boolean isCacheDisabled() {
        return DISABLE_CACHE_PROPERTY_VALUE.equals(System.getProperty(DISABLE_CACHE_PROPERTY));
    }

    private void validateTranscoder(final SerializationType serializationType, final CacheTranscoder cacheTranscoder,
            final String transcoderName) {
        if (defaultSerializationType == serializationType) {
            Assert.notNull(cacheTranscoder,
                    String.format("'%s' cannot be null if default serialization type is set to %s", transcoderName, serializationType));
        }
    }

}
