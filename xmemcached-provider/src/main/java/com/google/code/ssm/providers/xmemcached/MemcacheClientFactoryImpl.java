/*
 * Copyright (c) 2010-2014 Jakub Białek
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.yanf4j.core.SocketOption;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class MemcacheClientFactoryImpl implements CacheClientFactory {

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) throws IOException {
        MemcachedClientBuilder builder = null;

        if (conf instanceof XMemcachedConfiguration) {
            int[] weights = ((XMemcachedConfiguration) conf).getWeights();
            if (weights != null && weights.length > 0) {
                builder = new XMemcachedClientBuilder(addrs, weights);
            }
        }

        if (builder == null) {
            builder = new XMemcachedClientBuilder(addrs);
        }

        if (conf.isConsistentHashing()) {
            builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        }

        if (conf.isUseBinaryProtocol()) {
            builder.setCommandFactory(new BinaryCommandFactory());
        }

        if (conf instanceof XMemcachedConfiguration) {
            setProviderBuilderSpecificSettings(builder, (XMemcachedConfiguration) conf);
        }

        MemcachedClient client = builder.build();
        if (conf.getOperationTimeout() != null) {
            client.setOpTimeout(conf.getOperationTimeout());
        }

        if (conf instanceof XMemcachedConfiguration) {
            setProviderClientSpecificSettings(client, (XMemcachedConfiguration) conf);
        }

        return new MemcacheClientWrapper(client);
    }

    private void setProviderBuilderSpecificSettings(final MemcachedClientBuilder builder, final XMemcachedConfiguration conf) {
        if (conf.getConnectionPoolSize() != null) {
            builder.setConnectionPoolSize(conf.getConnectionPoolSize());
        }

        if (conf.getConfiguration() != null) {
            builder.setConfiguration(conf.getConfiguration());
        }

        if (conf.getFailureMode() != null) {
            builder.setFailureMode(conf.getFailureMode());
        }

        if (conf.getSocketOptions() != null) {
            for (Map.Entry<SocketOption<?>, Object> entry : conf.getSocketOptions().entrySet()) {
                builder.setSocketOption(entry.getKey(), entry.getValue());
            }
        }

        if (conf.getDefaultTranscoder() != null) {
            builder.setTranscoder(conf.getDefaultTranscoder());
        }

        if (conf.getConnectionTimeout() != null) {
            builder.setConnectTimeout(conf.getConnectionTimeout());
        }

        if (conf.getMaxQueuedNoReplyOperations() != null) {
            builder.setMaxQueuedNoReplyOperations(conf.getMaxQueuedNoReplyOperations());
        }

        if (conf.getEnableHealSession() != null) {
            builder.setEnableHealSession(conf.getEnableHealSession());
        }

        if (conf.getAuthInfoMap() != null) {
            builder.setAuthInfoMap(conf.getAuthInfoMap());
        }

        if (conf.getStateListeners() != null) {
            builder.setStateListeners(conf.getStateListeners());
        }
    }

    private void setProviderClientSpecificSettings(final MemcachedClient client, final XMemcachedConfiguration conf) {
        if (conf.getMaxAwayTime() != null) {
            client.addStateListener(new ReconnectListener(conf.getMaxAwayTime()));
        }

        if (conf.getEnableHeartBeat() != null) {
            client.setEnableHeartBeat(conf.getEnableHeartBeat());
        }

        if (conf.getHealSessionInterval() != null) {
            client.setHealSessionInterval(conf.getHealSessionInterval());
        }

        if (conf.getMergeFactor() != null) {
            client.setMergeFactor(conf.getMergeFactor());
        }

        if (conf.getOptimizeGet() != null) {
            client.setOptimizeGet(conf.getOptimizeGet());
        }

        if (conf.getOptimizeMergeBuffer() != null) {
            client.setOptimizeMergeBuffer(conf.getOptimizeMergeBuffer());
        }

        if (conf.getPrimitiveAsString() != null) {
            client.setPrimitiveAsString(conf.getPrimitiveAsString());
        }

        if (conf.getSanitizeKeys() != null) {
            client.setSanitizeKeys(conf.getSanitizeKeys());
        }

    }
}
