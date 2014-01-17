/*
 * Copyright (c) 2012-2014 Jakub Białek
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper.DefaultTypeResolverBuilder;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;
import org.springframework.util.Assert;

/**
 * Sets as a type resolver {@link ClassAliasIdResolver}.
 * 
 * @author Jakub Białek
 * @since 3.0.0
 * 
 */
public class ClassAliasTypeResolverBuilder extends DefaultTypeResolverBuilder {

    private Map<String, Class<?>> idToClass = new HashMap<String, Class<?>>();

    private Map<Class<?>, String> classToId = new HashMap<Class<?>, String>();

    public ClassAliasTypeResolverBuilder(final DefaultTyping typing) {
        super(typing);
    }

    @Override
    protected TypeIdResolver idResolver(final MapperConfig<?> config, final JavaType baseType, final Collection<NamedType> subtypes,
            final boolean forSer, final boolean forDeser) {
        return new ClassAliasIdResolver(baseType, config.getTypeFactory(), idToClass, classToId);
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
                throw new IllegalArgumentException("Two or more classes with the same alias (id): " + entry.getValue());
            }
        }

        this.classToId = classToId;
        this.idToClass = reverseMap;
    }

}
