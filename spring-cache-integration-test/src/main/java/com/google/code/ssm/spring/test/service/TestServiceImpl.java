/*
 * Copyright (c) 2014-2018 Jakub Białek
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

package com.google.code.ssm.spring.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.code.ssm.spring.test.dao.TestDAO;

/**
 * 
 * Test service responsible for delegating calls to dao. Used to verify that errors from memcached providers do not
 * interrupt cached (intercepted) method invocation.
 * 
 * @author Jakub Białek
 * @since 3.4.0
 * 
 */
@Service
public class TestServiceImpl implements TestService {

    private static final String CACHE = "testCache";

    @Autowired
    private TestDAO dao;

    @Override
    @Cacheable(value = CACHE, key = "#id")
    public String get(long id) {
        return dao.get(id);
    }

    @Override
    @CachePut(value = CACHE, key = "#id")
    public String update(long id, String value) {
        return dao.update(id, value);
    }

    @Override
    @CacheEvict(value = CACHE, key = "#id")
    public void remove(long id) {
        dao.remove(id);
    }

    @Override
    @CacheEvict(value = CACHE, key = "#id", beforeInvocation = true)
    public void removeClearCache(long id) {
        dao.remove(id);
    }

    public void setDao(TestDAO dao) {
        this.dao = dao;
    }

}
