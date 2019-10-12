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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ReactorBatch implements Batch<ReactorBatch.ReactorBatchItem>, Consumer<RpcCall> {

    private List<ReactorBatch.ReactorBatchItem<?, ?>> items = new ArrayList<>();
    private AtomicInteger ids = new AtomicInteger(1);

    public <JS, RES> ReactorBatch.ReactorBatchItem<JS, RES> add(RpcCall<JS, RES> call) {
        ReactorBatch.ReactorBatchItem<JS, RES> b = new ReactorBatch.ReactorBatchItem<>(ids.getAndIncrement(), call);
        items.add(b);
        return b;
    }

    public Flux<ReactorBatchItem<?, ?>> getItems() {
        return Flux.fromIterable(items);
    }

    @Override
    public void accept(RpcCall rpcCall) {
        add(rpcCall);
    }

    public static class ReactorBatchItem<JS, RES> extends BatchItem<Mono<RES>, JS, RES> {
        private MonoProcessor<RES> proc = MonoProcessor.create();

        public ReactorBatchItem(int id, RpcCall<JS, RES> call) {
            super(id, call);
        }

        @Override
        public void onResult(RES value) {
            proc.onNext(value);
        }

        @Override
        public void onError(RpcException err) {
            proc.onError(err);
        }

        public Mono<RES> getResult() {
            return Mono.from(proc);
        }
    }
}
