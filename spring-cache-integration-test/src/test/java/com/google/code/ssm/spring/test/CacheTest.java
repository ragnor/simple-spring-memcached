/*
 * Copyright (c) 2014-2015 Jakub Białek
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

package com.google.code.ssm.spring.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.spring.test.dao.AppUserDAO;
import com.google.code.ssm.spring.test.entity.AppUser;
import com.google.code.ssm.spring.test.entity.AppUserPK;

/**
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:simplesm-context.xml", "classpath*:application-context.xml" })
public class CacheTest {

    @Autowired
    private AppUserDAO dao;

    @Autowired
    @Qualifier("userCache")
    private Cache userCache;

    @Autowired
    @Qualifier("clearableCache")
    private Cache clearableCache;

    @Autowired
    @Qualifier("userCache")
    private CacheFactory userCacheFactory;

    @Autowired
    @Qualifier("clearableCache")
    private CacheFactory clearableCacheFactory;

    @Test
    public void test() throws TimeoutException, CacheException {
        AppUserPK pk = new AppUserPK(1, 2);
        AppUser appUser = new AppUser(pk);

        dao.create(appUser);

        check(appUser.getPK(), appUser);

        appUser.setEnabled(false);
        dao.update(appUser);

        check(appUser.getPK(), appUser);

        dao.remove(appUser.getPK());

        check(appUser.getPK(), null);
        check(appUser.getPK(), null);
    }

    @Test
    public void validClearAll() throws TimeoutException, CacheException {
        String key = "test-key";
        clearableCache.set(key, 10, "test-value", clearableCacheFactory.getDefaultSerializationType());
        assertNotNull(clearableCache.get(key, clearableCacheFactory.getDefaultSerializationType()));

        dao.removeAllFromCache();
        assertNull(clearableCache.get(key, clearableCacheFactory.getDefaultSerializationType()));
    }

    @Test(expected = IllegalStateException.class)
    public void invalidClearAll() {
        AppUserPK pk = new AppUserPK(10, 340);
        try {
            dao.create(new AppUser(pk));
            assertNotNull(dao.getByPk(pk));

            dao.removeAllUsers();
        } finally {
            assertNotNull(dao.getByPk(pk));
        }
    }

    private void check(final AppUserPK pk, final AppUser target) throws TimeoutException, CacheException {
        AppUser au;
        au = getResult(userCache.get(pk.cacheKey(), userCacheFactory.getDefaultSerializationType()));
        assertEquals(target, au);

        au = dao.getByPk(pk);
        assertEquals(target, au);
    }

    private AppUser getResult(final Object result) {
        return (result instanceof PertinentNegativeNull) ? null : (AppUser) result;
    }

}
