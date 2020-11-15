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
import io.emeraldpay.etherjar.rpc.RpcCallResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

/**
 * Interface for an actual executor of call on an upstream
 */
public interface ReactorRpcTransport {

    /**
     * Execute batch
     *
     * @param batch source of calls
     * @param context context of the batch
     * @return responses from upstream
     */
    Publisher<RpcCallResponse> execute(Flux<ReactorBatch.ReactorBatchItem> batch,
                                  BatchCallContext<ReactorBatch.ReactorBatchItem> context);

}
