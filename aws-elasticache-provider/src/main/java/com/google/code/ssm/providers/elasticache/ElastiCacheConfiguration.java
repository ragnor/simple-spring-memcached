/*
 * Copyright (c) 2014-2019 Jakub Białek
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

import java.util.Collection;

import javax.net.ssl.SSLContext;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.FailureMode;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.metrics.MetricCollector;
import net.spy.memcached.metrics.MetricType;
import net.spy.memcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheConfiguration;

/**
 * Allows to set provider specific settings. If property is not set (null) default value defined by provider will be
 * used. Description of each property can be found in {@link ConnectionFactoryBuilder} class.
 * 
 * @author Jakub Białek
 * @since 3.5.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ElastiCacheConfiguration extends CacheConfiguration {

    /**
     * @see ConnectionFactoryBuilder#setDaemon(boolean)
     */
    private Boolean daemon;

    /**
     * @see ConnectionFactoryBuilder#setFailureMode(FailureMode)
     */
    private FailureMode failureMode;

    /**
     * @see ConnectionFactoryBuilder#setHashAlg(HashAlgorithm)
     */
    private HashAlgorithm hashAlg;

    /**
     * @see ConnectionFactoryBuilder#setLocatorType(Locator)
     */
    private Locator locatorType;

    /**
     * @see ConnectionFactoryBuilder#setMaxReconnectDelay(long)
     */
    private Long maxReconnectDelay;

    /**
     * @see ConnectionFactoryBuilder#setOpQueueMaxBlockTime(long)
     */
    private Long opQueueMaxBlockTime;

    /**
     * @see ConnectionFactoryBuilder#setOpTimeout(long)
     * @since 4.0.0
     */
    private Long opTimeout;

    /**
     * @see ConnectionFactoryBuilder#setReadBufferSize(int)
     */
    private Integer readBufferSize;

    /**
     * @see ConnectionFactoryBuilder#setShouldOptimize(boolean)
     */
    private Boolean shouldOptimize;

    /**
     * @see ConnectionFactoryBuilder#setTimeoutExceptionThreshold(int)
     */
    private Integer timeoutExceptionThreshold;

    /**
     * @see ConnectionFactoryBuilder#setUseNagleAlgorithm(boolean)
     */
    private Boolean useNagleAlgorithm;

    /**
     * default transcoder or null if not set
     * 
     * @see ConnectionFactoryBuilder#setTranscoder(Transcoder)
     */
    private Transcoder<Object> defaultTranscoder;

    /**
     * @see ConnectionFactoryBuilder#setAuthDescriptor(AuthDescriptor)
     */
    private AuthDescriptor authDescriptor;

    /**
     * @see ConnectionFactoryBuilder#setAuthWaitTime(long)
     * @since 4.0.0
     */
    private Long authWaitTime;

    /**
     * @see ConnectionFactoryBuilder#setInitialObservers(Collection)
     */
    private Collection<ConnectionObserver> initialObservers;

    /**
     * Try to use Amazon ElastiCache Auto Discovery, more info:
     * http://docs.amazonwebservices.com/AmazonElastiCache/latest/UserGuide/AutoDiscovery.html. If set to true all other
     * client settings will be ignored, this is how AWS ElastiCache client works with auto discovery feature.
     */
    private Boolean useAutoDiscovery;

    /**
     * If set to {@link ClientMode#Dynamic} the client is initialized using configuration endpoint and some of defined 
     * client settings (like timeout) might be ignored. 
     * 
     * @see ConnectionFactoryBuilder#setClientMode(ClientMode)
     */
    private ClientMode clientMode;

    /**
     * @see ConnectionFactoryBuilder#setEnableMetrics(MetricType)
     * @since 4.0.0
     */
    private MetricType enableMetricType;

    /**
     * @see ConnectionFactoryBuilder#setMetricCollector(MetricCollector)
     * @since 4.0.0
     */
    private MetricCollector metricCollector;

    private SSLContext sslContext;

    private String hostnameForTlsVerification;

    private Boolean skipTlsHostnameVerification;

}
