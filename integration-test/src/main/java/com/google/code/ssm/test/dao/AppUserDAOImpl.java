/*
 * Copyright (c) 2010-2011 Jakub Białek
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughMultiCacheOptions;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.UpdateSingleCache;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

/**
 * 
 * @author Jakub Białek
 * 
 */
@Service
public class AppUserDAOImpl implements AppUserDAO {

    private static final String PREFIX = "1/";

    private static final String SINGLE_NS = PREFIX + "user/s";

    private static final String LIST_NS = PREFIX + "user/l";

    private static final Map<AppUserPK, AppUser> map = new ConcurrentHashMap<AppUserPK, AppUser>();

    private static final Map<Integer, List<AppUser>> userIdApp = new ConcurrentHashMap<Integer, List<AppUser>>();

    public AppUserDAOImpl() {

    }

    @Override
    @UpdateSingleCache(namespace = SINGLE_NS, expiration = 2)
    public AppUserPK create(@ParameterDataUpdateContent @ParameterValueKeyProvider AppUser entity) {
        map.put(entity.getPK(), entity);
        List<AppUser> list = userIdApp.get(entity.getUserId());
        if (list == null) {
            list = new ArrayList<AppUser>();
            userIdApp.put(entity.getUserId(), list);
        }
        list.add(entity);

        return entity.getPK();
    }

    @Override
    @ReadThroughSingleCache(namespace = SINGLE_NS, expiration = 0)
    public AppUser getByPk(@ParameterValueKeyProvider AppUserPK pk) {
        return getByPKFromDB(pk);
    }

    @Override
    @UpdateSingleCache(namespace = SINGLE_NS, expiration = 2)
    @ReturnDataUpdateContent
    public AppUser update(@ParameterValueKeyProvider AppUser entity) {
        AppUser appUser = entity;
        map.put(entity.getPK(), entity);
        List<AppUser> list = userIdApp.get(entity.getUserId());
        if (list == null) {
            list = new ArrayList<AppUser>();
            userIdApp.put(entity.getUserId(), list);
        }
        list.add(entity);

        assert appUser.getApplicationId() == entity.getApplicationId();
        assert appUser.getUserId() == entity.getUserId();

        return appUser;
    }

    @Override
    @InvalidateSingleCache(namespace = SINGLE_NS)
    public void remove(@ParameterValueKeyProvider AppUserPK pk) {
        AppUser appUser = map.remove(pk);
        List<AppUser> list = userIdApp.get(pk.getUserId());
        if (list != null) {
            list.remove(appUser);
        }
    }

    @Override
    @ReadThroughMultiCache(namespace = SINGLE_NS, expiration = 0, options = @ReadThroughMultiCacheOptions(generateKeysFromResult = true))
    public List<AppUser> getList(@ParameterValueKeyProvider(order = 0) int userId,
            @ParameterValueKeyProvider(order = 1) final List<Integer> appsIds) {
        List<AppUser> list = new ArrayList<AppUser>();
        for (Integer appId : appsIds) {
            AppUser au = map.get(new AppUserPK(userId, appId));
            if (au != null) {
                list.add(au);
            }
        }

        return list;
    }

    @Override
    @ReadThroughSingleCache(namespace = LIST_NS, expiration = 0)
    public List<Integer> getAppIdList(@ParameterValueKeyProvider(order = 0) int userId,
            @ParameterValueKeyProvider(order = 1) boolean authorized) {
        List<AppUser> list = userIdApp.get(userId);
        List<Integer> result = new ArrayList<Integer>();
        if (list != null) {
            for (AppUser appUser : list) {
                if (appUser.isEnabled() == authorized) {
                    result.add(appUser.getApplicationId());
                }
            }
        }

        return result;
    }

    @Override
    @ReadThroughMultiCache(namespace = SINGLE_NS, expiration = 0, options = @ReadThroughMultiCacheOptions(generateKeysFromResult = true))
    public List<AppUser> getUsersList(@ParameterValueKeyProvider(order = 1) int applicationId,
            @ParameterValueKeyProvider(order = 0) List<Integer> userIds) {
        List<AppUser> list = new ArrayList<AppUser>();
        for (Integer userId : userIds) {
            AppUser au = map.get(new AppUserPK(userId, applicationId));
            if (au != null && au.isEnabled()) {
                list.add(au);
            }
        }

        return list;
    }

    @Override
    @ReadThroughMultiCache(namespace = SINGLE_NS, expiration = 0, options = @ReadThroughMultiCacheOptions(generateKeysFromResult = true))
    public List<AppUser> getUsersListFromCache(@ParameterValueKeyProvider(order = 1) int applicationId,
            @ParameterValueKeyProvider(order = 0) List<Integer> usersIds, Collection<Integer> notFoundUsersIds) {
        notFoundUsersIds.addAll(usersIds);
        return null;
    }

    @Override
    @UpdateSingleCache(namespace = LIST_NS, expiration = 2)
    public void updateListInCache(@ParameterValueKeyProvider(order = 0) int userId,
            @ParameterValueKeyProvider(order = 1) boolean authorized, @ParameterDataUpdateContent List<Integer> appsIdsList) {
        // noting to do
    }

    @Override
    @UpdateSingleCache(namespace = SINGLE_NS, expiration = 0)
    @ReturnDataUpdateContent
    public AppUser getByPKFromDB(@ParameterValueKeyProvider AppUserPK pk) {
        AppUser appUser = map.get(pk);

        assert appUser == null || appUser.getApplicationId() == pk.getApplicationId();
        assert appUser == null || appUser.getUserId() == pk.getUserId();

        return appUser;
    }

}
