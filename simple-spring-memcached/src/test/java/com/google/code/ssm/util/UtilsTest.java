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
 */

package com.google.code.ssm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
 * 
 */
public class UtilsTest {

    @Test
    public void getMethodArg() throws Exception {
        final String answer = "bubba";
        final Object[] args = new Object[] { null, answer, "blue" };

        final Method method = AOPTargetClass1.class.getDeclaredMethod("doIt", String.class, String.class, String.class);

        try {
            Utils.getMethodArg(-1, args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("is invalid") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            Utils.getMethodArg(3, args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("too big") != -1);
            System.out.println(ex.getMessage());
        }
        try {
            Utils.getMethodArg(4, args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("too big") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            Utils.getMethodArg(0, args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("null") != -1);
            System.out.println(ex.getMessage());
        }

        assertEquals(answer, Utils.getMethodArg(1, args, method.toString()));
    }

    @Test
    public void getMethodArgs() throws Exception {
        final Object[] answer = new Object[] { "blue", "bubba" };
        final Object[] args = new Object[] { null, "bubba", "blue" };

        final Method method = AOPTargetClass1.class.getDeclaredMethod("doIt", String.class, String.class, String.class);

        try {
            Utils.getMethodArgs(ImmutableList.of(1, -1), args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("is invalid") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            Utils.getMethodArgs(ImmutableList.of(2, 3), args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("too big") != -1);
            System.out.println(ex.getMessage());
        }
        try {
            Utils.getMethodArgs(ImmutableList.of(4, 0), args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("too big") != -1);
            System.out.println(ex.getMessage());
        }

        try {
            Utils.getMethodArgs(ImmutableList.of(1, 0), args, method.toString());
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().indexOf("null") != -1);
            System.out.println(ex.getMessage());
        }

        assertArrayEquals(answer, Utils.getMethodArgs(ImmutableList.of(2, 1), args, method.toString()));
    }

    public static class AOPTargetClass1 {

        public String doIt(final String s1, final String s2, final String s3) {
            return null;
        }

    }

}
