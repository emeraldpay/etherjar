/*
 * Copyright (c) 2023 EmeraldPay Inc, All Rights Reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base implementation for a {@link RpcTransport}
 */
abstract public class AbstractRpcTransport implements RpcTransport<DefaultBatch.FutureBatchItem> {

    private final ExecutorService executorService;
    private final RpcConverter rpcConverter;

    private final ResponseJsonConverter responseJsonConverter = new ResponseJsonConverter();

    public AbstractRpcTransport(ExecutorService executorService, RpcConverter rpcConverter) {
        this.executorService = executorService;
        this.rpcConverter = rpcConverter;
    }

    @Override
    public CompletableFuture<Iterable<RpcCallResponse>> execute(List<DefaultBatch.FutureBatchItem> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(
                Collections.emptyList()
            );
        }
        Map<Integer, DefaultBatch.FutureBatchItem> requests = new HashMap<>(items.size());
        Map<Integer, Class> responseMapping = new HashMap<>(items.size());
        List<RequestJson<Integer>> rpcRequests = items.stream()
            .map(item -> {
                RequestJson<Integer> request = new RequestJson<>(
                    item.getCall().getMethod(),
                    item.getCall().getParams(),
                    item.getId()
                );
                requests.put(item.getId(), item);
                responseMapping.put(item.getId(), item.getCall().getJsonType());
                return request;
            }).collect(Collectors.toList());
        CompletableFuture<Iterable<RpcCallResponse>> f = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                String json = rpcConverter.toJson(rpcRequests);
                InputStream content = execute(json);
                List<ResponseJson<Object, Integer>> response = rpcConverter.parseBatch(content, responseMapping);
                List<RpcCallResponse> result = response.stream()
                    .map(reader(requests))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                f.complete(result);
            } catch (Throwable e) {
                RpcException rpcError;
                if (e instanceof RpcException) {
                    rpcError = (RpcException) e;
                } else if (e instanceof IOException) {
                    rpcError = new RpcException(RpcResponseError.CODE_UPSTREAM_CONNECTION_ERROR, e.getMessage(), null, e);
                } else {
                    rpcError = new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, e.getMessage(), null, e);
                }
                f.completeExceptionally(rpcError);
            }
        });
        return f;
    }

    @SuppressWarnings("unchecked")
    protected <JS, RES> Function<ResponseJson<?, Integer>, RpcCallResponse<JS, RES>> reader(final Map<Integer, DefaultBatch.FutureBatchItem> requests) {
        return (resp) -> {
            RpcCall<JS, RES> call = requests.get(resp.getId()).getCall();
            if (call != null) {
                ResponseJson<JS, Integer> castResp = resp.cast(call.getJsonType());
                return responseJsonConverter.convert(call, castResp);
            }
            return null;
        };
    }

    abstract public InputStream execute(String json) throws IOException;
}
