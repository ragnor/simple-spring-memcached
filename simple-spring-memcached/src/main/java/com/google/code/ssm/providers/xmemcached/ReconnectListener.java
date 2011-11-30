/*
 * Copyright (c) 2010-2011 Jakub Białek
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientStateListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
class ReconnectListener implements MemcachedClientStateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectListener.class);

    private Map<InetSocketAddress, Long> removedServers = new HashMap<InetSocketAddress, Long>();

    /**
     * Max time in seconds while memcached server can be absent and won't be flushed
     */
    private int maxAwayTime;

    ReconnectListener(int maxAwayTime) {
        this.maxAwayTime = maxAwayTime;
    }

    @Override
    public void onConnected(final MemcachedClient memcachedClient, final InetSocketAddress inetSocketAddress) {
        Long removedTime = removedServers.get(inetSocketAddress);

        if (removedTime != null && System.currentTimeMillis() - removedTime >= TimeUnit.SECONDS.toMillis(maxAwayTime)) {
            LOGGER.info("Memcached server {} is back and will be flushed", inetSocketAddress);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    try {
                        LOGGER.info("Flushing on memcached server {}", inetSocketAddress);
                        memcachedClient.flushAll(inetSocketAddress);
                        LOGGER.info("Memcached server {} flushed successfuly", inetSocketAddress);
                    } catch (Exception e) {
                        LOGGER.error("An error occured while flushing " + inetSocketAddress.toString(), e);
                    }
                }
            }).start();
        }

        removedServers.remove(inetSocketAddress);
    }

    @Override
    public void onDisconnected(MemcachedClient memcachedClient, InetSocketAddress inetSocketAddress) {
        removedServers.put(inetSocketAddress, System.currentTimeMillis());
    }

    @Override
    public void onException(MemcachedClient memcachedClient, Throwable throwable) {

    }

    @Override
    public void onShutDown(MemcachedClient memcachedClient) {

    }

    @Override
    public void onStarted(MemcachedClient memcachedClient) {

    }

}
