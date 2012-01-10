/* Copyright (c) 2010-2012 Jakub Białek
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;

import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.Transcoder;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class MemcacheClientWrapperTest {

    private MemcachedClientIF client;

    private MemcacheClientWrapper clientWrapper;

    @Before
    public void setUp() {
        client = getMock();
        clientWrapper = new MemcacheClientWrapper(client);
    }

    @Test
    public void addStringIntObject() throws TimeoutException, CacheException {
        EasyMock.expect(client.add("test", 1000, "value")).andReturn(getFuture(true));
        EasyMock.replay(client);
        assertTrue(clientWrapper.add("test", 1000, "value"));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addStringIntTMemcacheTranscoderOfT() throws TimeoutException, CacheException {
        CacheTranscoder<String> transcoder = EasyMock.createMock(CacheTranscoder.class);
        EasyMock.expect(client.add(EasyMock.eq("test"), EasyMock.eq(1000), EasyMock.eq("value"), EasyMock.anyObject(Transcoder.class)))
                .andReturn(getFuture(true));
        EasyMock.replay(client, transcoder);
        assertTrue(clientWrapper.add("test", 1000, "value", transcoder));
        EasyMock.verify(client, transcoder);
    }

    @Test
    public void decrStringInt() throws TimeoutException, CacheException {
        EasyMock.expect(client.decr("key1", 1)).andReturn(2L);
        EasyMock.replay(client);
        assertEquals(2L, clientWrapper.decr("key1", 1));
        EasyMock.verify(client);
    }

    @Test
    public void decrStringIntLong() throws TimeoutException, CacheException {
        EasyMock.expect(client.decr("key1", 1, 10L)).andReturn(2L);
        EasyMock.replay(client);
        assertEquals(2L, clientWrapper.decr("key1", 1, 10));
        EasyMock.verify(client);
    }

    @Test
    public void delete() throws TimeoutException, CacheException {
        EasyMock.expect(client.delete("key1")).andReturn(getFuture(true));
        EasyMock.replay(client);
        assertTrue(clientWrapper.delete("key1"));
        EasyMock.verify(client);
    }

    @Test
    public void flush() throws CacheException {
        EasyMock.expect(client.flush()).andReturn(getFuture(true));
        EasyMock.replay(client);
        clientWrapper.flush();
        EasyMock.verify(client);
    }

    @Test
    public void getString() throws TimeoutException, CacheException {
        EasyMock.expect(client.get("key1")).andReturn("test-value");
        EasyMock.replay(client);
        assertEquals("test-value", clientWrapper.get("key1"));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getStringMemcacheTranscoderOfT() throws CacheException, TimeoutException {
        CacheTranscoder<String> transcoder = EasyMock.createMock(CacheTranscoder.class);
        EasyMock.expect(client.get(EasyMock.eq("key1"), EasyMock.anyObject(Transcoder.class))).andReturn("test-value");
        EasyMock.replay(client);
        assertEquals("test-value", clientWrapper.get("key1", transcoder));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getStringMemcacheTranscoderOfTLong() throws TimeoutException, CacheException {
        CacheTranscoder<String> transcoder = EasyMock.createMock(CacheTranscoder.class);
        EasyMock.expect(client.asyncGet(EasyMock.eq("key1"), EasyMock.anyObject(Transcoder.class))).andReturn(getFuture("test-value"));
        EasyMock.replay(client);
        assertEquals("test-value", clientWrapper.get("key1", transcoder, 100));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAvailableServers() {
        Collection<SocketAddress> servers = EasyMock.createMock(Collection.class);
        EasyMock.expect(client.getAvailableServers()).andReturn(servers);
        EasyMock.replay(client);
        assertEquals(servers, clientWrapper.getAvailableServers());
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBulkCollectionOfString() throws TimeoutException, CacheException {
        Collection<String> keys = EasyMock.createMock(Collection.class);
        Map<String, Object> results = EasyMock.createMock(Map.class);

        EasyMock.expect(client.getBulk(keys)).andReturn(results);
        EasyMock.replay(client);
        assertEquals(results, clientWrapper.getBulk(keys));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBulkCollectionOfStringMemcacheTranscoderOfT() throws TimeoutException, CacheException {
        Collection<String> keys = EasyMock.createMock(Collection.class);
        Map<String, Object> results = EasyMock.createMock(Map.class);
        CacheTranscoder<String> transcoder = EasyMock.createMock(CacheTranscoder.class);

        EasyMock.expect(client.getBulk(EasyMock.eq(keys), EasyMock.anyObject(Transcoder.class))).andReturn(results);
        EasyMock.replay(client);
        assertEquals(results, clientWrapper.getBulk(keys, transcoder));
        EasyMock.verify(client);
    }

    @Test
    public void incrStringInt() throws TimeoutException, CacheException {
        EasyMock.expect(client.incr("key1", 1)).andReturn(2L);
        EasyMock.replay(client);
        assertEquals(2L, clientWrapper.incr("key1", 1));
        EasyMock.verify(client);
    }

    @Test
    public void incrStringIntLong() throws TimeoutException, CacheException {
        EasyMock.expect(client.incr("key1", 1, 10L)).andReturn(2L);
        EasyMock.replay(client);
        assertEquals(2L, clientWrapper.incr("key1", 1, 10));
        EasyMock.verify(client);
    }

    @Test
    public void incrStringIntLongInt() throws TimeoutException, CacheException {
        EasyMock.expect(client.incr("key1", 1, 10L, 1000)).andReturn(2L);
        EasyMock.replay(client);
        assertEquals(2L, clientWrapper.incr("key1", 1, 10, 1000));
        EasyMock.verify(client);
    }

    @Test
    public void setStringIntObject() throws TimeoutException, CacheException {
        EasyMock.expect(client.set("key1", 1, "value")).andReturn(getFuture(true));
        EasyMock.replay(client);
        assertTrue(clientWrapper.set("key1", 1, "value"));
        EasyMock.verify(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setStringIntTMemcacheTranscoderOfT() throws TimeoutException, CacheException {
        CacheTranscoder<String> transcoder = EasyMock.createMock(CacheTranscoder.class);
        EasyMock.expect(client.set(EasyMock.eq("key1"), EasyMock.eq(1), EasyMock.eq("value"), EasyMock.anyObject(Transcoder.class)))
                .andReturn(getFuture(true));
        EasyMock.replay(client);
        assertTrue(clientWrapper.set("key1", 1, "value", transcoder));
        EasyMock.verify(client);
    }

    @Test
    public void shutdown() {
        client.shutdown();
        EasyMock.expectLastCall();
        EasyMock.replay(client);
        clientWrapper.shutdown();
        EasyMock.verify(client);
    }

    @Test
    public void getTranscoder() {
        EasyMock.expect(client.getTranscoder()).andReturn(null);
        EasyMock.replay(client);
        clientWrapper.getTranscoder();
        EasyMock.verify(client);
    }

    private MemcachedClientIF getMock() {
        return EasyMock.createMock(MemcachedClientIF.class);
    }

    private <T> Future<T> getFuture(final T value) {
        return new Future<T>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                return value;
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return value;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

        };
    }

}
