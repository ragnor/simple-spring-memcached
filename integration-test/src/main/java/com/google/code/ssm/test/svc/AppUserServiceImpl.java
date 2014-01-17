/*
 * Copyright (c) 2010-2014 Jakub Białek
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.code.ssm.test.dao.AppUserDAO;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

/**
 * 
 * @author Jakub Białek
 * 
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceImpl.class);

    @Autowired
    private AppUserDAO dao;

    @Override
    public void disableAppForUser(final int userId, final int applicationId) {
        AppUserPK pk = new AppUserPK(userId, applicationId);
        AppUser appUser = getApplicationUserFromDB(pk);
        if (appUser != null && appUser.isEnabled()) {
            appUser.setEnabled(false);
            getDao().update(appUser);

            Set<Integer> appsIdsSet = new HashSet<Integer>(getDao().getAppIdList(userId, true));
            if (appsIdsSet.remove(applicationId)) {
                getDao().updateListInCache(userId, true, new ArrayList<Integer>(appsIdsSet));
            }

            appsIdsSet = new HashSet<Integer>(getDao().getAppIdList(userId, false));
            if (appsIdsSet.add(applicationId)) {
                getDao().updateListInCache(userId, false, new ArrayList<Integer>(appsIdsSet));
            }
        } else {
            LOGGER.info("Appuser with PK: " + pk + " won't be uninstalled because it is null or already not marked as authorized: "
                    + appUser);
        }
    }

    @Override
    public void enableAppForUser(final int userId, final int applicationId, final boolean favourite) {
        AppUserPK pk = new AppUserPK(userId, applicationId);
        AppUser appUser = getApplicationUserFromDB(pk);
        if (appUser == null) {
            appUser = new AppUser(pk);
        }

        appUser.setEnabled(true);

        if (appUser.getVersion() != 0) {
            getDao().update(appUser);
        } else {
            getDao().create(appUser);
        }

        Set<Integer> appsIdsSet = new HashSet<Integer>(getDao().getAppIdList(userId, true));
        if (appsIdsSet.add(applicationId)) {
            getDao().updateListInCache(userId, true, new ArrayList<Integer>(appsIdsSet));
        }

        appsIdsSet = new HashSet<Integer>(getDao().getAppIdList(userId, false));
        if (appsIdsSet.remove(applicationId)) {
            getDao().updateListInCache(userId, false, new ArrayList<Integer>(appsIdsSet));
        }
    }

    @Override
    public AppUser get(final int userId, final int applicationId) {
        return getDao().getByPk(new AppUserPK(userId, applicationId));
    }

    @Override
    public List<AppUser> getInstalledList(final int userId, final List<Integer> applicationsIds) {
        Collections.sort(applicationsIds);
        List<AppUser> applicationUsers = getDao().getList(userId, applicationsIds);

        Iterator<AppUser> iter = applicationUsers.iterator();
        AppUser appUser = null;
        while (iter.hasNext()) {
            appUser = iter.next();
            if (appUser == null || !appUser.isEnabled()) {
                iter.remove();
            }
        }

        return applicationUsers;
    }

    @Override
    public List<AppUser> getList(final int userId) {
        List<Integer> appsIds = getDao().getAppIdList(userId, true);
        appsIds.addAll(getDao().getAppIdList(userId, false));
        return getDao().getList(userId, getUniqueSortedList(appsIds));
    }

    @Override
    public List<AppUser> getList(final int userId, final boolean authorized) {
        List<Integer> appsIds = getUniqueSortedList(getDao().getAppIdList(userId, authorized));
        List<AppUser> appUsers = getDao().getList(userId, appsIds);
        removeWithDifferentAuth(appUsers, authorized);
        return appUsers;
    }

    @Override
    public List<Integer> getUserIdsList(final int applicationId, final List<Integer> userIds) {
        List<AppUser> applicationUsers = new ArrayList<AppUser>();
        List<Integer> notFoundUsersIds = new ArrayList<Integer>();

        // no matter which DAO is selected because data is fetched from global cache
        List<AppUser> result = getDao().getUsersListFromCache(applicationId, userIds, notFoundUsersIds);
        if (result != null) {
            applicationUsers.addAll(result);
        }

        if (notFoundUsersIds.size() > 0) {
            Collections.sort(notFoundUsersIds);
            applicationUsers.addAll(getDao().getUsersList(applicationId, notFoundUsersIds));
        }

        List<Integer> idsOfAppUsers = new ArrayList<Integer>(applicationUsers.size());
        for (AppUser au : applicationUsers) {
            if (au != null && au.isEnabled()) {
                idsOfAppUsers.add(au.getUserId());
            }
        }

        return idsOfAppUsers;
    }

    @Override
    public boolean isEnabled(final int userId, final int applicationId) {
        AppUser au = get(userId, applicationId);
        return isAuthorized(au);
    }

    private AppUser getApplicationUserFromDB(final AppUserPK pk) {
        return getDao().getByPKFromDB(pk);
    }

    private boolean isAuthorized(final AppUser applicationUser) {
        return applicationUser != null && applicationUser.isEnabled();
    }

    private List<Integer> getUniqueSortedList(final List<Integer> list) {
        // remove duplicates
        List<Integer> uniqueList = new ArrayList<Integer>(new HashSet<Integer>(list));
        // sort ASC
        Collections.sort(uniqueList);

        return uniqueList;
    }

    private List<Integer> removeWithDifferentAuth(final List<AppUser> appUsers, final boolean authorized) {
        List<Integer> removedAppIds = new ArrayList<Integer>();
        Iterator<AppUser> iter = appUsers.iterator();
        AppUser appUser = null;

        while (iter.hasNext()) {
            appUser = iter.next();
            if (appUser.isEnabled() != authorized) {
                iter.remove();
                removedAppIds.add(appUser.getApplicationId());
            }
        }

        return removedAppIds;
    }

    private AppUserDAO getDao() {
        return dao;
    }

}
