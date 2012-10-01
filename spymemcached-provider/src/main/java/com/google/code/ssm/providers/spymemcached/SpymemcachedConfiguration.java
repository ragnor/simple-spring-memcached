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

package com.google.code.ssm.providers.spymemcached;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.FailureMode;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheConfiguration;

/**
 * Allows to set provider specific settings. If property is not set (null) default value defined by provider will be
 * used. Description of each property can be found in {@link ConnectionFactoryBuilder} class.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpymemcachedConfiguration extends CacheConfiguration {

    private Boolean daemon;

    private FailureMode failureMode;

    private HashAlgorithm hashAlg;

    private Locator locatorType;

    private Long maxReconnectDelay;

    private Long opQueueMaxBlockTime;

    private Integer readBufferSize;

    private Boolean shouldOptimize;

    private Integer timeoutExceptionThreshold;

    private Boolean useNagleAlgorithm;

    /**
     * default transcoder or null if not set
     * 
     * @since 3.0.0
     */
    private Transcoder<Object> defaultTranscoder;

}
