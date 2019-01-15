/* Copyright (c) 2012-2019 Jakub Białek
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
import static com.google.code.ssm.test.Matcher.any;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
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

    protected final String name = "someCache";

    protected final Collection<String> aliases = ImmutableSet.of("alias1", "alias2");

    protected final SerializationType defaultSerializationType = SerializationType.PROVIDER;

    protected JsonTranscoder jsonTranscoder;

    protected JavaTranscoder javaTranscoder;

    protected CacheClient cacheClient;

    protected Cache cache;

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
        Mockito.verify(cacheClient).add(getKey(cacheKey), expiration, value);

        cache.add(cacheKey, expiration, value, SerializationType.JSON);
        Mockito.verify(cacheClient).add(getKey(cacheKey), expiration, value, jsonTranscoder);

    }

    @Test
    public void addSilently() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int expiration = 900;
        Object value = new Point(66, 99);

        cache.addSilently(cacheKey, expiration, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).add(getKey(cacheKey), expiration, value);

        cache.addSilently(cacheKey, expiration, value, SerializationType.JSON);
        Mockito.verify(cacheClient).add(getKey(cacheKey), expiration, value, jsonTranscoder);

    }

    @Test
    public void decr() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;

        cache.decr(cacheKey, by);
        Mockito.verify(cacheClient).decr(getKey(cacheKey), by);
    }

    @Test
    public void delete() throws TimeoutException, CacheException {
        String cacheKey = "key1";

        cache.delete(cacheKey);
        Mockito.verify(cacheClient).delete(getKey(cacheKey));
    }

    @Test
    public void deleteMany() throws TimeoutException, CacheException {
        Collection<String> keys = ImmutableSet.of("key1", "key2");

        cache.delete(keys);
        Mockito.verify(cacheClient).delete(sameItems(getKeys(keys)));
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
        Mockito.verify(cacheClient).get(getKey(cacheKey));

        cache.get(cacheKey, SerializationType.JSON);
        Mockito.verify(cacheClient).get(getKey(cacheKey), jsonTranscoder);
    }

    @Test
    public void getBulk() throws TimeoutException, CacheException {
        Collection<String> keys = ImmutableSet.of("key1", "key2");

        cache.getBulk(keys, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).getBulk(sameItems(getKeys(keys)));

        cache.getBulk(keys, SerializationType.JSON);
        Mockito.verify(cacheClient).getBulk(sameItems(getKeys(keys)), Mockito.eq(jsonTranscoder));
    }

    @Test
    public void incr() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;
        int def = 1;

        cache.incr(cacheKey, by, def);
        Mockito.verify(cacheClient).incr(getKey(cacheKey), by, def);
    }

    @Test
    public void incrExp() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int by = 5;
        int def = 1;
        int exp = 60000;

        cache.incr(cacheKey, by, def, exp);
        Mockito.verify(cacheClient).incr(getKey(cacheKey), by, def, exp);
    }

    @Test
    public void set() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int exp = 60000;
        Object value = new Point(11, 22);

        cache.set(cacheKey, exp, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).set(getKey(cacheKey), exp, value);

        cache.set(cacheKey, exp, value, SerializationType.JSON);
        Mockito.verify(cacheClient).set(getKey(cacheKey), exp, value, jsonTranscoder);
    }

    @Test
    public void setSilently() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int exp = 60000;
        Object value = new Point(11, 22);

        cache.setSilently(cacheKey, exp, value, SerializationType.PROVIDER);
        Mockito.verify(cacheClient).set(getKey(cacheKey), exp, value);

        cache.setSilently(cacheKey, exp, value, SerializationType.JSON);
        Mockito.verify(cacheClient).set(getKey(cacheKey), exp, value, jsonTranscoder);
    }

    @Test
    public void getCounter() throws TimeoutException, CacheException {
        String cacheKey = "key1";

        cache.getCounter(cacheKey);
        Mockito.verify(cacheClient).get(Mockito.eq(getKey(cacheKey)), any(LongToStringTranscoder.class));
    }

    @Test
    public void setCounter() throws TimeoutException, CacheException {
        String cacheKey = "key1";
        int expiration = 900;
        long value = 60;

        cache.setCounter(cacheKey, expiration, value);
        Mockito.verify(cacheClient).set(Mockito.eq(getKey(cacheKey)), Mockito.eq(expiration), Mockito.eq(value),
                any(LongToStringTranscoder.class));
    }

    @Test
    public void shutdown() {
        cache.shutdown();
        Mockito.verify(cacheClient).shutdown();
    }

    @Test
    public void getNativeClient() {
        cache.getNativeClient();
        Mockito.verify(cacheClient).getNativeClient();
    }

    protected String getKey(String key) {
        return key;
    }

    protected Collection<String> getKeys(Collection<String> keys) {
        final Collection<String> cacheKeys = new ArrayList<String>();
        for (String key : keys) {
            cacheKeys.add(getKey(key));
        }

        return cacheKeys;
    }

    private static Collection<String> sameItems(Collection<String> items) {
        class CollectionOfItemssMatcher implements ArgumentMatcher<Collection<String>> {

            private final Collection<String> expectedItems;

            public CollectionOfItemssMatcher(Collection<String> items) {
                this.expectedItems = items;
            }

            @Override
            public boolean matches(Collection<String> actual) {
                if (actual == null) {
                    return false;
                }

                if (actual.size() != expectedItems.size()) {
                    return false;
                }

                Iterator<String> actualIter = actual.iterator();
                Iterator<String> expectedIter = expectedItems.iterator();
                while (actualIter.hasNext()) {
                    if (!actualIter.next().equals(expectedIter.next())) {
                        return false;
                    }
                }

                return true;
            }
        }
        return Mockito.argThat(new CollectionOfItemssMatcher(items));
    }

}
