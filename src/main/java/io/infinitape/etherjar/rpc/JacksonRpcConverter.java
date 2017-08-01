/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.infinitape.etherjar.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.infinitape.etherjar.rpc.json.TraceItemJson;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.infinitape.etherjar.rpc.json.ResponseJson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JacksonRpcConverter implements RpcConverter {

    private ObjectMapper objectMapper;

    public JacksonRpcConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonRpcConverter() {
        this.objectMapper = createJsonMapper();
    }

    public ObjectMapper createJsonMapper() {
        SimpleModule module = new SimpleModule("EtherJar");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public String toJson(RequestJson request) throws JsonProcessingException {
        return objectMapper.writer().writeValueAsString(request);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(InputStream content, Class<T> target) throws IOException {
        if (TraceList.class.isAssignableFrom(target)) {
            return (T) fromJsonList(content, TraceItemJson.class);
        }
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, target);
        ResponseJson<T> responseJson = objectMapper.readerFor(type1).readValue(content);
        return responseJson.getResult();
    }

    public <T> List<T> fromJsonList(InputStream content, Class<T> target) throws IOException {
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(List.class, target);
        JavaType type2 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, type1);
        ResponseJson<List<T>> responseJson = objectMapper.readerFor(type2).readValue(content);
        return responseJson.getResult();
    }

}
