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
package io.infinitape.etherjar.rpc.http;

import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;

/**
 * Always executes a batch, even consisting of multiple items, as a series of separate JSON RPC calls
 */
public class SeparatedTransport implements ReactorRpcTransport {

    private HttpClient httpClient;
    private Mono<String> target;
    private RpcConverter rpcConverter;

    public SeparatedTransport(HttpClient httpClient, Mono<String> target, RpcConverter rpcConverter) {
        this.httpClient = httpClient;
        this.target = target;
        this.rpcConverter = rpcConverter;
    }

    @Override
    public Publisher<RpcCallResponse> execute(Flux<ReactorBatch.ReactorBatchItem> batch, BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
        return batch
            .map((bi) -> Tuples.of(bi, new RequestJson<>(bi.getCall().getMethod(), bi.getCall().getParams(), bi.getId())))
            .map((req) -> req.mapT2(request ->
                Unpooled.wrappedBuffer(rpcConverter.toJson(request).getBytes()))
            )
            .flatMap(this::sendRequest)
            .map((resp) -> processResponse(resp.getT2(), resp.getT1()))
            .onErrorResume(ConnectException.class, ReactorHandlers.catchConnectException());
    }

    /**
     * Convert response from HTTP bytes into RpcCallResponse
     *
     * @param json response data
     * @param source source of the call
     * @param <JS> Javascript Type for the data
     * @param <RES> Resulting Type for the data
     * @return converted response, which may contain RpcException as error field, in case of failure
     */
    protected <JS, RES> RpcCallResponse<JS, RES> processResponse(byte[] json, ReactorBatch.ReactorBatchItem<JS, RES> source) {
        try {
            JS value = rpcConverter.fromJson(new ByteArrayInputStream(json), source.getCall().getJsonType());
            RES result = source.getCall().getConverter().apply(value);
            return new RpcCallResponse<JS, RES>(source.getCall(), result);
        } catch (RpcException e) {
            return new RpcCallResponse<JS, RES>(source.getCall(), e);
        }
    }

    /**
     * Send request to HTTP
     *
     * @param req request data, consisting of source call and JSON as a binary representation
     *
     * @return Mono for response tuple, consisting of source call and a binary representation of the response JSON.
     *    It may be an Error Mono with RpcException if it failed to read response.
     */
    protected Publisher<Tuple2<ReactorBatch.ReactorBatchItem, byte[]>> sendRequest(Tuple2<ReactorBatch.ReactorBatchItem, ByteBuf> req) {
        HttpClient.ResponseReceiver<?> response = httpClient
            .post()
            .uri(target)
            .send(Mono.just(req.getT2()));

        return response
            .response(this::read)
            .map((x) -> Tuples.of(req.getT1(), x))
            .last();
    }

    /**
     * Read response
     * @param resp HTTP Response headers
     * @param data HTTP Response data
     * @return publisher of read bytes
     * @throws RpcException if status code is not 200 of an error happened on reading data
     */
    protected Publisher<byte[]> read(HttpClientResponse resp, ByteBufFlux data) {
        if (resp.status() == HttpResponseStatus.OK) {
            return data.aggregate()
                .asByteArray()
                .onErrorResume((t) ->
                    Mono.error(new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Upstream connection error. Failed to read response"))
                );
        } else {
            throw new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Upstream connection error. Status: " + resp.status().code());
        }
    }
}
