package com.google.code.ssm.test.svc;

import com.google.code.ssm.test.dao.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Copyright (c) 2008, 2009 Nelson Carpentier
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
@Service("testSvc")
public class TestSvcImpl implements TestSvc {

	@Autowired
	private TestDAO dao;

	public void setDao(TestDAO dao) {
		this.dao = dao;
	}

	public String getDateString(final String key) {
		return this.dao.getDateString(key);
	}

    public void overrideDateString(final int trash, final String key, final String overrideData) {
        dao.overrideDateString(trash, key, overrideData);
    }

	public List<String> getTimestampValues(final List<Long> keys) {
		return this.dao.getTimestampValues(keys);
	}

	public String updateTimestampValue(final Long key) {
		return this.dao.updateTimestampValue(key);
	}

	public List<String> updateTimestamValues(final List<Long> keys) {
		return this.dao.updateTimestamValues(keys);
	}

    public void overrideTimestampValues(final int trash, final List<Long> keys,
                                        final String nuthin, final List<String> overrideData) {
        dao.overrideTimestampValues(trash, keys, nuthin, overrideData);
    }

    public String getRandomString(final Long key) {
        return this.dao.getRandomString(key);
    }

    public void updateRandomString(final Long key) {
        this.dao.updateRandomString(key);
    }

    public Long updateRandomStringAgain(final Long key) {
        return this.dao.updateRandomStringAgain(key);
    }

    public List<String> getRandomStrings(List<Long> keys) {
        return this.dao.getRandomStrings(keys);
    }

    public void updateRandomStrings(List<Long> keys) {
        this.dao.updateRandomStrings(keys);
    }

    public List<Long> updateRandomStringsAgain(List<Long> keys) {
        return this.dao.updateRandomStringsAgain(keys);
    }

    public List<String> getAssignStrings() {
        return this.dao.getAssignStrings();
    }

    public void invalidateAssignStrings() {
        this.dao.invalidateAssignStrings();
    }

    public void updateAssignStrings(final List<String> newData) {
        this.dao.updateAssignStrings(25, newData);
    }

	@Override
	public void decrement(String key) {
		dao.decrement(key);		
	}

	@Override
	public long getCounter(String key) {
		return dao.getCounter(key);
	}

	@Override
	public void increment(String key) {
		dao.increment(key);		
	}
	
	@Override
	public void update(String key, Long value) {
		dao.update(key, value);
	}

	@Override
	public void invalidate(String key) {
		dao.invalidate(key);		
	}

}
