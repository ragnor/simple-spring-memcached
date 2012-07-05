package com.google.code.ssm.test;

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
import com.google.code.ssm.aop.support.PertinentNegativeNull;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.test.dao.AppUserDAO;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

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

    private static final SerializationType SERIALIZATION_TYPE = SerializationType.PROVIDER;

    @Autowired
    private AppUserDAO dao;

    @Autowired
    @Qualifier("userCache")
    private Cache userCache;

    @Autowired
    @Qualifier("clearableCache")
    private Cache clearableCache;

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
        clearableCache.set(key, 10, "test-value", SERIALIZATION_TYPE);
        assertNotNull(clearableCache.get(key, SERIALIZATION_TYPE));

        dao.removeAllFromCache();
        assertNull(clearableCache.get(key, SERIALIZATION_TYPE));
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
        au = getResult(userCache.get(pk.cacheKey(), SERIALIZATION_TYPE));
        assertEquals(target, au);

        au = dao.getByPk(pk);
        assertEquals(target, au);
    }

    private AppUser getResult(final Object result) {
        return (result instanceof PertinentNegativeNull) ? null : (AppUser) result;
    }

}
