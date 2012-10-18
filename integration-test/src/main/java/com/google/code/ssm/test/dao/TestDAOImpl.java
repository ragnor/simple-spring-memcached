/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

package com.google.code.ssm.test.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Repository;

import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateMultiCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughAssignCache;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.UpdateAssignCache;
import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateSingleCache;
import com.google.code.ssm.api.counter.DecrementCounterInCache;
import com.google.code.ssm.api.counter.IncrementCounterInCache;
import com.google.code.ssm.api.counter.ReadCounterFromCache;
import com.google.code.ssm.api.counter.UpdateCounterInCache;
import com.google.code.ssm.test.cache.CacheConst;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
@Repository("testDao")
public class TestDAOImpl implements TestDAO {

    @Override
    @ReadThroughSingleCache(namespace = CacheConst.ALPHA, expiration = 30)
    public String getDateString(@ParameterValueKeyProvider final String key) {
        final Date now = new Date();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        return now.toString() + ":" + now.getTime();
    }

    @Override
    @UpdateSingleCache(namespace = CacheConst.ALPHA, expiration = 30)
    public void overrideDateString(final int trash, @ParameterValueKeyProvider final String key,
            @ParameterDataUpdateContent final String overrideData) {
    }

    @Override
    @ReadThroughMultiCache(namespace = CacheConst.BRAVO, expiration = 300)
    public List<String> getTimestampValues(@ParameterValueKeyProvider final List<Long> keys) {
        final List<String> results = new ArrayList<String>();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        final Long now = new Date().getTime();
        for (final Long key : keys) {
            results.add(now.toString() + "-X-" + key.toString());
        }
        return results;
    }

    @Override
    @UpdateSingleCache(namespace = CacheConst.BRAVO, expiration = 300)
    @ReturnDataUpdateContent
    public String updateTimestampValue(@ParameterValueKeyProvider final Long key) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        final Long now = new Date().getTime();
        return now.toString() + "-U-" + key.toString();
    }

    @Override
    @UpdateMultiCache(namespace = CacheConst.BRAVO, expiration = 300)
    @ReturnDataUpdateContent
    public List<String> updateTimestamValues(@ParameterValueKeyProvider final List<Long> keys) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        final Long now = new Date().getTime();
        final List<String> results = new ArrayList<String>();
        for (final Long key : keys) {
            results.add(now.toString() + "-M-" + key.toString());
        }
        return results;
    }

    @Override
    @UpdateMultiCache(namespace = CacheConst.BRAVO, expiration = 300)
    public void overrideTimestampValues(final int trash, @ParameterValueKeyProvider final List<Long> keys, final String nuthin,
            @ParameterDataUpdateContent final List<String> overrideData) {
    }

    @Override
    @ReadThroughSingleCache(namespace = "Charlie", expiration = 1000)
    public String getRandomString(@ParameterValueKeyProvider final Long key) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        return RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30));
    }

    @Override
    @InvalidateSingleCache(namespace = CacheConst.CHARLIE)
    public void updateRandomString(@ParameterValueKeyProvider final Long key) {
        // Nothing really to do here.
    }

    @Override
    @InvalidateSingleCache(namespace = CacheConst.CHARLIE)
    @ReturnValueKeyProvider
    public Long updateRandomStringAgain(final Long key) {
        return key;
    }

    @Override
    @ReadThroughMultiCache(namespace = CacheConst.DELTA, expiration = 1000)
    public List<String> getRandomStrings(@ParameterValueKeyProvider final List<Long> keys) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        final String series = RandomStringUtils.randomAlphabetic(6);
        final List<String> results = new ArrayList<String>(keys.size());
        for (final Long key : keys) {
            results.add(series + "-" + key.toString() + "-" + RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30)));
        }
        return results;
    }

    @Override
    @InvalidateMultiCache(namespace = CacheConst.DELTA)
    public void updateRandomStrings(@ParameterValueKeyProvider final List<Long> keys) {
        // Nothing to do.
    }

    @Override
    @InvalidateMultiCache(namespace = CacheConst.DELTA)
    @ReturnValueKeyProvider
    public List<Long> updateRandomStringsAgain(final List<Long> keys) {
        return keys;
    }

    @Override
    @ReadThroughAssignCache(assignedKey = "SomePhatKey", namespace = CacheConst.ECHO, expiration = 3000)
    public List<String> getAssignStrings() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        final List<String> results = new ArrayList<String>();
        final long extra = System.currentTimeMillis() % 20;
        final String base = System.currentTimeMillis() + "";
        for (int ix = 0; ix < 20 + extra; ix++) {
            results.add(ix + "-" + base);
        }
        return results;
    }

    @Override
    @InvalidateAssignCache(assignedKey = "SomePhatKey", namespace = CacheConst.ECHO)
    public void invalidateAssignStrings() {
    }

    @Override
    @UpdateAssignCache(assignedKey = "SomePhatKey", namespace = CacheConst.ECHO, expiration = 3000)
    public void updateAssignStrings(final int bubpkus, @ParameterDataUpdateContent final List<String> newData) {
    }

    @Override
    @DecrementCounterInCache(namespace = CacheConst.OMEGA)
    public void decrement(@ParameterValueKeyProvider final String key) {

    }

    @Override
    @IncrementCounterInCache(namespace = CacheConst.OMEGA)
    public void increment(@ParameterValueKeyProvider final String key) {

    }

    @Override
    @ReadCounterFromCache(namespace = CacheConst.OMEGA)
    public long getCounter(@ParameterValueKeyProvider final String key) {
        return 100;
    }

    @Override
    @UpdateCounterInCache(namespace = CacheConst.OMEGA, expiration = 100)
    public void update(@ParameterValueKeyProvider final String key, @ParameterDataUpdateContent final Long value) {

    }

    @Override
    @InvalidateSingleCache(namespace = CacheConst.OMEGA)
    public void invalidate(@ParameterValueKeyProvider final String key) {

    }
}
