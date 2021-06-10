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
package io.emeraldpay.etherjar.rpc;

import reactor.core.publisher.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ReactorBatch implements Batch<ReactorBatch.ReactorBatchItem>, Consumer<RpcCall>, AutoCloseable {

    private final List<ReactorBatch.ReactorBatchItem<?, ?>> items = new ArrayList<>();
    private final AtomicInteger ids = new AtomicInteger(1);

    public static Mono<ReactorBatch> from(Flux<RpcCall<?, ?>> calls) {
        ReactorBatch batch = new ReactorBatch();
        return calls
            .map(batch::add)
            .then(Mono.just(batch));
    }

    public <JS, RES> ReactorBatch.ReactorBatchItem<JS, RES> add(RpcCall<JS, RES> call) {
        ReactorBatch.ReactorBatchItem<JS, RES> b = new ReactorBatch.ReactorBatchItem<>(ids.getAndIncrement(), call);
        items.add(b);
        return b;
    }

    public Flux<ReactorBatchItem> getItems() {
        return Flux.fromIterable(items);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void accept(RpcCall rpcCall) {
        add(rpcCall);
    }

    /**
     * Tracks execution result of the current batch and makes sure individual calls follow
     * the main execution flow.
     *
     * @param execution execution of the current batch
     */
    public void withExecution(Flux<RpcCallResponse> execution) {
        items.forEach(item -> item.connectTo(execution));
    }

    @Override
    public void close() {
        items.forEach(ReactorBatchItem::close);
    }

    public static class ReactorBatchItem<JS, RES> extends BatchItem<Mono<RES>, JS, RES> {

        private Flux<RpcCallResponse> batch;

        private RES value;
        private Throwable exception;
        private boolean completed = false;

        ReactorBatchItem(int id, RpcCall<JS, RES> call) {
            super(id, call);
        }

        public void connectTo(Flux<RpcCallResponse> batch) {
            this.batch = batch;
        }

        @Override
        public void onResult(RES value) {
            if (isCompleted()) {
                return;
            }
            completed = true;
            this.value = value;
        }

        @Override
        public void onError(RpcException err) {
            if (isCompleted()) {
                return;
            }
            exception = err;
            completed = true;
        }

        private boolean isCompleted() {
            return completed;
        }

        private Mono<RES> getActualResult() {
            // resolve actual result as part of reactive flow.
            // during the original call to this method the state is just not established and must be
            // resolved only after the previous steps (i.e. the execution flow) is finished.
            return Mono.just(this).flatMap((self) -> {
                if (!self.completed) {
                    return Mono.error(new BatchNotExecutedException(this.id, this.call));
                }
                if (self.value != null) {
                    return Mono.just(self.value);
                } else if (self.exception != null) {
                    return Mono.error(self.exception);
                } else {
                    return Mono.empty();
                }
            });
        }

        public Mono<RES> getResult() {
            // if already completed with data just return it
            if (completed) {
                return getActualResult();
            }
            // otherwise if connected to a batch, wait for it and then process result
            if (batch != null) {
                return batch.then(getActualResult());
            }
            // must likely will fail since is not completed
            return getActualResult();
        }

        @Override
        public void close() {
            if (!isCompleted()) {
                if (exception == null) {
                    exception = new BatchNotExecutedException(this.id, this.call);
                }
            }
            completed = true;
        }
    }

}
