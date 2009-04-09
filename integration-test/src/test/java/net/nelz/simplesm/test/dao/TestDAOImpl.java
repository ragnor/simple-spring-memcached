package net.nelz.simplesm.test.dao;

import net.nelz.simplesm.annotations.*;
import org.springframework.stereotype.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

/**
Copyright (c) 2008  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
@Repository("testDao")
public class TestDAOImpl implements TestDAO {

	@ReadThroughSingleCache(namespace = "Alpha", keyIndex = 0, expiration = 30)
	public String getDateString(final String key) {
		final Date now = new Date();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {}
		return now.toString() + ":" + now.getTime();
	}

	@ReadThroughMultiCache(namespace = "Bravo", keyIndex = 0, expiration = 300)
	public List<String> getTimestampValues(final List<Long> keys) {
		final List<String> results = new ArrayList<String>();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {}
		final Long now = new Date().getTime();
		for (final Long key : keys) {
			results.add(now.toString() + "-X-" + key.toString());
		}
		return results;
	}

	@UpdateSingleCache(namespace = "Bravo", keyIndex = 0, expiration = 300)
	public String updateTimestampValue(final Long key) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {}
		final Long now = new Date().getTime();
		final String result = now.toString() + "-U-" + key.toString();
		return result;
	}

	@UpdateMultiCache(namespace = "Bravo", keyIndex = 0, expiration = 300)
	public List<String> updateTimestamValues(final List<Long> keys) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {}
		final Long now = new Date().getTime();
		final List<String> results = new ArrayList<String>();
		for (final Long key : keys) {
			results.add(now.toString() + "-M-" + key.toString());
		}
		return results;
	}

    @ReadThroughSingleCache(namespace = "Charlie", keyIndex = 0, expiration = 10000)
    public String getRandomString(final Long key) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}
        return RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30));
    }

    @InvalidateSingleCache(namespace = "Charlie", keyIndex = 0)
    public void updateRandomString(final Long key) {
        // Nothing really to do here.
    }

    @InvalidateSingleCache(namespace = "Charlie", keyIndex = -1)
    public Long updateRandomStringAgain(final Long key) {
        return key;
    }
}
