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

import io.infinitape.etherjar.rpc.json.ResponseJson;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
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
        result = result.doOnNext(new ProcessBatchResult(context));

        // Connect batch items to execution
        batch.withExecution(Flux.from(result));

        // Close unprocessed items
        result = result.doFinally((s) -> batch.close());

        return result;
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
