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

package com.google.code.ssm.spring.test.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.code.ssm.spring.test.entity.AppUser;
import com.google.code.ssm.spring.test.entity.AppUserPK;

/**
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
@Service
public class AppUserDAOImpl implements AppUserDAO {

    private static final String USER_CACHE_NAME = "userCache";

    private static final String CLEARABLE_CACHE_NAME = "clearableCache";

    private static final Map<AppUserPK, AppUser> MAP = new ConcurrentHashMap<AppUserPK, AppUser>();

    private static final Map<Integer, List<AppUser>> USER_ID_MAP = new ConcurrentHashMap<Integer, List<AppUser>>();

    public AppUserDAOImpl() {

    }

    @Override
    @CachePut(value = USER_CACHE_NAME, key = "#entity.cacheKey()")
    public AppUser create(final AppUser entity) {
        MAP.put(entity.getPK(), entity);
        List<AppUser> list = USER_ID_MAP.get(entity.getUserId());
        if (list == null) {
            list = new ArrayList<AppUser>();
            USER_ID_MAP.put(entity.getUserId(), list);
        }
        list.add(entity);

        return entity;
    }

    @Override
    @Cacheable(value = USER_CACHE_NAME, key = "#pk.cacheKey()")
    public AppUser getByPk(final AppUserPK pk) {
        return getByPKFromDB(pk);
    }

    @Override
    @CachePut(value = USER_CACHE_NAME, key = "#entity.cacheKey()")
    public AppUser update(final AppUser entity) {
        AppUser appUser = entity;
        MAP.put(entity.getPK(), entity);
        List<AppUser> list = USER_ID_MAP.get(entity.getUserId());
        if (list == null) {
            list = new ArrayList<AppUser>();
            USER_ID_MAP.put(entity.getUserId(), list);
        }
        list.add(entity);

        assert appUser.getApplicationId() == entity.getApplicationId();
        assert appUser.getUserId() == entity.getUserId();

        return appUser;
    }

    @Override
    @CacheEvict(value = USER_CACHE_NAME, key = "#pk.cacheKey()")
    public void remove(final AppUserPK pk) {
        AppUser appUser = MAP.remove(pk);
        List<AppUser> list = USER_ID_MAP.get(pk.getUserId());
        if (list != null) {
            list.remove(appUser);
        }
    }

    @Override
    @CacheEvict(value = USER_CACHE_NAME, allEntries = true)
    public void removeAllUsers() {
        MAP.clear();
    }

    @Override
    @CacheEvict(value = CLEARABLE_CACHE_NAME, allEntries = true)
    public void removeAllFromCache() {

    }

    private AppUser getByPKFromDB(final AppUserPK pk) {
        AppUser appUser = MAP.get(pk);

        assert appUser == null || appUser.getApplicationId() == pk.getApplicationId();
        assert appUser == null || appUser.getUserId() == pk.getUserId();

        return appUser;
    }

}
