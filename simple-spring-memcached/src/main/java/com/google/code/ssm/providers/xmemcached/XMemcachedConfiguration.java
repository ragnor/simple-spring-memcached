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

package com.google.code.ssm.providers.xmemcached;

import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;

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

    public void setMaxAwayTime(final Integer maxAwayTime) {
        this.maxAwayTime = maxAwayTime;
    }

    public Integer getMaxAwayTime() {
        return maxAwayTime;
    }

    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(final Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Boolean getFailureMode() {
        return failureMode;
    }

    public void setFailureMode(final Boolean failureMode) {
        this.failureMode = failureMode;
    }

    public Map<SocketOption<?>, Object> getSocketOptions() {
        return socketOptions;
    }

    public void setSocketOptions(final Map<SocketOption<?>, Object> socketOptions) {
        this.socketOptions = socketOptions;
    }

    public Long getHealSessionInterval() {
        return healSessionInterval;
    }

    public void setHealSessionInterval(final Long healSessionInterval) {
        this.healSessionInterval = healSessionInterval;
    }

    public Integer getMergeFactor() {
        return mergeFactor;
    }

    public void setMergeFactor(final Integer mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    public Boolean getOptimizeGet() {
        return optimizeGet;
    }

    public void setOptimizeGet(final Boolean optimizeGet) {
        this.optimizeGet = optimizeGet;
    }

    public Boolean getOptimizeMergeBuffer() {
        return optimizeMergeBuffer;
    }

    public void setOptimizeMergeBuffer(final Boolean optimizeMergeBuffer) {
        this.optimizeMergeBuffer = optimizeMergeBuffer;
    }

    public Boolean getEnableHeartBeat() {
        return enableHeartBeat;
    }

    public void setEnableHeartBeat(final Boolean enableHeartBeat) {
        this.enableHeartBeat = enableHeartBeat;
    }

    public Boolean getPrimitiveAsString() {
        return primitiveAsString;
    }

    public void setPrimitiveAsString(final Boolean primitiveAsString) {
        this.primitiveAsString = primitiveAsString;
    }

    public Boolean getSanitizeKeys() {
        return sanitizeKeys;
    }

    public void setSanitizeKeys(final Boolean sanitizeKeys) {
        this.sanitizeKeys = sanitizeKeys;
    }

}
