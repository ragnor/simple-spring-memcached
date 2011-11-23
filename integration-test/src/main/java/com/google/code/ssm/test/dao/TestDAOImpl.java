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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
@Repository("testDao")
public class TestDAOImpl implements TestDAO {

    @ReadThroughSingleCache(namespace = "Alpha", expiration = 30)
    public String getDateString(@ParameterValueKeyProvider final String key) {
        final Date now = new Date();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
        return now.toString() + ":" + now.getTime();
    }

    @UpdateSingleCache(namespace = "Alpha", expiration = 30)
    public void overrideDateString(final int trash, @ParameterValueKeyProvider final String key,
            @ParameterDataUpdateContent final String overrideData) {
    }

    @ReadThroughMultiCache(namespace = "Bravo", expiration = 300)
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

    @UpdateSingleCache(namespace = "Bravo", expiration = 300)
    @ReturnDataUpdateContent
    public String updateTimestampValue(@ParameterValueKeyProvider final Long key) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        final Long now = new Date().getTime();
        final String result = now.toString() + "-U-" + key.toString();
        return result;
    }

    @UpdateMultiCache(namespace = "Bravo", expiration = 300)
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

    @UpdateMultiCache(namespace = "Bravo", expiration = 300)
    public void overrideTimestampValues(final int trash, @ParameterValueKeyProvider final List<Long> keys, final String nuthin,
            @ParameterDataUpdateContent final List<String> overrideData) {
    }

    @ReadThroughSingleCache(namespace = "Charlie", expiration = 1000)
    public String getRandomString(@ParameterValueKeyProvider final Long key) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        return RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30));
    }

    @InvalidateSingleCache(namespace = "Charlie")
    public void updateRandomString(@ParameterValueKeyProvider final Long key) {
        // Nothing really to do here.
    }

    @InvalidateSingleCache(namespace = "Charlie")
    @ReturnValueKeyProvider
    public Long updateRandomStringAgain(final Long key) {
        return key;
    }

    @ReadThroughMultiCache(namespace = "Delta", expiration = 1000)
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

    @InvalidateMultiCache(namespace = "Delta")
    public void updateRandomStrings(@ParameterValueKeyProvider final List<Long> keys) {
        // Nothing to do.
    }

    @InvalidateMultiCache(namespace = "Delta")
    @ReturnValueKeyProvider
    public List<Long> updateRandomStringsAgain(final List<Long> keys) {
        return keys;
    }

    @ReadThroughAssignCache(assignedKey = "SomePhatKey", namespace = "Echo", expiration = 3000)
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

    @InvalidateAssignCache(assignedKey = "SomePhatKey", namespace = "Echo")
    public void invalidateAssignStrings() {
    }

    @UpdateAssignCache(assignedKey = "SomePhatKey", namespace = "Echo", expiration = 3000)
    public void updateAssignStrings(int bubpkus, @ParameterDataUpdateContent final List<String> newData) {
    }

    @DecrementCounterInCache(namespace = "Omega")
    public void decrement(@ParameterValueKeyProvider String key) {

    }

    @IncrementCounterInCache(namespace = "Omega")
    public void increment(@ParameterValueKeyProvider String key) {

    }

    @Override
    @ReadCounterFromCache(namespace = "Omega")
    public long getCounter(@ParameterValueKeyProvider String key) {
        return 100;
    }

    @Override
    @UpdateCounterInCache(namespace = "Omega", expiration = 100)
    public void update(@ParameterValueKeyProvider String key, @ParameterDataUpdateContent Long value) {

    }

    @Override
    @InvalidateSingleCache(namespace = "Omega")
    public void invalidate(@ParameterValueKeyProvider String key) {

    }
}
