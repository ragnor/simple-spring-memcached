/*
 * Copyright (c) 2012 Chiara Zambelli
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

package com.google.code.ssm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.code.ssm.test.dao.SpecificDAO;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

/**
 * 
 * @author Chiara Zambelli
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class GenericsTest {

    private static final AppUserPK TEST_APPUSER_PK = new AppUserPK(88, 1);

    @Autowired
    private SpecificDAO specificDao;

    @Test
    public void testMethodWithGenerics() {
        AppUser firstRandomUser = specificDao.getRandomUserByPk(TEST_APPUSER_PK);
        assertEquals(firstRandomUser, specificDao.getRandomUserByPk(TEST_APPUSER_PK));
        specificDao.updateUser(new AppUser(TEST_APPUSER_PK));
        AppUser secondRandomUser = specificDao.getRandomUserByPk(TEST_APPUSER_PK);
        assertFalse(firstRandomUser.equals(secondRandomUser));
        assertEquals(secondRandomUser, specificDao.getRandomUserByPk(TEST_APPUSER_PK));
    }

    @Test
    public void testMethodWithoutGenerics() {
        AppUser firstRandomUser = specificDao.getRandomUserByPk(TEST_APPUSER_PK);
        assertEquals(firstRandomUser, specificDao.getRandomUserByPk(TEST_APPUSER_PK));
        specificDao.updateUserWithoutGenerics(new AppUser(TEST_APPUSER_PK));
        AppUser secondRandomUser = specificDao.getRandomUserByPk(TEST_APPUSER_PK);
        assertFalse(firstRandomUser.equals(secondRandomUser));
        assertEquals(secondRandomUser, specificDao.getRandomUserByPk(TEST_APPUSER_PK));
    }

}
