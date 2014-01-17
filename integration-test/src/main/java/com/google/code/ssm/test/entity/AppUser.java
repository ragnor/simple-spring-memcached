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

package com.google.code.ssm.test.entity;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.google.code.ssm.api.CacheKeyMethod;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class AppUser implements Externalizable {

    private static final long serialVersionUID = 1L;

    private static final int CLASS_VERSION = 1;

    private int userId;

    private int applicationId;

    private boolean enabled;

    private int version;

    public AppUser() {

    }

    public AppUser(int userId, int applicationId) {
        this.userId = userId;
        this.applicationId = applicationId;
    }

    public AppUser(AppUserPK pk) {
        this(pk.getUserId(), pk.getApplicationId());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readInt(); // reads CLASS_VERSION
        userId = in.readInt();
        applicationId = in.readInt();
        enabled = in.readBoolean();
        version = in.readInt();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(CLASS_VERSION);
        out.writeInt(userId);
        out.writeInt(applicationId);
        out.writeBoolean(enabled);
        out.writeInt(version);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + applicationId;
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + userId;
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppUser other = (AppUser) obj;
        if (applicationId != other.applicationId)
            return false;
        if (enabled != other.enabled)
            return false;
        if (userId != other.userId)
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("AppUser [userId=%d, applicationId=%d, enabled=%b, verion=%d]", userId, applicationId, enabled, version);
    }

    @CacheKeyMethod
    public String cacheKey() {
        return userId + "/" + applicationId;
    }

    public AppUserPK getPK() {
        return new AppUserPK(userId, applicationId);
    }

}
