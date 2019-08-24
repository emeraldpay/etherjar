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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * List of RPC commands to execute
 */
public class Batch {

    private List<BatchItem<?, ?>> items = new ArrayList<>();

    /**
     * Add a new command into the batch
     *
     * @param call command
     * @param <T> type of returning result
     * @return async result
     */
    public <T> CompletableFuture<T> add(RpcCall<?, T> call) {
        BatchItem<?, T> b = new BatchItem<>(items.size(), call);
        items.add(b);
        return b.result;
    }

    /**
     *
     * @return all commands in current batch
     */
    public List<BatchItem<?, ?>> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(items, batch.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    /**
     * Context for a command in batch
     *
     * @param <JS> json data type when returned from RPC server
     * @param <T> java data type, converted from json
     */
    public class BatchItem<JS, T> {
        int pos;
        RpcCall<JS, T> call;
        CompletableFuture<T> result;

        protected BatchItem(int pos, RpcCall<JS, T> call) {
            this.pos = pos;
            this.call = call;
            this.result = new CompletableFuture<>();
        }

        /**
         * Called after completion on RPC server
         *
         * @param value value returned from server
         */
        public void onComplete(JS value) {
            T val = call.getConverter().apply(value);
            result.complete(val);
        }

        /**
         * Called if call failed to complete
         *
         * @param err error details
         * @see RpcException
         */
        public void onError(RpcException err) {
            result.completeExceptionally(err);
        }

        public RpcCall<JS, T> getCall() {
            return call;
        }

        public int getPos() {
            return pos;
        }

        public CompletableFuture<T> getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BatchItem<?, ?> batchItem = (BatchItem<?, ?>) o;
            return pos == batchItem.pos &&
                    Objects.equals(call, batchItem.call);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, call);
        }
    }
}
