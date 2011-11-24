/*
 * Copyright (c) 2008-2011 Nelson Carpentier, Jakub Białek
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
 */

package com.google.code.ssm.aop;

import java.util.Collection;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
public class AnnotationData {

    public static final int DEFAULT_INTEGER = Integer.MIN_VALUE;

    private static final int RETURN_INDEX = -1;

    private String namespace = "";
    private boolean isReturnKeyIndex;
    private Collection<Integer> keysIndex;
    private int dataIndex = DEFAULT_INTEGER;
    private int expiration = 0;
    private String className = "";
    private String assignedKey = "";

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isReturnKeyIndex() {
        return isReturnKeyIndex;
    }

    public void setReturnKeyIndex(boolean isReturnKeyIndex) {
        this.isReturnKeyIndex = isReturnKeyIndex;
    }

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }

    public boolean isReturnDataIndex() {
        return dataIndex == RETURN_INDEX;
    }
    
    public void setReturnDataIndex(boolean isReturnDataIndex) {
        if (isReturnDataIndex) {
            dataIndex = RETURN_INDEX;
        } else {
            dataIndex = DEFAULT_INTEGER;
        }
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAssignedKey() {
        return assignedKey;
    }

    public void setAssignedKey(String assignedKey) {
        this.assignedKey = assignedKey;
    }

    public void setKeysIndex(Collection<Integer> keysIndex) {
        this.keysIndex = keysIndex;
    }

    public Collection<Integer> getKeysIndex() {
        return keysIndex;
    }
}
