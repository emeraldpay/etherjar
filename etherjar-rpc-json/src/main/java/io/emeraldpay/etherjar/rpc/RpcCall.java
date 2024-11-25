/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * JSON RPC call
 *
 * @param <JS>  JSON data type, returned by RPC server
 * @param <RES> Java data type, converted from JSON data type
 */
public class RpcCall<JS, RES> {

    private final String method;
    private final List params;

    private JavaType jsonType;
    private Class<? extends RES> resultType;
    private java.util.function.Function<JS, RES> converter;
    private boolean isArray = false;

    private RpcCall(String method, List params) {
        if (method == null) {
            throw new IllegalArgumentException("Method must be not null");
        }
        method = method.trim();
        if ("".equals(method)) {
            throw new IllegalArgumentException("Method must be not empty");
        }
        this.method = method;
        if (params == null) {
            params = Collections.emptyList();
        }
        this.params = params;
    }

    /**
     *
     * @param method method name
     * @param type data type
     * @param params call parameters
     * @param <T> data type, same for Java and JSON (i.e. String)
     * @return call definition
     */
    public static <T> RpcCall<T, T> create(String method, Class<? extends T> type, List params) {
        return create(method, TypeFactory.defaultInstance().constructType(type), params);
    }

    /**
     *
     * @param method method name
     * @param type data type
     * @param params call parameters
     * @param <T> data type, same for Java and JSON (i.e. String)
     * @return call definition
     */
    @SuppressWarnings("unchecked")
    public static <T> RpcCall<T, T> create(String method, JavaType type, List params) {
        RpcCall<T, T> call = new RpcCall<>(method, params);
        call.jsonType = TypeFactory.defaultInstance().constructType(type);
        call.resultType = (Class<? extends T>) type.getRawClass();
        call.converter = Function.identity();
        return call;
    }

    /**
     * Creates call for String data type
     *
     * @param method method name
     * @param params call parameters
     * @return call definition
     */
    public static RpcCall<String, String> create(String method, List params) {
        return create(method, String.class, params);
    }

    /**
     *
     * @param method method name
     * @param type data type
     * @param params call parameters
     * @param <T> data type, same for Java and JSON (i.e. String)
     * @return call definition
     */

    public static <T> RpcCall<T, T> create(String method, Class<T> type, Object ... params) {
        return create(method, type, Arrays.asList(params));
    }

    /**
     * Creates call for String data type
     *
     * @param method method name
     * @param params call parameters
     * @return call definition
     */
    public static RpcCall<String, String> create(String method, Object ... params) {
        return create(method, String.class, params);
    }

    /**
     * Creates call with empty parameters list
     *
     * @param method method name
     * @param type data type
     * @param <T> data type, same for Java and JSON (i.e. String)
     * @return call definition
     */
    public static <T> RpcCall<T, T> create(String method, Class<T> type) {
        return create(method, type, Collections.emptyList());
    }

    /**
     * Creates call with empty parameters list, for String data type
     *
     * @param method method name
     * @return call definition
     */
    public static RpcCall<String, String> create(String method) {
        return create(method, String.class, Collections.emptyList());
    }

    /**
     * Setup conversion to a Java data type
     *
     * @param resultType Java data type
     * @param converter function that converts from JSON type to Java type
     * @param <T> Java data type
     * @return call definition
     */
    public <T> RpcCall<JS, T> converted(Class<T> resultType, java.util.function.Function<JS, T> converter) {
        RpcCall<JS, T> call = new RpcCall<>(this.method, this.params);
        call.jsonType = this.jsonType;
        call.resultType = resultType;
        call.converter = converter;
        return call;
    }

    /**
     *
     * @param clazz JSON data type
     */
    @SuppressWarnings("unchecked")
    public <T> RpcCall<T, RES> castJsonType(Class<T> clazz) {
        if (this.jsonType == null || clazz.isAssignableFrom(this.jsonType.getRawClass())) {
            return (RpcCall<T, RES>) this;
        }
        throw new ClassCastException("Value of " + this.jsonType + " is not assignable to " + clazz);
    }

    /**
     *
     * @param clazz Java data type
     */
    @SuppressWarnings("unchecked")
    public void setResultType(Class clazz) {
        if (isArray) {
            throw new IllegalStateException("Cannot change result type after enabling array type");
        }
        this.resultType = clazz;
    }

    /**
     * Convert into a new call definition with specified JSON data type
     *
     * @param clazz new JSON data type
     * @param <T> JSON data type
     * @return new call definition
     */
    public <T> RpcCall<T, RES> withJsonType(Class<? extends T> clazz) {
        return withJsonType(TypeFactory.defaultInstance().constructType(clazz));
    }

    /**
     * Convert into a new call definition with specified JSON data type
     *
     * @param jsonType new JSON data type
     * @param <T> JSON data type
     * @return new call definition
     */
    @SuppressWarnings("unchecked")
    public <T> RpcCall<T, RES> withJsonType(JavaType jsonType) {
        if (isArray) {
            throw new IllegalStateException("Cannot change json type after enabling array type");
        }
        RpcCall<T, RES> copy = new RpcCall<>(this.method, this.params);
        copy.resultType = this.resultType;
        copy.jsonType = jsonType;
        copy.converter = (Function<T, RES>) this.converter;
        return copy;
    }

    /**
     * Convert into a new call definition with specified Java data type
     *
     * @param clazz new Java data type
     * @param <T> Java data type
     * @return new call definition
     */
    @SuppressWarnings("unchecked")
    public <T> RpcCall<JS, T> withResultType(Class<T> clazz) {
        if (isArray) {
            throw new IllegalStateException("Cannot change result type after enabling array type");
        }
        RpcCall<JS, T> copy = new RpcCall<>(this.method, this.params);
        copy.resultType = clazz;
        copy.jsonType = this.jsonType;
        copy.converter = (Function<JS, T>) this.converter;
        return copy;
    }

    @SuppressWarnings("unchecked")
    public RpcCall<JS[], RES[]> asArray() {
        RpcCall<JS[], RES[]> copy = new RpcCall<>(this.method, this.params);
        copy.jsonType = TypeFactory.defaultInstance().constructArrayType(this.jsonType);
        copy.resultType = (Class<RES[]>) this.resultType.arrayType();
        if (this.converter != null) {
            copy.converter = (values) -> {
                if (values == null) {
                    return null;
                }
                // it's critical to create an instance of the expected type, otherwise the class type will be Object[]
                RES[] result = (RES[]) Array.newInstance(this.resultType, values.length);
                for (int i = 0; i < values.length; i++) {
                    result[i] = this.converter.apply(values[i]);
                }
                return result;
            };
        }
        copy.isArray = true;
        return copy;
    }

    /**
     *
     * @return method for RPC call
     */
    public String getMethod() {
        return method;
    }

    /**
     *
     * @return parameters for RPC call
     */
    public List getParams() {
        return params;
    }

    /**
     *
     * @return Java data type
     */
    @SuppressWarnings("unchecked")
    public Class<RES> getResultType() {
        return (Class<RES>) resultType;
    }

    /**
     *
     * @return JSON data type
     */
    public JavaType getJsonType() {
        return jsonType;
    }

    /**
     *
     * @return function that converts from JSON data to Java data
     */
    public Function<JS, RES> getConverter() {
        return converter;
    }

    /**
     * Create instance of a request
     *
     * @param batchId request id (uniq per batch)
     * @return JSON RPC request
     */
    public RequestJson<Integer> toJson(int batchId) {
        return new RequestJson<>(method, params, batchId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcCall<?, ?> call = (RpcCall<?, ?>) o;
        return method.equals(call.method) &&
                params.equals(call.params) &&
                Objects.equals(isArray, call.isArray) &&
                Objects.equals(jsonType, call.jsonType) &&
                Objects.equals(resultType, call.resultType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, params);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(method)
            .append('(');
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Object param = params.get(i);
                if (param != null) {
                    buf.append(param);
                } else {
                    buf.append("null");
                }
            }
        }
        buf.append(')');
        return buf.toString();
    }
}
