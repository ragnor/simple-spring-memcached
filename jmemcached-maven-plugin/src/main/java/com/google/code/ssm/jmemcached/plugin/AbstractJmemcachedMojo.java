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

package com.google.code.ssm.jmemcached.plugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 
     * @parameter expression="${jmemcached.disable}" default-value="false"
     */
    protected boolean disabled;

    /**
     * 
     * @parameter
     */
    protected List<Server> servers;

    private static boolean started = false;

    private static List<MemCacheDaemon<LocalCacheElement>> daemons;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(final List<Server> servers) {
        this.servers = servers;
    }

    protected void start() {
        if (disabled || servers == null) {
            getLog().info("Skipping starting jmemcached daemons");
            return;
        }

        getLog().info("Startng jmemcached daemons: " + servers);
        if (started) {
            getLog().info("Jmemcached daemons are already started");
            return;
        }

        MemCacheDaemon<LocalCacheElement> daemon;
        daemons = new ArrayList<MemCacheDaemon<LocalCacheElement>>();
        for (Server server : servers) {
            getLog().debug("Creating memcached: " + server);
            daemon = build(server);
            getLog().debug("Starting memcached: " + server);
            daemon.start();
            getLog().info("Memcached has been started: " + server);
            daemons.add(daemon);
        }

        started = true;

        getLog().info("Jmemcached daemons have been started");
    }

    protected void stop() {
        if (disabled) {
            return;
        }

        getLog().info("Stoping jmemcached daemons");

        for (MemCacheDaemon<LocalCacheElement> daemon : daemons) {
            daemon.stop();
        }

        started = false;
        getLog().info("Jmemcached daemons have been stopped");
    }

    private MemCacheDaemon<LocalCacheElement> build(final Server server) {
        MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();

        CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO,
                server.getMaximumCapacity(), server.getMaximumMemoryCapacity());
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(false);
        daemon.setAddr(new InetSocketAddress("localhost", server.getPort()));

        return daemon;
    }

}
