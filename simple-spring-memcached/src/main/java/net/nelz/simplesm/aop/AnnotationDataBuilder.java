package net.nelz.simplesm.aop;

import net.nelz.simplesm.api.*;

import java.security.InvalidParameterException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

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
class AnnotationDataBuilder {

    static final Set<Class> ASSIGNS = new HashSet<Class>();
    static final Set<Class> SINGLES = new HashSet<Class>();
    static final Set<Class> MULTIS = new HashSet<Class>();
    static final Set<Class> INVALIDATES = new HashSet<Class>();
    static final Set<Class> UPDATES = new HashSet<Class>();

    static {
        Collections.addAll(ASSIGNS,
                ReadThroughAssignCache.class,
                UpdateAssignCache.class,
                InvalidateAssignCache.class);

        Collections.addAll(SINGLES,
                ReadThroughSingleCache.class,
                UpdateSingleCache.class,
                InvalidateSingleCache.class);

        Collections.addAll(MULTIS,
                ReadThroughMultiCache.class,
                UpdateMultiCache.class,
                InvalidateMultiCache.class);

        Collections.addAll(UPDATES,
                UpdateSingleCache.class,
                UpdateMultiCache.class,
                UpdateAssignCache.class);

        Collections.addAll(INVALIDATES,
                InvalidateAssignCache.class,
                InvalidateSingleCache.class,
                InvalidateMultiCache.class);
    }

    static AnnotationData buildAnnotationData(final Annotation annotation,
                                              final Class expectedAnnotationClass,
                                              final String targetMethodName) {
        final AnnotationData data = new AnnotationData();
        final Class clazz = populateClassName(data, annotation, expectedAnnotationClass);

        try {
            populateKeyIndex(data, annotation, expectedAnnotationClass, targetMethodName, clazz);

            populateDataIndex(data, annotation, expectedAnnotationClass, targetMethodName, clazz);

            populateExpiration(data, annotation, expectedAnnotationClass, targetMethodName, clazz);

            populateNamespace(data, annotation, expectedAnnotationClass, targetMethodName, clazz);

            populateAssignedKey(data, annotation, expectedAnnotationClass, targetMethodName, clazz);

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }

    static AnnotationData buildAnnotationData(final Annotation annotation,
                                              final Class expectedAnnotationClass,
                                              final Method targetMethod) {
        final AnnotationData data = new AnnotationData();
        final Class clazz = populateClassName(data, annotation, expectedAnnotationClass);

        try {
            populateKeyIndex(data, annotation, expectedAnnotationClass, targetMethod.getName(), clazz);

            populateDataIndex(data, annotation, expectedAnnotationClass, targetMethod.getName(), clazz);

            populateExpiration(data, annotation, expectedAnnotationClass, targetMethod.getName(), clazz);

            populateNamespace(data, annotation, expectedAnnotationClass, targetMethod.getName(), clazz);

            populateAssignedKey(data, annotation, expectedAnnotationClass, targetMethod.getName(), clazz);

        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Problem assembling Annotation information.", ex);
        }

        return data;
    }

    private static void populateAssignedKey(final AnnotationData data,
                                            final Annotation annotation,
                                            final Class expectedAnnotationClass,
                                            final String targetMethodName,
                                            final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (ASSIGNS.contains(expectedAnnotationClass)) {
            final Method assignKeyMethod = clazz.getDeclaredMethod("assignedKey", null);
            final String assignKey = (String) assignKeyMethod.invoke(annotation, null);
            if (AnnotationConstants.DEFAULT_STRING.equals(assignKey)
                    || assignKey == null
                    || assignKey.length() < 1) {
                throw new InvalidParameterException(String.format(
                        "AssignedKey for annotation [%s] must be defined on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethodName
                ));
            }
            data.setAssignedKey(assignKey);
        }
    }

    private static void populateNamespace(final AnnotationData data,
                                          final Annotation annotation,
                                          final Class expectedAnnotationClass,
                                          final String targetMethodName,
                                          final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method namespaceMethod = clazz.getDeclaredMethod("namespace", null);
        final String namespace = (String) namespaceMethod.invoke(annotation, null);
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    expectedAnnotationClass.getName(),
                    targetMethodName
            ));
        }
        data.setNamespace(namespace);
    }

    private static void populateExpiration(final AnnotationData data,
                                           final Annotation annotation,
                                           final Class expectedAnnotationClass,
                                           final String targetMethodName,
                                           final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!INVALIDATES.contains(expectedAnnotationClass)) {
            final Method expirationMethod = clazz.getDeclaredMethod("expiration", null);
            final int expiration = (Integer) expirationMethod.invoke(annotation, null);
            if (expiration < 0) {
                throw new InvalidParameterException(String.format(
                        "Expiration for annotation [%s] must be 0 or greater on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethodName
                ));
            }
            data.setExpiration(expiration);
        }
    }

    private static void populateDataIndex(final AnnotationData data,
                                          final Annotation annotation,
                                          final Class expectedAnnotationClass,
                                          final String targetMethodName,
                                          final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (UPDATES.contains(expectedAnnotationClass)) {
            final Method dataIndexMethod = clazz.getDeclaredMethod("dataIndex", null);
            final int dataIndex = (Integer) dataIndexMethod.invoke(annotation, null);
            if (dataIndex < -1) {
                throw new InvalidParameterException(String.format(
                        "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethodName
                ));
            }
            data.setDataIndex(dataIndex);
        }
    }

    private static void populateKeyIndex(final AnnotationData data,
                                         final Annotation annotation,
                                         final Class expectedAnnotationClass,
                                         final String targetMethodName,
                                         final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!ASSIGNS.contains(expectedAnnotationClass)) {
            final Method keyIndexMethod = clazz.getDeclaredMethod("keyIndex", null);
            final int keyIndex = (Integer) keyIndexMethod.invoke(annotation, null);
            if (keyIndex < -1) {
                throw new InvalidParameterException(String.format(
                        "KeyIndex for annotation [%s] must be -1 or greater on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethodName
                ));
            }
            data.setKeyIndex(keyIndex);
        }
    }

    // TODO: Implement using Annotations to calculate the keyIndex.
    private static void populateKeyIndex(final AnnotationData data,
                                         final Annotation annotation,
                                         final Class expectedAnnotationClass,
                                         final Method targetMethod,
                                         final Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!ASSIGNS.contains(expectedAnnotationClass)) {
            final Method keyIndexMethod = clazz.getDeclaredMethod("keyIndex", null);
            final int keyIndex = (Integer) keyIndexMethod.invoke(annotation, null);
            if (keyIndex < -1) {
                throw new InvalidParameterException(String.format(
                        "KeyIndex for annotation [%s] must be -1 or greater on [%s]",
                        expectedAnnotationClass.getName(),
                        targetMethod.getName()
                ));
            }
            data.setKeyIndex(keyIndex);
        }
    }

    static Class populateClassName(final AnnotationData data,
                                   final Annotation annotation,
                                   final Class expectedAnnotationClass) {
        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    expectedAnnotationClass.getName()
            ));
        }

        final Class clazz = annotation.annotationType();
        if (!expectedAnnotationClass.equals(clazz)) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found, class was of type [%s].",
                    expectedAnnotationClass.getName(),
                    clazz.getName()
            ));
        }
        data.setClassName(clazz.getName());
        return clazz;
    }
}
