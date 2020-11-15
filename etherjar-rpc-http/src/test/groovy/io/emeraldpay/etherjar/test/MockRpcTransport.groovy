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
package io.emeraldpay.etherjar.test

import io.emeraldpay.etherjar.rpc.DefaultBatch
import io.emeraldpay.etherjar.rpc.RpcCallResponse
import io.emeraldpay.etherjar.rpc.RpcException
import io.emeraldpay.etherjar.rpc.RpcResponseError
import io.emeraldpay.etherjar.rpc.RpcTransport

import java.util.concurrent.CompletableFuture

class MockRpcTransport implements RpcTransport<DefaultBatch.FutureBatchItem> {

    List<MockedRequest> commands = []

    @Override
    CompletableFuture<Iterable<RpcCallResponse>> execute(List<DefaultBatch.FutureBatchItem> items) {
        List<RpcCallResponse> result = []
        items.forEach { item ->
            def found = commands.find {
                it.accept(item.call.method, item.call.params)
            }
            if (found != null) {
                result << new RpcCallResponse<>(item.call, found.response)
            } else {
                result << new RpcCallResponse<>(item.call, new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, "No command for $item.call.method($item.call.params)"))
            }
        }
        return CompletableFuture.completedFuture(result)
    }

    @Override
    void close() throws IOException {

    }

    void mock(String method, List params, Class type, Object response) {
        commands.add(new MockedRequest(method: method, params: params, type: type, response: response))
    }

    void mock(String method, List params, Object response) {
        commands.add(new MockedRequest(method: method, params: params, response: response))
    }


    class MockedRequest {
        String method
        List params
        Class type

        Object response

        boolean accept(String method, List params) {
            return this.method == method && this.params == params
        }
    }
}
