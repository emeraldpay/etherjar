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

package io.emeraldpay.etherjar.rpc.http;

import io.emeraldpay.etherjar.rpc.*;
import io.emeraldpay.etherjar.rpc.RpcTransport;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultRpcClient extends AbstractFuturesRpcClient implements FuturesRpcClient {

    private RpcTransport<DefaultBatch.FutureBatchItem> rpcTransport;

    public DefaultRpcClient(RpcTransport<DefaultBatch.FutureBatchItem> rpcTransport) {
        this.rpcTransport = rpcTransport;
    }

    public ExecutableBatch newBatch() {
        return new ExecutableBatch(this);
    }

    @Override
    public List<CompletableFuture> execute(DefaultBatch batch) {
        List<DefaultBatch.FutureBatchItem> items = batch.getItems();
        if (items.isEmpty()) {
            batch.close();
            return Collections.emptyList();
        }

        BatchCallContext<DefaultBatch.FutureBatchItem> context = new BatchCallContext<>();
        Consumer<RpcCallResponse> processBatch = new ProcessBatchResult(context);
        List<CompletableFuture> result = batch.getItems()
            .stream()
            .peek(context::add)
            .map((Function<DefaultBatch.FutureBatchItem, CompletableFuture>) DefaultBatch.FutureBatchItem::getResult)
            .collect(Collectors.toList());

        rpcTransport.execute(items)
            .thenAccept((Iterable<RpcCallResponse> responses) -> responses.forEach(processBatch))
            .thenAccept((_it) -> batch.close());

        return result;
    }

}
