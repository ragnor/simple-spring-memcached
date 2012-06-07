/* Copyright (c) 2012 Jakub Białek
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class ClassAliasIdResolverTest {

    private ClassAliasIdResolver resolver;

    private JavaType baseType;

    private TypeFactory typeFactory;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() {
        baseType = Mockito.mock(JavaType.class);
        typeFactory = TypeFactory.instance;

        resolver = new ClassAliasIdResolver(baseType, typeFactory);
    }

    @Test
    public void setClassToIdMap() {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(ClassAliasIdResolverTest.class, "cairt");

        resolver.setClassToId(map);
        String id = resolver.idFromValue(new ClassAliasIdResolverTest());
        assertNotNull(id);
        assertEquals("cairt", id);

        id = resolver.idFromValue(new Object());
        assertEquals(Object.class.getName(), id);
    }

    @Test
    public void addClassToId() {
        resolver.addClassToId(ClassAliasIdResolverTest.class, "cairt");
        String id = resolver.idFromValue(new ClassAliasIdResolverTest());
        assertNotNull(id);
        assertEquals("cairt", id);

        id = resolver.idFromValue(new Object());
        assertEquals(Object.class.getName(), id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSetClassToIdMapNotUniqueAlias() {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(ClassAliasIdResolverTest.class, "cairt");
        map.put(Object.class, "cairt");

        resolver.setClassToId(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSetClassToIdMapNullAlias() {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(ClassAliasIdResolverTest.class, "cairt");
        map.put(Object.class, null);

        resolver.setClassToId(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSetClassToIdMapEmptyAlias() {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(ClassAliasIdResolverTest.class, "cairt");
        map.put(Object.class, "");

        resolver.setClassToId(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAddClassToIdNullClass() {
        resolver.addClassToId(null, "id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAddClassToIdMapNullAlias() {
        resolver.addClassToId(ClassAliasIdResolverTest.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAddClassToIdMapEmptyAlias() {
        resolver.addClassToId(ClassAliasIdResolverTest.class, "");
    }

}
