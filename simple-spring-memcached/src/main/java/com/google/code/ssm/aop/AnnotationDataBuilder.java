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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.code.ssm.api.AnnotationConstants;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateMultiCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughAssignCache;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.UpdateAssignCache;
import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateSingleCache;
import com.google.code.ssm.api.counter.DecrementCounterInCache;
import com.google.code.ssm.api.counter.IncrementCounterInCache;
import com.google.code.ssm.api.counter.UpdateCounterInCache;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * @author Nelson Carpentier, Jakub Białek
 * 
 */
public class AnnotationDataBuilder {

    private static final Comparator<ParameterValueKeyProvider> COMPARATOR = new ParameterValueKeyProviderComparator();

    private static final Set<Class<? extends Annotation>> ASSIGNS = ImmutableSet.of( //
            ReadThroughAssignCache.class, //
            UpdateAssignCache.class, //
            InvalidateAssignCache.class);

    /*
     * private static final Set<Class<? extends Annotation>> SINGLES = ImmutableSet.of( // ReadThroughSingleCache.class,
     * // UpdateSingleCache.class, // InvalidateSingleCache.class, // ReadCounterFromCache.class, //
     * UpdateCounterInCache.class);
     * 
     * private static final Set<Class<? extends Annotation>> MULTIS = ImmutableSet.of( // ReadThroughMultiCache.class,
     * // UpdateMultiCache.class, // InvalidateMultiCache.class);
     */

    private static final Set<Class<? extends Annotation>> INVALIDATES = ImmutableSet.of( //
            InvalidateAssignCache.class, //
            InvalidateSingleCache.class, //
            InvalidateMultiCache.class);

    private static final Set<Class<? extends Annotation>> UPDATES = ImmutableSet.of( //
            UpdateSingleCache.class, //
            UpdateMultiCache.class, //
            UpdateAssignCache.class, //
            UpdateCounterInCache.class);

    private static final Set<Class<? extends Annotation>> INCDEC = ImmutableSet.of( //
            IncrementCounterInCache.class, //
            DecrementCounterInCache.class);

    public static AnnotationData buildAnnotationData(final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final Method targetMethod) {
        final AnnotationData data = new AnnotationData();
        populateClassName(data, annotation, expectedAnnotationClass);

        try {
            populateKeyIndexAndBeanName(data, expectedAnnotationClass, targetMethod);

            populateDataIndexFromAnnotations(data, expectedAnnotationClass, targetMethod);

            populateExpiration(data, annotation, expectedAnnotationClass, targetMethod.getName());

            populateNamespace(data, annotation, expectedAnnotationClass, targetMethod.getName());

            populateAssignedKey(data, annotation, expectedAnnotationClass, targetMethod.getName());

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }

    static void populateAssignedKey(final AnnotationData data, final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final String targetMethodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        if (ASSIGNS.contains(expectedAnnotationClass)) {
            final Method assignKeyMethod = expectedAnnotationClass.getDeclaredMethod("assignedKey", (Class<?>[]) null);
            final String assignKey = (String) assignKeyMethod.invoke(annotation, (Object[]) null);
            if (AnnotationConstants.DEFAULT_STRING.equals(assignKey) || assignKey == null || assignKey.length() < 1) {
                throw new InvalidParameterException(String.format("AssignedKey for annotation [%s] must be defined on [%s]",
                        expectedAnnotationClass.getName(), targetMethodName));
            }
            data.setAssignedKey(assignKey);
        }
    }

    static void populateNamespace(final AnnotationData data, final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final String targetMethodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        final Method namespaceMethod = expectedAnnotationClass.getDeclaredMethod("namespace", (Class<?>[]) null);
        final String namespace = (String) namespaceMethod.invoke(annotation, (Object[]) null);
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace) || namespace == null || namespace.length() < 1) {
            throw new InvalidParameterException(String.format("Namespace for annotation [%s] must be defined on [%s]",
                    expectedAnnotationClass.getName(), targetMethodName));
        }
        data.setNamespace(namespace);
    }

    static void populateExpiration(final AnnotationData data, final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass, final String targetMethodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        if (!INVALIDATES.contains(expectedAnnotationClass) && !INCDEC.contains(expectedAnnotationClass)) {
            final Method expirationMethod = expectedAnnotationClass.getDeclaredMethod("expiration", (Class<?>[]) null);
            final int expiration = (Integer) expirationMethod.invoke(annotation, (Object[]) null);
            if (expiration < 0) {
                throw new InvalidParameterException(String.format("Expiration for annotation [%s] must be 0 or greater on [%s]",
                        expectedAnnotationClass.getName(), targetMethodName));
            }
            data.setExpiration(expiration);
        }
    }

    static void populateDataIndexFromAnnotations(final AnnotationData data, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!UPDATES.contains(expectedAnnotationClass)) {
            return;
        }

        final ReturnDataUpdateContent returnAnnotation = targetMethod.getAnnotation(ReturnDataUpdateContent.class);
        if (returnAnnotation != null) {
            if (targetMethod.getReturnType().equals(void.class)) {
                throw new InvalidParameterException(String.format("Annotation [%s] is defined on void method  [%s]", returnAnnotation,
                        targetMethod.getName()));
            }
            data.setDataIndex(AnnotationData.RETURN_INDEX);
            return;
        }

        final Annotation[][] paramAnnotationArrays = targetMethod.getParameterAnnotations();
        int foundIndex = Integer.MIN_VALUE;

        if (paramAnnotationArrays != null && paramAnnotationArrays.length > 0) {
            for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
                final Annotation[] paramAnnotations = paramAnnotationArrays[ix];
                if (paramAnnotations != null && paramAnnotations.length > 0) {
                    for (int jx = 0; jx < paramAnnotations.length; jx++) {
                        final Annotation paramAnnotation = paramAnnotations[jx];
                        if (ParameterDataUpdateContent.class.equals(paramAnnotation.annotationType())) {
                            if (foundIndex >= 0) {
                                throw new InvalidParameterException(String.format("Multiple annotations of type [%s] found on method [%s]",
                                        ParameterDataUpdateContent.class.getName(), targetMethod.getName()));
                            }
                            foundIndex = ix;
                        }
                    }
                }
            }
        }

        if (foundIndex < 0) {
            throw new InvalidParameterException(String.format(
                    "No ReturnDataUpdateContent or ParameterDataUpdateContent annotation found method [%s]", targetMethod.getName()));
        }

        data.setDataIndex(foundIndex);
    }

    static void populateKeyIndexAndBeanName(final AnnotationData data, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (ASSIGNS.contains(expectedAnnotationClass)) {
            return;
        }

        final ReturnValueKeyProvider returnAnnotation = targetMethod.getAnnotation(ReturnValueKeyProvider.class);
        if (returnAnnotation != null) {
            /*
             * final String beanName = returnAnnotation.keyProviderBeanName(); if (beanName == null || beanName.length()
             * < 1) { throw new
             * InvalidParameterException(String.format("No valid keyIndexBeanName defined in annotation [%s] on method [%s]"
             * , ReturnValueKeyProvider.class.getName(), targetMethod.getName())); }
             */
            data.setKeyIndex(AnnotationData.RETURN_INDEX);
            // data.setKeyProviderBeanName(beanName);
            return;
        }

        final Annotation[][] paramAnnotationArrays = targetMethod.getParameterAnnotations();
        int foundIndex = Integer.MIN_VALUE;
        ParameterValueKeyProvider foundAnnotation = null;
        SortedMap<ParameterValueKeyProvider, Integer> founds = new TreeMap<ParameterValueKeyProvider, Integer>(COMPARATOR);
        Set<Integer> order = new HashSet<Integer>();

        if (paramAnnotationArrays != null && paramAnnotationArrays.length > 0) {
            for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
                final Annotation[] paramAnnotations = paramAnnotationArrays[ix];
                if (paramAnnotations != null && paramAnnotations.length > 0) {
                    for (int jx = 0; jx < paramAnnotations.length; jx++) {
                        final Annotation paramAnnotation = paramAnnotations[jx];
                        if (ParameterValueKeyProvider.class.equals(paramAnnotation.annotationType())) {
                            foundAnnotation = (ParameterValueKeyProvider) paramAnnotation;
                            foundIndex = ix;
                            // throw if order below 0
                            if (foundAnnotation.order() < 0) {
                                throw new InvalidParameterException(
                                        String.format(
                                                "No valid order [%d] defined in annotation [%s] on method [%s], only no negative integers are allowed.",
                                                foundAnnotation.order(), ParameterValueKeyProvider.class.getName(), targetMethod.getName()));
                            }
                            // throw exception if there are two annotations with the same value of order
                            if (!order.add(foundAnnotation.order())) {
                                throw new InvalidParameterException(String.format(
                                        "No valid order defined in annotation [%s] on method [%s]. "
                                                + "There are two annotations with the same order.",
                                        ParameterValueKeyProvider.class.getName(), targetMethod.getName()));
                            }

                            founds.put(foundAnnotation, foundIndex);
                            /*
                             * final String beanName = foundAnnotation.keyProviderBeanName(); if (beanName == null ||
                             * beanName.length() < 1) { throw new InvalidParameterException(String.format(
                             * "No valid beanName defined in annotation [%s] on method [%s]",
                             * ParameterValueKeyProvider.class.getName(), targetMethod.getName())); }
                             */
                            // TODO KeyProvider is not supported (issue with many ParameterValueKeyProvider in one
                            // method), this line is only to pass prev junit tests
                            // data.setKeyProviderBeanName(beanName);
                        }
                    }
                }
            }
        }

        if (founds.isEmpty()) {
            throw new InvalidParameterException(String.format("No KeyProvider annotation found method [%s]", targetMethod.getName()));
        }

        data.setKeyIndex(foundIndex);
        data.setKeysIndex(founds.values());
    }

    static void populateClassName(final AnnotationData data, final Annotation annotation,
            final Class<? extends Annotation> expectedAnnotationClass) {
        if (annotation == null) {
            throw new InvalidParameterException(String.format("No annotation of type [%s] found.", expectedAnnotationClass.getName()));
        }

        final Class<? extends Annotation> clazz = annotation.annotationType();
        if (!expectedAnnotationClass.equals(clazz)) {
            throw new InvalidParameterException(String.format("No annotation of type [%s] found, class was of type [%s].",
                    expectedAnnotationClass.getName(), clazz.getName()));
        }
        data.setClassName(clazz.getName());
    }

    private static class ParameterValueKeyProviderComparator implements Comparator<ParameterValueKeyProvider>, Serializable {

        private static final long serialVersionUID = 2791887056140560908L;

        @Override
        public int compare(ParameterValueKeyProvider o1, ParameterValueKeyProvider o2) {
            return o1.order() - o2.order();
        }

    }

}
