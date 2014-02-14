/* Copyright (c) 2012-2014 Jakub Białek
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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import net.spy.memcached.FailureMode;
import net.spy.memcached.metrics.MetricType;
import net.spy.memcached.metrics.NoopMetricCollector;

import org.junit.Before;
import org.junit.Test;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheConfiguration;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class MemcacheClientFactoryImplTest {

    private MemcacheClientFactoryImpl factory;

    private List<InetSocketAddress> addrs;

    @Before
    public void setUp() {
        factory = new MemcacheClientFactoryImpl();
        addrs = Collections.singletonList(new InetSocketAddress(12345));
    }

    @Test
    public void createWithBaseConf() throws IOException {
        CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(true);
        conf.setOperationTimeout(1000);
        conf.setUseBinaryProtocol(false);
        CacheClient client = factory.create(addrs, conf);
        assertNotNull(client);
        client.shutdown();
    }

    @Test
    public void createWithSpecificConf() throws IOException {
        SpymemcachedConfiguration conf = new SpymemcachedConfiguration();
        conf.setConsistentHashing(true);
        conf.setOperationTimeout(1000);
        conf.setUseBinaryProtocol(false);
        conf.setFailureMode(FailureMode.Cancel);
        conf.setShouldOptimize(true);
        conf.setMaxReconnectDelay(1000L);
        conf.setTimeoutExceptionThreshold(100);
        conf.setUseNagleAlgorithm(false);
        conf.setMetricCollector(new NoopMetricCollector());
        conf.setMetricType(MetricType.PERFORMANCE);
        CacheClient client = factory.create(addrs, conf);
        assertNotNull(client);
        client.shutdown();
    }

}
