/*
 * Copyright (c) 2008-2009 Nelson Carpentier
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

package com.google.code.ssm.aop;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import com.google.code.ssm.aop.support.AnnotationDataBuilder;
import com.google.code.ssm.api.ReadThroughAssignCache;

import org.junit.Test;

/**
 * 
 * @author Nelson Carpentier
 * 
 */
public class ReadThroughAssignCacheTest {

    @Test
    public void testAnnotationValidation() throws Exception {
        final AnnotationValidator testClass = new AnnotationValidator();
        Method method = testClass.getClass().getMethod("cacheMe1", (Class<?>[]) null);
        Annotation annotation = method.getAnnotation(ReadThroughAssignCache.class);
        try {
            AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughAssignCache.class, method);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage().indexOf("AssignedKey") != -1);
        }

        method = testClass.getClass().getMethod("cacheMe2", (Class<?>[]) null);
        annotation = method.getAnnotation(ReadThroughAssignCache.class);
        try {
            AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughAssignCache.class, method);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            System.out.println(ex.getMessage());
            assertTrue(ex.getMessage().indexOf("AssignedKey") != -1);
        }

        method = testClass.getClass().getMethod("cacheMe3", (Class<?>[]) null);
        annotation = method.getAnnotation(ReadThroughAssignCache.class);
        AnnotationDataBuilder.buildAnnotationData(annotation, ReadThroughAssignCache.class, method);

    }

    @SuppressWarnings("unused")
    private static class AnnotationValidator {

        @ReadThroughAssignCache(namespace = "bubba", expiration = 100)
        public String cacheMe1() {
            return null;
        }

        @ReadThroughAssignCache(namespace = "bubba", expiration = 100, assignedKey = "")
        public String cacheMe2() {
            return null;
        }

        @ReadThroughAssignCache(namespace = "bubba", expiration = 100, assignedKey = "gump")
        public String cacheMe3() {
            return null;
        }
    }

}
