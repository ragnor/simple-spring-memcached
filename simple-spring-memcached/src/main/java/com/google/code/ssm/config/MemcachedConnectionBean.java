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

package com.google.code.ssm.config;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
public class MemcachedConnectionBean {

    private String nodeList;

    private String jndiKey;

    private boolean consistentHashing;

    private boolean useBinaryProtocol;

    private int operationTimeout = 500;

    private Integer maxAwayTime;

    public String getNodeList() {
        return nodeList;
    }

    public void setNodeList(final String nodeList) {
        this.nodeList = nodeList;
    }

    public boolean isConsistentHashing() {
        return consistentHashing;
    }

    public void setConsistentHashing(final boolean consistentHashing) {
        this.consistentHashing = consistentHashing;
    }

    public void setJndiKey(String jndiKey) {
        this.jndiKey = jndiKey;
    }

    public String getJndiKey() {
        return jndiKey;
    }

    public void setUseBinaryProtocol(boolean useBinaryProtocol) {
        this.useBinaryProtocol = useBinaryProtocol;
    }

    public boolean isUseBinaryProtocol() {
        return useBinaryProtocol;
    }

    public void setOperationTimeout(int operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

    public int getOperationTimeout() {
        return operationTimeout;
    }

    public void setMaxAwayTime(Integer maxAwayTime) {
        this.maxAwayTime = maxAwayTime;
    }

    public Integer getMaxAwayTime() {
        return maxAwayTime;
    }

}
