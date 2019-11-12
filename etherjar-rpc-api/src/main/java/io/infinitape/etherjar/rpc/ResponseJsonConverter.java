/*
 * Copyright (c) 2016-2019 Igor Artamonov
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

import io.infinitape.etherjar.rpc.json.ResponseJson;

import java.util.function.Function;

/**
 * Reads JSON RPC raw response data, together with source Call data, and convert into RpcCallResponse
 * suitable for further processing
 */
public class ResponseJsonConverter {

    public <JS, RES> RpcCallResponse<JS, RES> convert(RpcCall<JS, RES> call, ResponseJson<JS, Integer> response) {
        if (response.getError() != null) {
            return new RpcCallResponse<>(call, response.getError().asException());
        } else {
            RES value = call.getConverter().apply(response.getResult());
            return new RpcCallResponse<>(call, value);
        }
    }

    public <JS, RES> Function<ResponseJson<JS, Integer>, RpcCallResponse<JS, RES>> forCall(RpcCall<JS, RES> call) {
        return (response) -> convert(call, response);
    }

    @SuppressWarnings("unchecked")
    public <JS, RES> Function<ResponseJson<JS, Integer>, RpcCallResponse<JS, RES>> forContext(BatchCallContext context) {
        return (response) -> {
            RpcCall<JS, RES> call = context.getCall(response.getId());
            if (call == null) {
                throw new RuntimeException("No call was made with id " + response.getId());
            }
            return convert(call, response);
        };
    }

}
