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

import org.reactivestreams.Publisher;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class AbstractReactorRpcClient implements ReactorRpcClient {

    private FailedBatchProcessor failedBatchProcessor = new FailedBatchProcessor();

    public FailedBatchProcessor getFailedBatchProcessor() {
        return failedBatchProcessor;
    }

    public void setFailedBatchProcessor(FailedBatchProcessor failedBatchProcessor) {
        this.failedBatchProcessor = failedBatchProcessor;
    }

    public Flux<RpcCallResponse> execute(Flux<RpcCall<?, ?>> calls) {
        return ReactorBatch.from(calls).flatMapMany(this::execute);
    }

    @Override
    public <JS, RES> Mono<RES> execute(RpcCall<JS, RES> call) {
        ReactorBatch batch = new ReactorBatch();
        ReactorBatch.ReactorBatchItem<JS, RES> item = batch.add(call);
        return execute(batch)
            .onErrorResume((t) -> Mono.empty())
            .then(item.getResult());
    }

    public Flux<RpcCallResponse> postProcess(ReactorBatch batch, BatchCallContext context, Flux<RpcCallResponse> result) {
        // Fill batch items with result
        Flux<RpcCallResponse> shared = result
            .doOnNext(new ProcessBatchResult(context))
            // each batch item would attach to the response flux to build its own result
            .share()
            // cache the results to avoid double calls when both execute() and individual call has own subscriptions
            .cache();

        // Connect batch items to execution
        batch.withExecution(shared);

        // Close unprocessed items
        return shared
            .doFinally((s) -> {
                if (s != SignalType.CANCEL) batch.close();
            });
    }

    /**
     * Strategy to restore from upstream RpcException
     */
    public static class FailedBatchProcessor {

        public Function<RpcException, Publisher<RpcCallResponse>> createFallback(ReactorBatch batch) {
            return err -> batch.getItems()
                .doOnNext((bi) -> bi.onError(err))
                .then(Mono.error(err));
        }

    }
}
