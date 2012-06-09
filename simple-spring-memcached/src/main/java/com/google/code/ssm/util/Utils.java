/*
 * Copyright (c) 2012 Jakub Białek
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

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class Utils {

    public static Object getMethodArg(final int index, final Object[] args, final String methodDesc) {
        if (index < 0) {
            throw new InvalidParameterException(String.format("An index of %s is invalid", index));
        }
        if (args.length <= index) {
            throw new InvalidParameterException(String.format("An index of %s is too big for the number of arguments in [%s]", index,
                    methodDesc));
        }
        final Object indexObject = args[index];
        if (indexObject == null) {
            throw new InvalidParameterException(String.format("The argument passed into [%s] at index %s is null.", methodDesc, index));
        }
        return indexObject;
    }

    public static Object[] getMethodArgs(final Collection<Integer> indexes, final Object[] args, final String methodDesc) {
        Object[] selectedArgs = new Object[indexes.size()];
        Iterator<Integer> iter = indexes.iterator();
        for (int i = 0; i < indexes.size(); i++) {
            selectedArgs[i] = getMethodArg(iter.next(), args, methodDesc);
        }

        return selectedArgs;
    }

    public static Class<?>[] getMethodParameterTypes(final Collection<Integer> indexes, final Method method) {
        Class<?>[] selectedParameterTypes = new Class<?>[indexes.size()];
        Class<?>[] methodParameterTypes = method.getParameterTypes();

        int i = 0;
        for (int index : indexes) {
            if (index < 0) {
                throw new InvalidParameterException(String.format("An index of %s is invalid", index));
            }
            if (methodParameterTypes.length <= index) {
                throw new InvalidParameterException(String.format("An index of %s is too big for the number of arguments in [%s]", index,
                        method.toString()));
            }
            selectedParameterTypes[i++] = methodParameterTypes[index];
        }

        return selectedParameterTypes;
    }

}
