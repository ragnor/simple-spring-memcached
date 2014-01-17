/*
 * Copyright (c) 2012-2014 Jakub Białek
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
package com.google.code.ssm.jmemcached.plugin;

/**
 * 
 * @author Jakub Białek
 * @since 3.1.0
 * 
 */
public class Server {

    private int port = 11211;

    /**
     * Max amount of elements in cache.
     */
    private int maximumCapacity = 1000;

    /**
     * Max cache size in bytes. Default about 10MB.
     */
    private long maximumMemoryCapacity = 10000000;

    private boolean binary = false;

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(final int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public long getMaximumMemoryCapacity() {
        return maximumMemoryCapacity;
    }

    public void setMaximumMemoryCapacity(final long maximumMemoryCapacity) {
        this.maximumMemoryCapacity = maximumMemoryCapacity;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(final boolean binary) {
        this.binary = binary;
    }

    @Override
    public String toString() {
        return "Server [port=" + port + ", maximumCapacity=" + maximumCapacity + ", maximumMemoryCapacity=" + maximumMemoryCapacity
                + ", binary=" + binary + "]";
    }

}
