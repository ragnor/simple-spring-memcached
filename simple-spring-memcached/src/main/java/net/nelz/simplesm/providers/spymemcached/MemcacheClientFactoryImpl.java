package net.nelz.simplesm.providers.spymemcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import net.nelz.simplesm.config.MemcachedConnectionBean;
import net.nelz.simplesm.providers.MemcacheClient;
import net.nelz.simplesm.providers.MemcacheClientFactory;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import net.spy.memcached.KetamaConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * 
 * @author Jakub Białek
 * 
 */
public class MemcacheClientFactoryImpl implements MemcacheClientFactory {

    private ConnectionFactory connectionFactory;

    @Override
    public MemcacheClient create(List<InetSocketAddress> addrs, MemcachedConnectionBean connectionBean) throws IOException {
        if (connectionFactory == null) {
            connectionFactory = connectionBean.isConsistentHashing() ? new KetamaConnectionFactory() : new ConnectionFactoryBuilder()
                    .setFailureMode(FailureMode.Cancel).setOpTimeout(connectionBean.getOperationTimeout()).setOpQueueMaxBlockTime(500)
                    .setMaxReconnectDelay(5000).setProtocol(connectionBean.isUseBinaryProtocol() ? Protocol.BINARY : Protocol.TEXT).build();
        }

        return new MemcacheClientWrapper(new MemcachedClient(connectionFactory, addrs));
    }

}
