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

import io.emeraldpay.etherjar.rpc.BatchCallContext;
import io.emeraldpay.etherjar.rpc.ReactorBatch;
import io.emeraldpay.etherjar.rpc.RpcConverter;
import io.emeraldpay.etherjar.rpc.RequestJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.function.Function;

/**
 * Converts batch to JSON RPC request string
 */
public class BatchToString {

    private RpcConverter rpcConverter;

    public BatchToString(RpcConverter rpcConverter) {
        this.rpcConverter = rpcConverter;
    }

    private Function<Tuple2<String, Boolean>, Publisher<String>> arrange = (x) -> {
        boolean first = x.getT2();
        if (first) {
            return Mono.just(x.getT1());
        } else {
            return Flux.just(",", x.getT1());
        }
    };

    private Function<ReactorBatch.ReactorBatchItem, RequestJson> toRequest =
        (bi) -> new RequestJson<>(bi.getCall().getMethod(), bi.getCall().getParams(), bi.getId());


    /**
     * Converts batch to JSON RPC request string
     *
     * @param batch request batch
     * @return string serialized batch with individual mappings as context
     */
    public Flux<ByteBuf> convertToJson(Flux<ReactorBatch.ReactorBatchItem> batch) {
        Flux<String> items = batch
            .map(toRequest)
            .map(rpcConverter::toJson)
            .zipWith(Flux.range(0, Integer.MAX_VALUE).map(i -> i == 0))
            .flatMap(arrange);
        return Flux.concat(Flux.just("["), items, Flux.just("]"))
            .map(String::getBytes)
            .map(Unpooled::wrappedBuffer);
    }

    /**
     * Container for mapping between serialized JSON array and source request items
     */
    public static class BatchWithContext {
        private final Flux<ByteBuf> batch;
        private final BatchCallContext<ReactorBatch.ReactorBatchItem> context;

        public BatchWithContext(Flux<ByteBuf> batch, BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
            this.batch = batch;
            this.context = context;
        }

        /**
         *
         * @return batch serialized to JSON array
         */
        public Flux<ByteBuf> getBatch() {
            return batch;
        }

        /**
         * @return mapping to original requests
         */
        public BatchCallContext<ReactorBatch.ReactorBatchItem> getContext() {
            return context;
        }
    }
}
