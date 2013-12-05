/*
 * Copyright (c) 2008-2013 Nelson Carpentier, Jakub Białek
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

package com.google.code.ssm.providers;

import lombok.Data;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Data
public class CacheConfiguration {

    private boolean consistentHashing;

    private boolean useBinaryProtocol;

    private Integer operationTimeout;

    /**
     * If true name of cache (name or alias used to get the cache instance) will be used as a prefix to all cache keys.
     * 
     * @since 3.3.0
     */
    private boolean useNameAsKeyPrefix;

    /**
     * If {@link CacheConfiguration#useNameAsKeyPrefix} is true then this value is used as a separator in a key to
     * separate cache name or alias from other parts of key.
     * 
     * @since 3.3.0
     */
    private String keyPrefixSeparator = "#";

}
