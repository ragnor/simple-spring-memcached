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

package com.google.code.ssm.providers.xmemcached;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedClientStateListener;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.yanf4j.config.Configuration;
import com.google.code.yanf4j.core.SocketOption;

/**
 * 
 * Allows to set provider specific settings. If property is not set (null) default value defined by provider will be
 * used. Description of each property can be found in {@link MemcachedClientBuilder} and {@link MemcachedClient}
 * classes.
 * 
 * @author Jakub Białek
 * @version 2.0.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XMemcachedConfiguration extends CacheConfiguration {

    /**
     * @see MemcachedClient#addStateListener(MemcachedClientStateListener)
     * @see ReconnectListener
     */
    private Integer maxAwayTime;

    /**
     * @see MemcachedClientBuilder#setConnectionPoolSize(int)
     */
    private Integer connectionPoolSize;

    /**
     * @see MemcachedClientBuilder#setConfiguration(Configuration)
     */
    private Configuration configuration;

    /**
     * @see MemcachedClientBuilder#setFailureMode(boolean)
     */
    private Boolean failureMode;

    /**
     * @see MemcachedClientBuilder#setSocketOption(SocketOption, Object)
     */
    private Map<SocketOption<?>, Object> socketOptions;

    /**
     * @see MemcachedClientBuilder#setHealSessionInterval(long)
     */
    private Long healSessionInterval;

    /**
     * @see MemcachedClient#setMergeFactor(int)
     */
    private Integer mergeFactor;

    /**
     * @see MemcachedClient#setOptimizeGet(boolean)
     */
    private Boolean optimizeGet;

    /**
     * @see MemcachedClient#setOptimizeMergeBuffer(boolean)
     */
    private Boolean optimizeMergeBuffer;

    /**
     * @see MemcachedClient#setEnableHeartBeat(boolean)
     */
    private Boolean enableHeartBeat;

    /**
     * @see MemcachedClient#setPrimitiveAsString(boolean)
     */
    private Boolean primitiveAsString;

    /**
     * @see MemcachedClientBuilder#setSanitizeKeys(boolean)
     */
    private Boolean sanitizeKeys;

    /**
     * default transcoder or null if not set
     * 
     * @see MemcachedClient#setTranscoder(Transcoder)
     * @since 3.0.0
     */
    private Transcoder<?> defaultTranscoder;

    /**
     * @see MemcachedClientBuilder#setConnectTimeout(long)
     * @since 3.2.0
     */
    private Long connectionTimeout;

    /**
     * @see MemcachedClientBuilder#setMaxQueuedNoReplyOperations(int)
     * @since 3.2.0
     */
    private Integer maxQueuedNoReplyOperations;

    /**
     * @see MemcachedClientBuilder#setEnableHealSession(boolean)
     * @since 3.2.0
     */
    private Boolean enableHealSession;

    /**
     * @see MemcachedClientBuilder#setAuthInfoMap(Map)
     * @since 3.2.0
     */
    private Map<InetSocketAddress, AuthInfo> authInfoMap;

    /**
     * @see MemcachedClientBuilder#setStateListeners(List)
     * @since 3.2.0
     */
    private List<MemcachedClientStateListener> stateListeners;

    /**
     * @see XMemcachedClientBuilder#XMemcachedClientBuilder(List, int[])
     * @since 3.3.0
     */
    private int[] weights;

}
