/*
 * Copyright (c) 2010-2013 Jakub Białek
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

import java.util.Collection;
import java.util.List;

import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

/**
 * 
 * @author Jakub Białek
 * 
 */
public interface AppUserDAO {

    AppUserPK create(AppUser entity);

    List<Integer> getAppIdList(int userId, boolean enbled);

    AppUser getByPk(AppUserPK pk);

    AppUser getByPKFromDB(AppUserPK pk);

    /**
     * 
     * @param userId
     *            the user ID
     * @param appsIds
     *            sorted list of applications' IDs in ASC order
     * @return list of entities
     */
    List<AppUser> getList(int userId, List<Integer> appsIds);

    /**
     * 
     * @param applicationId
     *            the ID of application
     * @param userIds
     *            sorted list of users IDs
     * @return list of entities
     */
    List<AppUser> getUsersList(int applicationId, List<Integer> userIds);

    /**
     * Fetch {@link AppUser} entities from remote cache (memcached).
     * 
     * @param applicationId
     *            ID of application
     * @param usersIds
     *            list of users IDs to fetch
     * @param notFoundUsersIds
     *            collection with users IDs that weren't found in cache
     * @return list of users of given application with given IDs from cache
     */
    List<AppUser> getUsersListFromCache(int applicationId, List<Integer> usersIds, Collection<Integer> notFoundUsersIds);

    void remove(AppUserPK pk);

    AppUser update(AppUser entity);

    void updateListInCache(int userId, boolean authorized, List<Integer> appsIdsList);

}
