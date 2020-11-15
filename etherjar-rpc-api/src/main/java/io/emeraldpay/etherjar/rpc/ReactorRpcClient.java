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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorRpcClient extends RpcClient<Flux<RpcCallResponse>, ReactorBatch.ReactorBatchItem, ReactorBatch> {

    Flux<RpcCallResponse> execute(ReactorBatch batch);

    <JS, RES> Mono<RES> execute(RpcCall<JS, RES> call);

}
