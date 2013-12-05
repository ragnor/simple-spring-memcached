/* Copyright (c) 2012-2013 Jakub Białek
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

package com.google.code.ssm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.test.Point;
import com.google.code.ssm.transcoders.JavaTranscoder;
import com.google.code.ssm.transcoders.JsonTranscoder;
import com.google.code.ssm.transcoders.LongToStringTranscoder;
import com.google.code.ssm.util.ImmutableSet;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class CacheImplTest {

    private final String name = "someCache";

    private final Collection<String> aliases = ImmutableSet.of("alias1", "alias2");

    private final SerializationType defaultSerializationType = SerializationType.PROVIDER;

    private JsonTranscoder jsonTranscoder;

    private JavaTranscoder javaTranscoder;

    private CacheClient cacheClient;

    private CacheImpl cache;

    @Before
    public void setUp() {
        cacheClient = Mockito.mock(CacheClient.class);
        jsonTranscoder = Mockito.mock(JsonTranscoder.class);
        javaTranscoder = Mockito.mock(JavaTranscoder.class);
        cache = new CacheImpl(name, aliases, cacheClient, defaultSerializationType, jsonTranscoder, javaTranscoder, null,
                new CacheProperties());
    }

    @Test
    public void getName() {
        assertEquals(name, cache.getName());
    }

    @Test
    public void getAliases() {
        assertEquals(aliases, cache.getAliases());
    }

    @Test
    public void getAvailableServers() {
        @SuppressWarnings("unchecked")
        Collection<SocketAddress> addresses = Mockito.mock(Collection.class);
        Mockito.when(cacheClient.getAvailableServers()).thenReturn(addresses);

        assertSame(addresses, cache.getAvailableServers());
    }

    @Test
    public void add() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int expiration = 900;
        Object value = new Point(66, 99);

        cache.add(cacheKey, expiration, value, defaultSerializationType);
        Mockito.verify(cacheClient).add(cacheKey, expiration, value);

        cache.add(cacheKey, expiration, value, SerializationType.JSON);
        Mockito.verify(cacheClient).add(cacheKey, expiration, value, jsonTranscoder);

    }

    @Test
    public void addSilently() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int expiration = 900;
        Object value = new Point(66, 99);

        cache.addSilently(cacheKey, expiration, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).add(cacheKey, expiration, value);

        cache.addSilently(cacheKey, expiration, value, SerializationType.JSON);
        Mockito.verify(cacheClient).add(cacheKey, expiration, value, jsonTranscoder);

    }

    @Test
    public void decr() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;

        cache.decr(cacheKey, by);
        Mockito.verify(cacheClient).decr(cacheKey, by);
    }

    @Test
    public void delete() throws TimeoutException, CacheException {
        String cacheKey = "key1";

        cache.delete(cacheKey);
        Mockito.verify(cacheClient).delete(cacheKey);
    }

    @Test
    public void deleteMany() throws TimeoutException, CacheException {
        Collection<String> keys = ImmutableSet.of("key1", "key2");

        cache.delete(keys);
        Mockito.verify(cacheClient).delete(keys);
    }

    @Test
    public void flush() throws TimeoutException, CacheException {
        cache.flush();
        Mockito.verify(cacheClient).flush();
    }

    @Test
    public void get() throws TimeoutException, CacheException {
        String cacheKey = "key1";

        cache.get(cacheKey, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).get(cacheKey);

        cache.get(cacheKey, SerializationType.JSON);
        Mockito.verify(cacheClient).get(cacheKey, jsonTranscoder);
    }

    @Test
    public void getBulk() throws TimeoutException, CacheException {
        Collection<String> keys = ImmutableSet.of("key1", "key2");

        cache.getBulk(keys, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).getBulk(keys);

        cache.getBulk(keys, SerializationType.JSON);
        Mockito.verify(cacheClient).getBulk(keys, jsonTranscoder);
    }

    @Test
    public void incr() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;
        int def = 1;

        cache.incr(cacheKey, by, def);
        Mockito.verify(cacheClient).incr(cacheKey, by, def);
    }

    @Test
    public void incrExp() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;
        int def = 1;
        int exp = 60000;

        cache.incr(cacheKey, by, def, exp);
        Mockito.verify(cacheClient).incr(cacheKey, by, def, exp);
    }

    @Test
    public void set() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int exp = 60000;
        Object value = new Point(11, 22);

        cache.set(cacheKey, exp, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).set(cacheKey, exp, value);

        cache.set(cacheKey, exp, value, SerializationType.JSON);
        Mockito.verify(cacheClient).set(cacheKey, exp, value, jsonTranscoder);
    }

    @Test
    public void setSilently() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int exp = 60000;
        Object value = new Point(11, 22);

        cache.setSilently(cacheKey, exp, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).set(cacheKey, exp, value);

        cache.setSilently(cacheKey, exp, value, SerializationType.JSON);
        Mockito.verify(cacheClient).set(cacheKey, exp, value, jsonTranscoder);
    }

    @Test
    public void getCounter() throws TimeoutException, CacheException {
        String cacheKey = "key1";

        cache.getCounter(cacheKey);
        Mockito.verify(cacheClient).get(Mockito.eq(cacheKey), Mockito.any(LongToStringTranscoder.class));
    }

    @Test
    public void setCounter() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int expiration = 900;
        long value = 60;

        cache.setCounter(cacheKey, expiration, value);
        Mockito.verify(cacheClient).set(Mockito.eq(cacheKey), Mockito.eq(expiration), Mockito.eq(value),
                Mockito.any(LongToStringTranscoder.class));
    }

    @Test
    public void shutdown() {
        cache.shutdown();
        Mockito.verify(cacheClient).shutdown();
    }

}
