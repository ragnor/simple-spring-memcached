/*
 * Copyright (c) 2008-2012 Nelson Carpentier, Jakub Białek
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

package com.google.code.ssm.aop.support;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.code.ssm.api.AnnotationConstants;
import com.google.code.ssm.api.CacheName;
import com.google.code.ssm.api.InvalidateAssignCache;
import com.google.code.ssm.api.InvalidateMultiCache;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterDataUpdateContent;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughAssignCache;
import com.google.code.ssm.api.ReadThroughMultiCache;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.ReturnDataUpdateContent;
import com.google.code.ssm.api.ReturnValueKeyProvider;
import com.google.code.ssm.api.UpdateAssignCache;
import com.google.code.ssm.api.UpdateMultiCache;
import com.google.code.ssm.api.UpdateSingleCache;
import com.google.code.ssm.api.counter.DecrementCounterInCache;
import com.google.code.ssm.api.counter.IncrementCounterInCache;
import com.google.code.ssm.api.counter.ReadCounterFromCache;
import com.google.code.ssm.api.counter.UpdateCounterInCache;
import com.google.code.ssm.util.ImmutableSet;
import com.google.code.ssm.util.Utils;

/**
 * 
 * @author Nelson Carpentier
 * @author Jakub Białek
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
     */
    private static final Set<Class<? extends Annotation>> MULTIS = ImmutableSet.of( //
            ReadThroughMultiCache.class, //
            UpdateMultiCache.class, //
            InvalidateMultiCache.class);

    private static final Set<Class<? extends Annotation>> READS = ImmutableSet.of( //
            ReadThroughAssignCache.class, //
            ReadThroughSingleCache.class, //
            ReadThroughMultiCache.class, //
            ReadCounterFromCache.class);

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
            populateCacheName(data, targetMethod);

            populateKeyIndexes(data, expectedAnnotationClass, targetMethod);

            populateDataIndexFromAnnotations(data, expectedAnnotationClass, targetMethod);

            populateExpiration(data, annotation, expectedAnnotationClass, targetMethod.getName());

            populateNamespace(data, annotation, expectedAnnotationClass, targetMethod.getName());

            populateAssignedKey(data, annotation, expectedAnnotationClass, targetMethod.getName());

            populateListKeyIndex(data, expectedAnnotationClass, targetMethod);

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }

    static void populateCacheName(final AnnotationData data, final Method targetMethod) {
        // get annotation on cached method
        CacheName cache = targetMethod.getAnnotation(CacheName.class);
        if (cache == null) {
            // if annotation on cached method doesn't exist try to get annotation on class
            cache = targetMethod.getDeclaringClass().getAnnotation(CacheName.class);
        }

        if (cache != null) {
            // set cache zone name only if annotation is present otherwise default value will be used
            data.setCacheName(cache.value());
        }
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
            data.setReturnDataIndex(true);
            return;
        }

        final Annotation[][] paramAnnotationArrays = targetMethod.getParameterAnnotations();
        int foundIndex = Integer.MIN_VALUE;

        if (paramAnnotationArrays != null && paramAnnotationArrays.length > 0) {
            for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
                if (getAnnotation(ParameterDataUpdateContent.class, paramAnnotationArrays[ix]) != null) {
                    if (foundIndex >= 0) {
                        throw new InvalidParameterException(String.format("Multiple annotations of type [%s] found on method [%s]",
                                ParameterDataUpdateContent.class.getName(), targetMethod.getName()));
                    }
                    foundIndex = ix;
                }
            }
        }

        if (foundIndex < 0) {
            throw new InvalidParameterException(String.format(
                    "No ReturnDataUpdateContent or ParameterDataUpdateContent annotation found method [%s]", targetMethod.getName()));
        }

        data.setDataIndex(foundIndex);
    }

    static void populateKeyIndexes(final AnnotationData data, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (ASSIGNS.contains(expectedAnnotationClass)) {
            return;
        }

        // Read*Cache annotations don't support ReturnValueKeyProvider, to cache method result cache keys must be
        // available before executing method.
        if (!READS.contains(expectedAnnotationClass)) {
            final ReturnValueKeyProvider returnAnnotation = targetMethod.getAnnotation(ReturnValueKeyProvider.class);
            if (returnAnnotation != null) {
                data.setReturnKeyIndex(true);
                return;
            }
        }

        final Annotation[][] paramAnnotationArrays = targetMethod.getParameterAnnotations();
        SortedMap<ParameterValueKeyProvider, Integer> founds = new TreeMap<ParameterValueKeyProvider, Integer>(COMPARATOR);
        Set<Integer> order = new HashSet<Integer>();

        if (paramAnnotationArrays != null && paramAnnotationArrays.length > 0) {
            ParameterValueKeyProvider foundAnnotation;
            for (int ix = 0; ix < paramAnnotationArrays.length; ix++) {
                foundAnnotation = getAnnotation(ParameterValueKeyProvider.class, paramAnnotationArrays[ix]);
                if (foundAnnotation != null) {
                    // throw if order below 0
                    if (foundAnnotation.order() < 0) {
                        throw new InvalidParameterException(String.format(
                                "No valid order [%d] defined in annotation [%s] on method [%s], only no negative integers are allowed.",
                                foundAnnotation.order(), ParameterValueKeyProvider.class.getName(), targetMethod.getName()));
                    }
                    // throw exception if there are two annotations with the same value of order
                    if (!order.add(foundAnnotation.order())) {
                        throw new InvalidParameterException(String.format("No valid order defined in annotation [%s] on method [%s]. "
                                + "There are two annotations with the same order.", ParameterValueKeyProvider.class.getName(),
                                targetMethod.getName()));
                    }

                    founds.put(foundAnnotation, ix);
                }
            }
        }

        if (founds.isEmpty()) {
            throw new InvalidParameterException(String.format("No KeyProvider annotation found method [%s]", targetMethod.getName()));
        }

        data.setKeyIndexes(founds.values());
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

    static void populateListKeyIndex(final AnnotationData data, final Class<? extends Annotation> expectedAnnotationClass,
            final Method targetMethod) {
        if (data.getKeyIndexes().isEmpty() || !MULTIS.contains(expectedAnnotationClass)) {
            return;
        }

        Class<?>[] keyMethodParamTypes = Utils.getMethodParameterTypes(data.getKeyIndexes(), targetMethod);
        Integer[] keyIndexArray = data.getKeyIndexes().toArray(new Integer[data.getKeyIndexes().size()]);

        boolean listOccured = false;
        for (int i = 0; i < keyMethodParamTypes.length; i++) {
            if (verifyTypeIsList(keyMethodParamTypes[i])) {
                if (listOccured) {
                    throw new InvalidAnnotationException(
                            "There are more than one method's parameter annotated by @ParameterValueKeyProvider that is list "
                                    + targetMethod.toString());
                }
                listOccured = true;
                data.setListIndexInKeys(i);
                data.setListIndexInMethodArgs(keyIndexArray[i]);
            }
        }

        if (!listOccured) {
            throw new InvalidAnnotationException(String.format("No one parameter objects found at dataIndexes [%s] is not a [%s]. "
                    + "[%s] does not fulfill the requirements.", data.getKeyIndexes(), List.class.getName(), targetMethod.toString()));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T getAnnotation(final Class<T> annotationClass, final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (final Annotation annotation : annotations) {
                if (annotationClass.equals(annotation.annotationType())) {
                    return (T) annotation;
                }
            }
        }

        return null;
    }

    private static boolean verifyTypeIsList(final Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    private static class ParameterValueKeyProviderComparator implements Comparator<ParameterValueKeyProvider>, Serializable {

        private static final long serialVersionUID = 2791887056140560908L;

        @Override
        public int compare(final ParameterValueKeyProvider o1, final ParameterValueKeyProvider o2) {
            return o1.order() - o2.order();
        }

    }

}
