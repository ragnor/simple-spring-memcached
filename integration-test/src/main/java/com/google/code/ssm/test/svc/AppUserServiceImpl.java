package com.google.code.ssm.test.svc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.code.ssm.test.dao.AppUserDAO;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * 
 * @author Jakub Białek
 * 
 */
@Service
public class AppUserServiceImpl 
        implements AppUserService {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceImpl.class);
    
    @Autowired
    private AppUserDAO dao;
    
    @Override
    public void disableAppForUser(int userId, int applicationId) {
        AppUserPK pk = new AppUserPK(userId, applicationId);
        AppUser appUser = getApplicationUserFromDB(pk);
        if (appUser != null && appUser.isEnabled()) {
            appUser.setEnabled(false);
            getResponsibleDAO(userId).update(appUser);

            Set<Integer> appsIdsSet = new HashSet<Integer>(getResponsibleDAO(userId).getAppIdList(userId, true));
            if (appsIdsSet.remove(applicationId)) {
                getResponsibleDAO(userId).updateListInCache(userId, true, new ArrayList<Integer>(appsIdsSet));
            }

            appsIdsSet = new HashSet<Integer>(getResponsibleDAO(userId).getAppIdList(userId, false));
            if (appsIdsSet.add(applicationId)) {
                getResponsibleDAO(userId).updateListInCache(userId, false, new ArrayList<Integer>(appsIdsSet));
            }
        } else {
            LOGGER.info("Appuser with PK: " + pk
                    + " won't be uninstalled because it is null or already not marked as authorized: " + appUser);
        }
    }

    @Override
    public void enableAppForUser(int userId, int applicationId, boolean favourite) {
        AppUserPK pk = new AppUserPK(userId, applicationId);
        AppUser appUser = getApplicationUserFromDB(pk);
        if (appUser == null) {
            appUser = new AppUser(pk);
        }

        appUser.setEnabled(true);

        if (appUser.getVersion() != 0) {
            getResponsibleDAO(userId).update(appUser);
        } else {
            getResponsibleDAO(userId).create(appUser);
        }

        Set<Integer> appsIdsSet = new HashSet<Integer>(getResponsibleDAO(userId).getAppIdList(userId, true));
        if (appsIdsSet.add(applicationId)) {
            getResponsibleDAO(userId).updateListInCache(userId, true, new ArrayList<Integer>(appsIdsSet));
        }

        appsIdsSet = new HashSet<Integer>(getResponsibleDAO(userId).getAppIdList(userId, false));
        if (appsIdsSet.remove(applicationId)) {
            getResponsibleDAO(userId).updateListInCache(userId, false, new ArrayList<Integer>(appsIdsSet));
        }
    }

    @Override
    public AppUser get(int userId, int applicationId) {
        return getResponsibleDAO(userId).getByPk(new AppUserPK(userId, applicationId));
    }

    @Override
    public List<AppUser> getInstalledList(int userId, List<Integer> applicationsIds) {
        Collections.sort(applicationsIds);
        List<AppUser> applicationUsers = getResponsibleDAO(userId).getList(userId, applicationsIds);

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
    public List<AppUser> getList(int userId) {
        List<Integer> appsIds = getResponsibleDAO(userId).getAppIdList(userId, true);
        appsIds.addAll(getResponsibleDAO(userId).getAppIdList(userId, false));
        return getResponsibleDAO(userId).getList(userId, getUniqueSortedList(appsIds));
    }

    @Override
    public List<AppUser> getList(int userId, boolean authorized) {
        List<Integer> appsIds = getUniqueSortedList(getResponsibleDAO(userId).getAppIdList(userId, authorized));
        List<AppUser> appUsers = getResponsibleDAO(userId).getList(userId, appsIds);
        removeWithDifferentAuth(appUsers, authorized);        
        return appUsers;
    }

    @Override
    public List<Integer> getUserIdsList(int applicationId, List<Integer> userIds) {
        List<AppUser> applicationUsers = new ArrayList<AppUser>();
        List<Integer> notFoundUsersIds = new ArrayList<Integer>();

        // no matter which DAO is selected because data is fetched from global cache
        List<AppUser> result = getResponsibleDAO(0).getUsersListFromCache(applicationId, userIds,
                notFoundUsersIds);
        if (result != null) {
            applicationUsers.addAll(result);
        }

        if (notFoundUsersIds.size() > 0) {
                Collections.sort(notFoundUsersIds);
                applicationUsers.addAll(getResponsibleDAO(1).getUsersList(applicationId, notFoundUsersIds));
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
    public boolean isEnabled(int userId, int applicationId) {
        AppUser au = get(userId, applicationId);
        return isAuthorized(au);
    }

    private AppUser getApplicationUserFromDB(AppUserPK pk) {
        return getResponsibleDAO(pk.getUserId()).getByPKFromDB(pk);
    }

    private boolean isAuthorized(AppUser applicationUser) {
        return applicationUser != null && applicationUser.isEnabled();
    }

    private List<Integer> getUniqueSortedList(List<Integer> list) {
        // remove duplicates
        List<Integer> uniqueList = new ArrayList<Integer>(new HashSet<Integer>(list));
        // sort ASC
        Collections.sort(uniqueList);

        return uniqueList;
    }
    
    private List<Integer> removeWithDifferentAuth(List<AppUser> appUsers, boolean authorized) {
        List<Integer> removedAppIds = new ArrayList<Integer>();
        Iterator<AppUser> iter = appUsers.iterator();
        AppUser appUser = null;
        
        while(iter.hasNext()) {
            appUser = iter.next();
            if (appUser.isEnabled() != authorized) {
                iter.remove();
                removedAppIds.add(appUser.getApplicationId());
            }  
        }
        
        return removedAppIds;
    }

    private AppUserDAO getResponsibleDAO(int userId) {
    	return dao;
    }
    
}
