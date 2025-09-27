/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rpc.json.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@NullMarked
public class JacksonRpcConverter implements RpcConverter {

    private final ObjectMapper objectMapper;

    public JacksonRpcConverter(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        this.objectMapper = objectMapper;
    }

    public JacksonRpcConverter() {
        this.objectMapper = createJsonMapper();
    }

    public static ObjectMapper createJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new EtherjarModule());
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String toJson(ResponseJson response) throws JsonProcessingException {
        return objectMapper.writer().writeValueAsString(response);
    }

    @Override
    public String toJson(RequestJson request) {
        try {
            return objectMapper.writer().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize to JSON", e);
        }
    }

    @Override
    public String toJson(List<RequestJson<Integer>> batch) {
        try {
            return objectMapper.writer().writeValueAsString(batch);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize to JSON", e);
        }
    }

    public <T> T fromJson(InputStream content, Class<T> target) throws RpcException {
        return fromJson(content, objectMapper.getTypeFactory().constructType(target), Integer.class);
    }

    @Override
    public <T> T fromJson(InputStream content, JavaType target) throws RpcException {
        return fromJson(content, target, Integer.class);
    }

    public <T, X> T fromJson(InputStream content, Class<T> target, Class<X> idtype) throws RpcException {
        return fromJson(content, objectMapper.getTypeFactory().constructType(target), idtype);
    }

    public <T, X> T fromJson(InputStream content, JavaType target, Class<X> idtype) throws RpcException {
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(FullResponseJson.class, target, objectMapper.getTypeFactory().constructType(idtype));
        FullResponseJson<T, X> responseJson;
        try {
            responseJson = objectMapper.readerFor(type1).readValue(content);
        } catch (IOException e) {
            throw new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Invalid JSON received from RPC endpoint: " + e.getMessage());
        }
        if (responseJson.hasError()) {
            RpcResponseError error = responseJson.getError();
            throw new RpcException(error.getCode(), error.getMessage(), error.getData());
        }
        return responseJson.getResult();
    }

    public <T> T fromJsonResult(InputStream content, JavaType target) throws RpcException {
        try {
            return objectMapper.readerFor(target).readValue(content);
        } catch (IOException e) {
            throw new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Invalid JSON received from RPC endpoint: " + e.getMessage());
        }
    }

    @Override
    public List<ResponseJson<Object, Integer>> parseBatch(InputStream content, Map<Integer, JavaType> targets) throws RpcException {
        try {
            JsonNode nodes = objectMapper.reader().readTree(content);
            if (!nodes.isArray()) {
                throw new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Not array");
            }
            Iterator<JsonNode> elements = nodes.elements();
            List<ResponseJson<Object, Integer>> parsedBatch = new ArrayList<>();
            while (elements.hasNext()) {
                JsonNode resp = elements.next();
                if (!resp.isObject()) {
                    continue;
                }
                Integer id = resp.get("id").asInt();
                if (!targets.containsKey(id)) {
                    continue;
                }
                JavaType[] inner = new JavaType[] { targets.get(id), objectMapper.getTypeFactory().constructType(Integer.TYPE) };
                JavaType type1 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, inner);
                ResponseJson<Object, Integer> parsedItem = objectMapper.reader().forType(type1).readValue(resp);
                parsedBatch.add(parsedItem);
            }
            return parsedBatch;
        } catch (IOException e) {
            throw new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, e.getMessage());
        }
    }

}
