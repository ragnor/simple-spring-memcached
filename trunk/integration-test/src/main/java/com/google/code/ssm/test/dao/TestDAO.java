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

import java.util.*;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public interface TestDAO {

    public String getDateString(final String key);

    public void overrideDateString(final int trash, final String key, final String overrideData);

    public List<String> getTimestampValues(final List<Long> keys);

    public String updateTimestampValue(final Long key);

    public List<String> updateTimestamValues(final List<Long> keys);

    public void overrideTimestampValues(final int trash, final List<Long> keys, final String nuthin, final List<String> overrideData);

    public String getRandomString(final Long key);

    public void updateRandomString(final Long key);

    public Long updateRandomStringAgain(final Long key);

    public List<String> getRandomStrings(final List<Long> keys);

    public void updateRandomStrings(final List<Long> keys);

    public List<Long> updateRandomStringsAgain(final List<Long> keys);

    public List<String> getAssignStrings();

    public void invalidateAssignStrings();

    public void updateAssignStrings(int bubpkus, final List<String> newData);

    public void increment(String key);

    public void decrement(String key);

    public long getCounter(String key);

    public void update(String key, Long value);

    public void invalidate(String key);

}
