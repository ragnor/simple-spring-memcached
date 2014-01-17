/*
 * Copyright (c) 2008-2014 Nelson Carpentier, Jakub Białek
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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.support.AnnotationData;
import com.google.code.ssm.api.UpdateAssignCache;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
@Aspect
public class UpdateAssignCacheAdvice extends SingleUpdateCacheAdvice<UpdateAssignCache> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssignCacheAdvice.class);

    public UpdateAssignCacheAdvice() {
        super(UpdateAssignCache.class);
    }

    @Pointcut("@annotation(com.google.code.ssm.api.UpdateAssignCache)")
    public void updateAssign() {
    }

    @AfterReturning(pointcut = "updateAssign()", returning = "retVal")
    public void cacheUpdateAssign(final JoinPoint jp, final Object retVal) throws Throwable {
        update(jp, retVal);
    }

    @Override
    protected String getCacheKey(final AnnotationData data, final Object[] args, final String methodDesc) throws Exception {
        return getCacheBase().getCacheKeyBuilder().getAssignCacheKey(data);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
