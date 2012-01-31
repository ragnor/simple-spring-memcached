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

package com.google.code.ssm.test.dao;

import org.springframework.stereotype.Repository;

import com.google.code.ssm.api.BridgeMethodMapping;
import com.google.code.ssm.api.BridgeMethodMappings;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.test.entity.AppUser;
import com.google.code.ssm.test.entity.AppUserPK;

/**
 * 
 * @author Chiara Zambelli
 * 
 */
@Repository("specificDao")
@BridgeMethodMappings({ @BridgeMethodMapping(methodName = "updateUser", erasedParamTypes = { Object.class }, targetParamTypes = { AppUser.class }) })
public class SpecificDAOImpl implements SpecificDAO {

    @Override
    @InvalidateSingleCache(namespace = "Baggins")
    public void updateUser(@ParameterValueKeyProvider final AppUser entity) {
    }

    @Override
    @InvalidateSingleCache(namespace = "Baggins")
    public void updateUserWithoutGenerics(@ParameterValueKeyProvider final AppUser entity) {
    }

    @Override
    public void updateUser(final AppUser entity, final int id) {
    }

    public void updateUser(final int id) {
    }

    @Override
    @ReadThroughSingleCache(namespace = "Baggins")
    public AppUser getRandomUserByPk(@ParameterValueKeyProvider final AppUserPK pk) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        return new AppUser((int) (Math.random() * 100000), 1);
    }

}
