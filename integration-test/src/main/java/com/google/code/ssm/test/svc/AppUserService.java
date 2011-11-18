package com.google.code.ssm.test.svc;

import java.util.List;

import com.google.code.ssm.test.entity.AppUser;

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
public interface AppUserService {

    /**
     * Returns application - user association
     * 
     * @param userId
     *            user ID
     * @param applicationId
     *            application ID
     * @return {@link AppUser} for given user and application
     */
    AppUser get(int userId, int applicationId);

    /**
     * Get the list of {@link AppUser} entities for specified user that contains authorized
     * and unauthorized (uninstalled) applications.
     * 
     * @param userId
     *            the portal user ID
     * @return list of all {@link AppUser} of given user
     */
    List<AppUser> getList(int userId);

    /**
     * Get the list of {@link AppUser} entities for specified portal user.
     * 
     * 
     * @param userId
     *            the portal user ID
     * @param authorized
     *            if true the only authorized applications will be returned, otherwise other
     *            unauthorized (uninstalled) application will be returned
     * @return list of authorized or unauthorized {@link AppUser} of given user
     * 
     */
    List<AppUser> getList(int userId, boolean authorized);

    /**
     * Returns application - user associations
     * 
     * @param userId
     *            user ID
     * @param applicationsIds
     *            the IDs of applications
     * @return list of installed (authorized) {@link AppUser} of given user with given
     *         applications' IDs
     * 
     */
    List<AppUser> getInstalledList(int userId, List<Integer> applicationsIds);

    /**
     * Gets the sublist of given users IDs list that contains only those users that installed given
     * application.
     * 
     * @param applicationId
     * @param userIds
     *            portal user IDs
     * @return find who have installed the selected application
     */
    List<Integer> getUserIdsList(int applicationId, List<Integer> userIds);

    /**
     * Check if the user (by UID) is user of selected application.
     * 
     * @param userId
     *            the user portal ID
     * @param applicationId
     *            the application ID
     * @return true if given portal user is a user of selected application (authorized = true)
     */
    boolean isEnabled(int userId, int applicationId);

    /**
     * 
     * Installs application for user.
     * 
     * @param userId
     *            the portal user ID
     * @param applicationId
     *            the application ID
     * @param permissions
     *            granted permissions
     * @param favourite
     *            is application marked as favourite
     */
    void enableAppForUser(int userId, int applicationId, boolean favourite);

    /**
     * Uninstall application for user.
     * 
     * @param userId
     *            the portal user ID
     * @param applicationId
     *            the application ID
     */
    void disableAppForUser(int userId, int applicationId);

}
