package net.nelz.simplesm.aop;

import net.nelz.simplesm.api.InvalidateAssignCache;
import net.nelz.simplesm.api.InvalidateSingleCache;
import net.nelz.simplesm.api.UpdateAssignCache;

/**
Copyright (c) 2008, 2009  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class AnnotationDataDummy {

    public static final String SAMPLE_KEY = "bigSampleKey";
    public static final String SAMPLE_NS = "bigSampleNamespace";
    public static final int SAMPLE_EXP = 42;

    @InvalidateSingleCache
    public void populateAssign01(final String key1) { }

    @InvalidateAssignCache
    public void populateAssign02(final String key1) { }

    @InvalidateAssignCache(assignedKey = "")
    public void populateAssign03(final String key1) { }

    @InvalidateAssignCache(assignedKey = SAMPLE_KEY)
    public void populateAssign04(final String key1) { }



    @InvalidateAssignCache
    public void populateNamespace01(final String key1) { }

    @InvalidateAssignCache(namespace = "")
    public void populateNamespace02(final String key1) { }

    @InvalidateAssignCache(namespace = SAMPLE_NS)
    public void populateNamespace03(final String key1) { }



    @InvalidateAssignCache
    public void populateExpiration01(final String key1) { }

    @UpdateAssignCache(expiration = -1)
    public void populateExpiration02(final String key1) { }

    @UpdateAssignCache(expiration = SAMPLE_EXP)
    public void populateExpiration03(final String key1) { }


    @InvalidateAssignCache
    public void populateClassName01(final String key1) { }


}
