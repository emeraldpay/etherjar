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

package io.infinitape.etherjar.rpc.http;

import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.transport.RpcTransport;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultRpcClient extends AbstractFuturesRcpClient implements FuturesRcpClient {

    private RpcTransport rpcTransport;

    public DefaultRpcClient(RpcTransport rpcTransport) {
        this.rpcTransport = rpcTransport;
    }

    public ExecutableBatch newBatch() {
        return new ExecutableBatch(this);
    }

    @Override
    public CompletableFuture<List<DefaultBatch.FutureBatchItem>> execute(DefaultBatch batch) {
        List<DefaultBatch.FutureBatchItem> items = batch.getItems();
        if (items.isEmpty()) {
            batch.close();
            return CompletableFuture.completedFuture(
                Collections.emptyList()
            );
        }
        BatchCallContext<DefaultBatch.FutureBatchItem> context = new BatchCallContext<>();
        List<RpcTransport.RpcRequest> rpcRequests = items.stream()
            .map(new BatchTransformer<Object, Object>(context))
            .collect(Collectors.toList());

        return rpcTransport.execute(rpcRequests).thenApply((resp) -> {
            List<DefaultBatch.FutureBatchItem> result = batch.getItems();
            resp.forEach(new ResponseReader<Object>(context));
            batch.close();
            return result;
        });
    }

    public static class BatchTransformer<JS, T> implements Function<DefaultBatch.FutureBatchItem, RpcTransport.RpcRequest<JS>> {
        private final BatchCallContext<DefaultBatch.FutureBatchItem> context;

        BatchTransformer(BatchCallContext<DefaultBatch.FutureBatchItem> context) {
            this.context = context;
        }

        @Override
        public RpcTransport.RpcRequest<JS> apply(DefaultBatch.FutureBatchItem item) {
            int id = context.add(item);
            RpcCall<JS, T> call = item.getCall();
            Class<JS> jsonType = call.getJsonType();
            return new RpcTransport.RpcRequest<>(jsonType, call.getMethod(), call.toJson(id));
        }
    }

    public static class ResponseReader<JS> implements Consumer<RpcTransport.RpcResponse> {
        private final BatchCallContext<DefaultBatch.FutureBatchItem> context;

        ResponseReader(BatchCallContext<DefaultBatch.FutureBatchItem> context) {
            this.context = context;
        }

        @Override
        public void accept(RpcTransport.RpcResponse value) {
            DefaultBatch.FutureBatchItem<JS, ?> batchItem = context.getResultMapper().get(value.getRequest().getPayload().getId());
            batchItem.read(value);
        }

    }
}
