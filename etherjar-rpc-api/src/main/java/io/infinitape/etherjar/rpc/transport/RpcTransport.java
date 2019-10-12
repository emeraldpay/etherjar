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

package io.infinitape.etherjar.rpc.transport;

import io.infinitape.etherjar.rpc.RpcCall;
import io.infinitape.etherjar.rpc.RpcException;
import io.infinitape.etherjar.rpc.json.RequestJson;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RpcTransport extends Closeable {

    CompletableFuture<Iterable<RpcResponse>> execute(List<RpcRequest> items);

    class RpcRequest<T> {
        private String method;
        private RequestJson<Integer> payload;
        private Class<T> type;

        public RpcRequest(Class<T> type, String method, RequestJson<Integer> payload) {
            this.type = type;
            this.method = method;
            this.payload = payload;
        }

        public RpcRequest(RpcCall<T, ?> call, int id) {
            this(call.getJsonType(), call.getMethod(), call.toJson(id));
        }

        public String getMethod() {
            return method;
        }

        public RequestJson<Integer> getPayload() {
            return payload;
        }

        public Class<T> getType() {
            return type;
        }

        public RpcResponse<T> asResponse(T payload) {
            return new RpcResponse<T>(this, payload);
        }

        public RpcResponse<T> asError(RpcException error) {
            return new RpcResponse<T>(this, error);
        }

        public <X> RpcRequest<X> cast(Class<X> type) {
            if (this.type.isAssignableFrom(type)) {
                return (RpcRequest<X>) this;
            }
            throw new IllegalArgumentException("Invalid type requested: " + type + "; while original is: " + this.type);
        }
    }

    class RpcResponse<T> {
        private RpcRequest<T> request;
        private T payload;
        private RpcException error;

        public RpcResponse(RpcRequest<T> request, T payload) {
            this.request = request;
            this.payload = payload;
        }

        public RpcResponse(RpcRequest<T> request, RpcException error) {
            this.request = request;
            this.error = error;
        }

        public T getPayload() {
            return payload;
        }

        public RpcRequest getRequest() {
            return request;
        }

        public RpcException getError() {
            return error;
        }
    }
}
