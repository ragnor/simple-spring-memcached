package com.google.code.ssm.test.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.google.code.ssm.api.CacheName;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.UpdateSingleCache;

/*
 * Copyright (c) 2012 Jakub Białek
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

/**
 * 
 * @author Jakub Białek
 * 
 */
@Repository("multiCacheDao")
public class MultiCacheDAOImpl implements MultiCacheDAO {

    private final Map<String, String> storedValues = new HashMap<String, String>();

    @Override
    @UpdateSingleCache(namespace = "multi", expiration = 30)
    public void storeToDefault(@ParameterValueKeyProvider final String key, @ParameterDataUpdateContent final String value) {
        storedValues.put(key, value);
    }

    @Override
    @ReadThroughSingleCache(namespace = "multi", expiration = 30)
    public String getFromDefault(@ParameterValueKeyProvider final String key) {
        return storedValues.get(key);
    }

    @Override
    @InvalidateSingleCache(namespace = "multi")
    public void removeFromDefault(@ParameterValueKeyProvider final String key) {
        storedValues.remove(key);
    }

    @Override
    @CacheName("cache1")
    @UpdateSingleCache(namespace = "multi", expiration = 30)
    public void storeToDedicated(@ParameterValueKeyProvider final String key, @ParameterDataUpdateContent final String value) {
        storedValues.put(key, value);
    }

    @Override
    @CacheName("cache1Alias")
    @ReadThroughSingleCache(namespace = "multi", expiration = 30)
    public String getFromDedicated(@ParameterValueKeyProvider final String key) {
        return storedValues.get(key);
    }

    @Override
    @CacheName("cache1Alias")
    @InvalidateSingleCache(namespace = "multi")
    public void removeFromDedicated(@ParameterValueKeyProvider final String key) {
        storedValues.remove(key);
    }

}
