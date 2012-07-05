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

package com.google.code.ssm.json;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.jsontype.impl.ClassNameIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.springframework.util.Assert;

/**
 * To minimalize size of serialized json object instead of full qualified class name each class can be registered under
 * alias (id). This alias (id) will be used in serialized string to mark type of object (required in deserialization).
 * Alias (id) must be unique! If alias is not defined for class then full qualified name is used.
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class ClassAliasIdResolver extends ClassNameIdResolver {

    private Map<String, Class<?>> idToClass = new HashMap<String, Class<?>>();

    private Map<Class<?>, String> classToId = new HashMap<Class<?>, String>();

    public ClassAliasIdResolver(final JavaType baseType, final TypeFactory typeFactory) {
        super(baseType, typeFactory);
    }

    @Override
    public JavaType typeFromId(final String id) {
        Class<?> clazz = idToClass.get(id);
        if (clazz != null) {
            return _typeFactory.constructSpecializedType(_baseType, clazz);
        }

        return super.typeFromId(id);
    }

    @Override
    public String idFromValue(final Object value) {
        String id = null;
        if (value != null && (id = classToId.get(value.getClass())) != null) {
            return id;
        }

        return _idFrom(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(final Object value, final Class<?> type) {
        String id = null;
        if (type != null && (id = classToId.get(type)) != null) {
            return id;
        }

        return _idFrom(value, type);
    }

    /**
     * Registers mappings between classes and aliases (ids).
     * 
     * @param classToId
     */
    public void setClassToId(final Map<Class<?>, String> classToId) {

        Map<String, Class<?>> reverseMap = new HashMap<String, Class<?>>();
        for (Map.Entry<Class<?>, String> entry : classToId.entrySet()) {
            Assert.notNull(entry.getKey(), "Class cannot be null: " + entry);
            Assert.hasText(entry.getValue(), "Alias (id) cannot be null or contain only whitespaces" + entry);

            if (reverseMap.put(entry.getValue(), entry.getKey()) != null) {
                throw new IllegalArgumentException("Two or more classes with the same alias: " + entry.getValue());
            }
        }

        this.classToId = classToId;
        this.idToClass = reverseMap;
    }

    /**
     * Adds single mapping: class <-> alias (id).
     * 
     * @param clazz
     * @param id
     */
    public void addClassToId(final Class<?> clazz, final String id) {
        Assert.notNull(clazz, "Class cannot be null");
        Assert.hasText(id, "Alias (id) cannot be null or contain only whitespaces");

        if (classToId.containsKey(clazz)) {
            throw new IllegalArgumentException("Class " + clazz + " has already defined alias (id) " + classToId.get(clazz)
                    + " cannot set another alias " + id);
        }

        if (idToClass.containsKey(id)) {
            throw new IllegalArgumentException("Alias (id) " + id + " is used by another class " + idToClass.get(id)
                    + " and cannot be used by " + clazz);
        }

        classToId.put(clazz, id);
        idToClass.put(id, clazz);
    }

}
