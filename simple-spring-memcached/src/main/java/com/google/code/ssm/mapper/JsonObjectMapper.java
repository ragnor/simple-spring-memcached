package com.google.code.ssm.mapper;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.stereotype.Component;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
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
 * Default jackson {@link ObjectMapper} initialized with disabled auto detecting of fields, getters and setters.
 * 
 * @author Jakub Białek
 * 
 */
@Component("jsonObjectMapper")
public class JsonObjectMapper extends ObjectMapper {

    public JsonObjectMapper() {
        setSerializationConfig(getSerializationConfig().without( //
                SerializationConfig.Feature.AUTO_DETECT_FIELDS, //
                SerializationConfig.Feature.AUTO_DETECT_GETTERS, //
                SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS));

        setDeserializationConfig(getDeserializationConfig().without( //
                DeserializationConfig.Feature.AUTO_DETECT_FIELDS, //
                DeserializationConfig.Feature.AUTO_DETECT_SETTERS));
    }

}
