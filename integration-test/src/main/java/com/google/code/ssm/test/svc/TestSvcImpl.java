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

package com.google.code.ssm.test.svc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.code.ssm.test.dao.TestDAO;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
@Service("testSvc")
public class TestSvcImpl implements TestSvc {

    @Autowired
    private TestDAO dao;

    public void setDao(final TestDAO dao) {
        this.dao = dao;
    }

    @Override
    public String getDateString(final String key) {
        return this.dao.getDateString(key);
    }

    @Override
    public void overrideDateString(final int trash, final String key, final String overrideData) {
        dao.overrideDateString(trash, key, overrideData);
    }

    @Override
    public List<String> getTimestampValues(final List<Long> keys) {
        return this.dao.getTimestampValues(keys);
    }

    @Override
    public String updateTimestampValue(final Long key) {
        return this.dao.updateTimestampValue(key);
    }

    @Override
    public List<String> updateTimestamValues(final List<Long> keys) {
        return this.dao.updateTimestamValues(keys);
    }

    @Override
    public void overrideTimestampValues(final int trash, final List<Long> keys, final String nuthin, final List<String> overrideData) {
        dao.overrideTimestampValues(trash, keys, nuthin, overrideData);
    }

    @Override
    public String getRandomString(final Long key) {
        return this.dao.getRandomString(key);
    }

    @Override
    public void updateRandomString(final Long key) {
        this.dao.updateRandomString(key);
    }

    @Override
    public Long updateRandomStringAgain(final Long key) {
        return this.dao.updateRandomStringAgain(key);
    }

    @Override
    public List<String> getRandomStrings(final List<Long> keys) {
        return this.dao.getRandomStrings(keys);
    }

    @Override
    public void updateRandomStrings(final List<Long> keys) {
        this.dao.updateRandomStrings(keys);
    }

    @Override
    public List<Long> updateRandomStringsAgain(final List<Long> keys) {
        return this.dao.updateRandomStringsAgain(keys);
    }

    @Override
    public List<String> getAssignStrings() {
        return this.dao.getAssignStrings();
    }

    @Override
    public void invalidateAssignStrings() {
        this.dao.invalidateAssignStrings();
    }

    @Override
    public void updateAssignStrings(final List<String> newData) {
        this.dao.updateAssignStrings(25, newData);
    }

    @Override
    public void decrement(final String key) {
        dao.decrement(key);
    }

    @Override
    public long getCounter(final String key) {
        return dao.getCounter(key);
    }

    @Override
    public void increment(final String key) {
        dao.increment(key);
    }

    @Override
    public void update(final String key, final Long value) {
        dao.update(key, value);
    }

    @Override
    public void invalidate(final String key) {
        dao.invalidate(key);
    }

}
