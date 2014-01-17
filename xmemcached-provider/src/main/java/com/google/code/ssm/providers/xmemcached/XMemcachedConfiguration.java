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

    private Integer maxAwayTime;

    private Integer connectionPoolSize;

    private Configuration configuration;

    private Boolean failureMode;

    private Map<SocketOption<?>, Object> socketOptions;

    private Long healSessionInterval;

    private Integer mergeFactor;

    private Boolean optimizeGet;

    private Boolean optimizeMergeBuffer;

    private Boolean enableHeartBeat;

    private Boolean primitiveAsString;

    private Boolean sanitizeKeys;

    /**
     * default transcoder or null if not set
     * 
     * @since 3.0.0
     * */
    private Transcoder<?> defaultTranscoder;

    /**
     * @since 3.2.0
     */
    private Long connectionTimeout;

    /**
     * @since 3.2.0
     */
    private Integer maxQueuedNoReplyOperations;

    /**
     * @since 3.2.0
     */
    private Boolean enableHealSession;

    /**
     * @since 3.2.0
     */
    private Map<InetSocketAddress, AuthInfo> authInfoMap;

    /**
     * @since 3.2.0
     */
    private List<MemcachedClientStateListener> stateListeners;

    /**
     * @since 3.3.0
     */
    private int[] weights;

}
