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
package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.rpc.json.ResponseJson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * List of RPC commands to execute
 */
public class DefaultBatch implements Batch<DefaultBatch.FutureBatchItem>, AutoCloseable {

    private List<FutureBatchItem<?, ?>> items = new ArrayList<>();
    private AtomicInteger ids = new AtomicInteger(0);

    /**
     * Add a new command into the batch
     *
     * @param call command
     * @return async result
     */
    public <JS, RES> FutureBatchItem<JS, RES> add(RpcCall<JS, RES> call) {
        FutureBatchItem<JS, RES> b = new FutureBatchItem<>(ids.getAndIncrement(), call);
        items.add(b);
        return b;
    }

    /**
     *
     * @return all commands in current batch
     */
    public List<FutureBatchItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBatch batch = (DefaultBatch) o;
        return Objects.equals(items, batch.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public void close() {
        items.forEach(FutureBatchItem::close);
    }

    /**
     * Context for a command in batch
     *
     * @param <JS> json data type when returned from RPC server
     * @param <T> java data type, converted from json
     */
    public class FutureBatchItem<JS, T> extends BatchItem<CompletableFuture<T>, JS, T> {
        CompletableFuture<T> result;

        protected FutureBatchItem(int pos, RpcCall<JS, T> call) {
            super(pos, call);
            this.result = new CompletableFuture<>();
        }

        /**
         * Called after completion on RPC server
         *
         * @param value value returned from server
         */
        @Override
        public void onResult(T value) {
            result.complete(value);
        }

        /**
         * Called if call failed to complete
         *
         * @param err error details
         * @see RpcException
         */
        @Override
        public void onError(RpcException err) {
            result.completeExceptionally(err);
        }

        @Override
        public CompletableFuture<T> getResult() {
            return result;
        }

        @Override
        public void close() {
            if (!result.isDone()) {
                result.completeExceptionally(new BatchNotExecutedException(this.id, this.call));
            }
        }
    }
}
