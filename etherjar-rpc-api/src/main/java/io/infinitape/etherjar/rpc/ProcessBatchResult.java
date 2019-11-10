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

import java.util.function.Consumer;

/**
 * Processes all individual items in the batch, providing them with a result or error received from RpcCallResponse
 *
 */
public class ProcessBatchResult implements Consumer<RpcCallResponse> {
    private final BatchCallContext<?> context;

    public ProcessBatchResult(BatchCallContext<?> context) {
        this.context = context;
    }

    public <JS, RES> void process(BatchItem<?, JS, RES> bi, RpcCallResponse<JS, RES> response) {
        if (response.isError()) {
            bi.onError(response.getError());
        } else {
            RES value = response.getValue();
            bi.onResult(value);
        }
    }

    @Override
    public void accept(RpcCallResponse response) {
        BatchItem item = context.getBatchItem(response.getSource());
        process(item, response);
    }

}
