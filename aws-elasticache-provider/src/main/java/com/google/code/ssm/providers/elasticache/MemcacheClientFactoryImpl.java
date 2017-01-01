/*
 * Copyright (c) 2014-2016 Jakub Białek
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

package com.google.code.ssm.providers.elasticache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.MemcachedClient;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;

/**
 * 
 * @author Jakub Białek
 * @since 3.5.0
 * 
 */
public class MemcacheClientFactoryImpl implements CacheClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcacheClientFactoryImpl.class);
    
    private ConnectionFactory connectionFactory;

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) throws IOException {
        // currently its works because this factory creates clients with the same connection settings, only memcached
        // addresses can be changed
        if (connectionFactory == null) {
            ElastiCacheConfiguration elasticacheConf = null;
            if (conf instanceof ElastiCacheConfiguration) {
                elasticacheConf = (ElastiCacheConfiguration) conf;
            }
            
            if (elasticacheConf != null && Boolean.TRUE.equals(elasticacheConf.getUseAutoDiscovery())) {
                // there is no way to use custom client settings and auto discovery together
                LOGGER.info("All cache settings will be ignored because useAutoDiscovery is true");
                return new MemcacheClientWrapper(new MemcachedClient(addrs));
            }
            
            
            ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();

            if (conf.isConsistentHashing()) {
                builder.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH);
                builder.setLocatorType(Locator.CONSISTENT);
            }

            builder.setProtocol(conf.isUseBinaryProtocol() ? Protocol.BINARY : Protocol.TEXT);
            if (conf.getOperationTimeout() != null) {
                builder.setOpTimeout(conf.getOperationTimeout());
            }

            if (elasticacheConf != null) {
                setProviderSpecificSettings(builder, elasticacheConf);
            }

            connectionFactory = builder.build();
        }

        return new MemcacheClientWrapper(new MemcachedClient(connectionFactory, addrs));
    }

    private void setProviderSpecificSettings(final ConnectionFactoryBuilder builder, final ElastiCacheConfiguration conf) {
        if (conf.getDaemon() != null) {
            builder.setDaemon(conf.getDaemon());
        }

        if (conf.getFailureMode() != null) {
            builder.setFailureMode(conf.getFailureMode());
        }

        if (conf.getHashAlg() != null) {
            builder.setHashAlg(conf.getHashAlg());
        }

        if (conf.getLocatorType() != null) {
            builder.setLocatorType(conf.getLocatorType());
        }

        if (conf.getMaxReconnectDelay() != null) {
            builder.setMaxReconnectDelay(conf.getMaxReconnectDelay());
        }

        if (conf.getOpQueueMaxBlockTime() != null) {
            builder.setOpQueueMaxBlockTime(conf.getOpQueueMaxBlockTime());
        }
        
        if (conf.getOpTimeout() != null) {
            builder.setOpTimeout(conf.getOpTimeout());
        }

        if (conf.getReadBufferSize() != null) {
            builder.setReadBufferSize(conf.getReadBufferSize());
        }

        if (conf.getShouldOptimize() != null) {
            builder.setShouldOptimize(conf.getShouldOptimize());
        }

        if (conf.getTimeoutExceptionThreshold() != null) {
            builder.setTimeoutExceptionThreshold(conf.getTimeoutExceptionThreshold());
        }

        if (conf.getUseNagleAlgorithm() != null) {
            builder.setUseNagleAlgorithm(conf.getUseNagleAlgorithm());
        }

        if (conf.getDefaultTranscoder() != null) {
            builder.setTranscoder(conf.getDefaultTranscoder());
        }

        if (conf.getAuthDescriptor() != null) {
            builder.setAuthDescriptor(conf.getAuthDescriptor());
        }
        
        if (conf.getAuthWaitTime() != null) {
            builder.setAuthWaitTime(conf.getAuthWaitTime());
        }

        if (conf.getInitialObservers() != null) {
            builder.setInitialObservers(conf.getInitialObservers());
        }
        
        if (conf.getClientMode() != null) {
            builder.setClientMode(conf.getClientMode());
        }
        
        if (conf.getEnableMetricType() != null) {
            builder.setEnableMetrics(conf.getEnableMetricType());
        }
        
        if (conf.getMetricCollector() != null) {
            builder.setMetricCollector(conf.getMetricCollector());
        }

    }

}
