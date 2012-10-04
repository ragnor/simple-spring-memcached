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

package com.google.code.ssm.jmemcached.plugin;

import java.net.InetSocketAddress;

import org.apache.maven.plugin.AbstractMojo;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

/**
 * 
 * @author Jakub Białek
 * @since 3.1.0
 * 
 */
public abstract class AbstractJmemcachedMojo extends AbstractMojo {

    private static MemCacheDaemon<LocalCacheElement> daemon1;

    private static MemCacheDaemon<LocalCacheElement> daemon2;

    private static boolean started = false;

    protected void start() {
        if (started) {
            return;
        }

        started = true;

        daemon1 = build(11211);
        daemon2 = build(11212);

        System.err.println("#########2###########");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                daemon1.start();
            }
        });
        t.setDaemon(false);
        t.start();

        t = new Thread(new Runnable() {

            @Override
            public void run() {
                daemon2.start();
            }
        });
        t.setDaemon(false);
        t.start();

    }

    private static MemCacheDaemon<LocalCacheElement> build(final int port) {
        MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();

        CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 1000,
                100000);
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(false);
        daemon.setAddr(new InetSocketAddress("localhost", port));
        daemon.setIdleTime(10);
        daemon.setVerbose(true);

        return daemon;
    }

    protected void stop() {
        if (daemon1 != null && daemon1.isRunning()) {
            daemon1.stop();
        }

        if (daemon2 != null && daemon2.isRunning()) {
            daemon2.stop();
        }

        started = false;
    }

}
