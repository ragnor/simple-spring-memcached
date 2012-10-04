package com.google.code.ssm.jmemcached.plugin;

import java.net.InetSocketAddress;

import org.apache.maven.plugin.AbstractMojo;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

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

            public void run() {
                daemon1.start();
            }
        });
        t.setDaemon(false);
        t.start();

        t = new Thread(new Runnable() {

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
