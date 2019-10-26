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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public abstract class AbstractReactorRpcClient implements ReactorRpcClient {

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

    public class ResponseTransformer implements Function<ResponseJson<?, Integer>, Mono<RpcCallResponse>> {
        private final BatchCallContext<ReactorBatch.ReactorBatchItem> context;

        public ResponseTransformer(BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
            this.context = context;
        }

        @Override
        public Mono<RpcCallResponse> apply(ResponseJson<?, Integer> singleResponse) {
            ReactorBatch.ReactorBatchItem bi = context.getResultMapper().get(singleResponse.getId());
            return extract(bi, singleResponse);
        }

        public <JS, RES> Mono<RpcCallResponse> extract(ReactorBatch.ReactorBatchItem<JS, RES> bi, ResponseJson<JS, Integer> response)  {
            RpcResponseError error = response.getError();
            RpcCall<JS, RES> call = bi.getCall();
            if (error != null) {
                RpcException err = new RpcException(call, error.getCode(), error.getMessage(), error.getData(), null);
                bi.onError(err);
                return Mono.error(err);
            } else {
                JS js = response.getResult();
                RES value = call.getConverter().apply(js);
                bi.onResult(value);
                return Mono.just(new RpcCallResponse<>(call, value));
            }
        }
    }
}
