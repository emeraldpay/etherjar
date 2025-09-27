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
package io.emeraldpay.etherjar.rpc.http;

import io.emeraldpay.etherjar.rpc.*;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * A transport which always executes calls as a single JSON RPC batch, e.x. by translating into
 * JSON Array and sending in a single request
 */
@NullMarked
public class BatchTransport implements ReactorRpcTransport {

    private final HttpClient httpClient;
    private final Mono<String> target;
    private final RpcConverter rpcConverter;
    private final BatchToString batchToString;

    public BatchTransport(HttpClient httpClient, Mono<String> target, RpcConverter rpcConverter, BatchToString batchToString) {
        this.httpClient = httpClient;
        this.target = target;
        this.rpcConverter = rpcConverter;
        this.batchToString = batchToString;
    }

    @Override
    public Publisher<RpcCallResponse> execute(Flux<ReactorBatch.ReactorBatchItem> batch, BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
        Flux<ByteBuf> converted = batchToString.convertToJson(batch);
        HttpClient.ResponseReceiver<?> response =
            httpClient
                .post()
                .uri(target)
                .send(converted);
        Flux<RpcCallResponse> result = response.response((resp, data) -> {
            if (resp.status() == HttpResponseStatus.OK) {
                ReactorHttpRpcClient.ResponseReader reader = new ReactorHttpRpcClient.ResponseReader(rpcConverter, context);
                return data.aggregate().flatMapMany(reader);
            } else {
                RpcException err = new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Upstream connection error. Status: " + resp.status().code());
                return Flux.error(err);
            }
        });

        return result;
    }
}
