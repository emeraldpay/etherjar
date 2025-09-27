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
package io.emeraldpay.etherjar.rpc;

import io.emeraldpay.etherjar.domain.TransactionId;
import io.emeraldpay.etherjar.rpc.json.TraceItemJson;
import org.jspecify.annotations.NullMarked;

/**
 * Commands specific for Parity Ethereum
 */
@NullMarked
public class ParityCommands {

    /**
     * Call for trace_transaction
     *
     * @param hash hash of the transaction
     * @return trace list
     */
    public RpcCall<TraceItemJson[], TraceItemJson[]> traceTransaction(TransactionId hash) {
        return RpcCall.create("trace_transaction", TraceItemJson.class, hash.toHex()).asArray();
    }
}
