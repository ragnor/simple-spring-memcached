/* Copyright (c) 2012-2014 Jakub Białek
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

package com.google.code.ssm.transcoders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.code.ssm.mapper.JsonObjectMapper;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.test.Point;

/**
 * 
 * @author Jakub Białek
 * 
 */
public class JsonTranscoderTest {

    private JsonTranscoder transcoder;

    @Test
    public void testEncodeAndDecode() {
        transcoder = new JsonTranscoder(new JsonObjectMapper());

        Point p = new Point(40, 50);

        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());

        Point p2 = (Point) transcoder.decode(co);
        assertNotNull(p2);
        assertEquals(p, p2);
    }

    @Test
    public void testEncodeAndDecodeWithCustomSerializer() {
        JsonObjectMapper mapper = new JsonObjectMapper();

        Map<Class<?>, JsonSerializer<?>> serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(Point.class, new PointSerializer());
        mapper.setSerializers(serializers);

        transcoder = new JsonTranscoder(mapper);

        Point p = new Point(40, 50);

        // it won't be possible to deserialize it because no custom deserializer is registered and serialized data
        // doesn't contain info about class (type of serialized object), so the Jackson cannot figure out what
        // deserializer should be used
        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());
        assertEquals("{\"v\":\"40x50\"}", new String(co.getData()));
    }

    @Test
    public void testEncodeAndDecodeRegisterSerializerDirectlyToModule() {
        JsonObjectMapper mapper = new JsonObjectMapper();

        // first add serializer then register module
        SimpleModule module = new SimpleModule("cemo", Version.unknownVersion());
        module.addSerializer(Point.class, new PointSerializer());
        mapper.registerModule(module);

        transcoder = new JsonTranscoder(mapper);

        Point p = new Point(40, 50);

        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());
        assertEquals("{\"v\":\"40x50\"}", new String(co.getData()));
    }

    @Test
    public void testEncodeAndDecodeWithCustomSerializerAndDeserializerWithTypeInfoInside() {
        JsonObjectMapper mapper = createMapper();

        transcoder = new JsonTranscoder(mapper);

        Point p = new Point(40, 50);

        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());
        assertEquals("{\"v\":{\"com.google.code.ssm.test.Point\":{\"c\":\"40x50\"}}}", new String(co.getData()));

        Point p2 = (Point) transcoder.decode(co);
        assertNotNull(p2);
        assertEquals(p, p2);
    }

    @Test
    public void testEncodeAndDecodeWithCustomSerializerAndDeserializerWithTypeInfoInsideAndShorterClassIdentifier() {
        JsonObjectMapper mapper = createMapper();
        mapper.setClassToId(Collections.<Class<?>, String> singletonMap(Point.class, "point"));

        transcoder = new JsonTranscoder(mapper);

        Point p = new Point(40, 50);

        CachedObject co = transcoder.encode(p);
        assertNotNull(co);
        assertNotNull(co.getData());
        assertEquals("{\"v\":{\"point\":{\"c\":\"40x50\"}}}", new String(co.getData()));

        Point p2 = (Point) transcoder.decode(co);
        assertNotNull(p2);
        assertEquals(p, p2);
    }

    @Test
    public void testEncodeAndDecodeListWithCustomSerializerAndDeserializerWithTypeInfoInsideAndShorterClassIdentifier() {
        JsonObjectMapper mapper = createMapper();
        mapper.setClassToId(Collections.<Class<?>, String> singletonMap(Point.class, "point"));

        transcoder = new JsonTranscoder(mapper);

        List<Point> list = Arrays.asList(new Point(40, 50), new Point(10, 50));

        CachedObject co = transcoder.encode(list);
        assertNotNull(co);
        assertNotNull(co.getData());
        assertEquals("{\"v\":{\"java.util.ArrayList\":[{\"point\":{\"c\":\"40x50\"}},{\"point\":{\"c\":\"10x50\"}}]}}",
                new String(co.getData()));

        @SuppressWarnings("unchecked")
        List<Point> list2 = (List<Point>) transcoder.decode(co);
        assertNotNull(list2);
        assertEquals(list, list2);
    }

    private JsonObjectMapper createMapper() {
        JsonObjectMapper mapper = new JsonObjectMapper();

        Map<Class<?>, JsonSerializer<?>> serializers = new HashMap<Class<?>, JsonSerializer<?>>();
        serializers.put(Point.class, new PointSerializerWithTypeInfo());
        mapper.setSerializers(serializers);

        Map<Class<?>, JsonDeserializer<?>> deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
        deserializers.put(Point.class, new PointDeserializer());
        mapper.setDeserializers(deserializers);

        return mapper;
    }

    static class PointSerializer extends JsonSerializer<Point> {
        @Override
        public void serialize(final Point value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeString(value.getX() + "x" + value.getY());
        }

        @Override
        public void serializeWithType(final Point value, final JsonGenerator jgen, final SerializerProvider provider,
                final TypeSerializer typeSer) throws IOException, JsonProcessingException {
            serialize(value, jgen, provider);
        }
    }

    /**
     * Stores information of Point type, it is used by Jackson to decide what deserializer should be used.
     * 
     */
    static class PointSerializerWithTypeInfo extends JsonSerializer<Point> {
        @Override
        public void serialize(final Point value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            jgen.writeFieldName("c");
            jgen.writeString(value.getX() + "x" + value.getY());
        }

        @Override
        public void serializeWithType(final Point value, final JsonGenerator jgen, final SerializerProvider provider,
                final TypeSerializer typeSer) throws IOException, JsonProcessingException {
            typeSer.writeTypePrefixForObject(value, jgen);
            serialize(value, jgen, provider);
            typeSer.writeTypeSuffixForObject(value, jgen);
        }

    }

    static class PointDeserializer extends JsonDeserializer<Point> {

        @Override
        public Point deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            jp.nextToken();
            String value = jp.nextTextValue();
            jp.nextToken();
            String[] parts = value.split("x");
            return new Point(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
        }

    }

}
