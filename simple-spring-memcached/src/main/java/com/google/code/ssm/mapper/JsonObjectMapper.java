/*
 * Copyright (c) 2010-2014 Jakub Białek
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

package com.google.code.ssm.mapper;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.code.ssm.json.ClassAliasTypeResolverBuilder;

/**
 * 
 * Default jackson {@link ObjectMapper} initialized with enabled auto detecting of fields, getters and setters.
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
public class JsonObjectMapper extends ObjectMapper { // NO_UCD

    private static final long serialVersionUID = 1L;

    private final SimpleModule module = new SimpleModule("ssm", new Version(1, 0, 0, null, "com.google.code.ssm", "core"));

    private final ClassAliasTypeResolverBuilder typer;

    public JsonObjectMapper() {
        registerModule(module);

        configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        enableDefaultTyping(DefaultTyping.NON_FINAL, As.WRAPPER_OBJECT);

        typer = new ClassAliasTypeResolverBuilder(DefaultTyping.NON_FINAL);
        setDefaultTyping(typer.inclusion(As.WRAPPER_OBJECT));
    }

    public void setSerializers(final List<JsonSerializer<?>> serializers) {
        for (JsonSerializer<?> serializer : serializers) {
            module.addSerializer(serializer);
        }

        // required otherwise new serializers are not registered to context
        registerModule(module);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setSerializers(final Map<Class<?>, JsonSerializer<?>> serializers) {
        for (Map.Entry<Class<?>, JsonSerializer<?>> entry : serializers.entrySet()) {
            module.addSerializer((Class) entry.getKey(), entry.getValue());
        }

        // required otherwise new serializers are not registered to context
        registerModule(module);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setDeserializers(final Map<Class<?>, JsonDeserializer<?>> deserializers) {
        for (Map.Entry<Class<?>, JsonDeserializer<?>> entry : deserializers.entrySet()) {
            module.addDeserializer((Class) entry.getKey(), entry.getValue());
        }

        // required otherwise new deserializers are not registered to context
        registerModule(module);
    }

    /**
     * Registers mappings between classes and aliases (ids).
     * 
     * @param classToId
     * @since 3.0.0
     */
    public void setClassToId(final Map<Class<?>, String> classToId) {
        typer.setClassToId(classToId);
    }

}
