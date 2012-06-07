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

import java.util.Collection;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper.DefaultTypeResolverBuilder;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

/**
 * Sets as a type resolver {@link ClassAliasIdResolver}.
 * 
 * @author Jakub Białek
 * @since 2.1.0
 * 
 */
public class ClassAliasTypeResolverBuilder extends DefaultTypeResolverBuilder {

    public ClassAliasTypeResolverBuilder(final DefaultTyping typing) {
        super(typing);
    }

    @Override
    protected TypeIdResolver idResolver(final MapperConfig<?> config, final JavaType baseType, final Collection<NamedType> subtypes,
            final boolean forSer, final boolean forDeser) {
        return new ClassAliasIdResolver(baseType, config.getTypeFactory());
    }

}
