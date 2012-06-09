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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Simple replacement for guava ImmutableSet.
 * 
 * @author Jakub Białek
 * @since 2.1.0
 * 
 */
public class ImmutableSet {

    public static <E> Set<E> of(final E e1) {
        Set<E> set = new HashSet<E>(1, 1);
        set.add(e1);

        return Collections.unmodifiableSet(set);
    }

    public static <E> Set<E> of(final E e1, final E e2) {
        Set<E> set = new HashSet<E>(2, 1);
        set.add(e1);
        set.add(e2);

        return Collections.unmodifiableSet(set);
    }

    public static <E> Set<E> of(final E e1, final E e2, final E e3) {
        Set<E> set = new HashSet<E>(3, 1);
        set.add(e1);
        set.add(e2);
        set.add(e3);

        return Collections.unmodifiableSet(set);
    }

    public static <E> Set<E> of(final E e1, final E e2, final E e3, final E e4) {
        Set<E> set = new HashSet<E>(4, 1);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);

        return Collections.unmodifiableSet(set);
    }

}
