/*
 * Copyright (c) 2025 EmeraldPay Ltd, All Rights Reserved.
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

package io.emeraldpay.etherjar.rpc.kotlin

import io.emeraldpay.etherjar.rpc.RpcCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

suspend inline fun <JS, RES> CoroutineRpcClient.execute(
    callBuilder: () -> RpcCall<JS, RES>
): RES = execute(callBuilder())

suspend inline fun CoroutineRpcClient.batch(
    builder: CoroutineBatch.() -> Unit
): List<Any?> {
    val batch = createBatch()
    batch.builder()
    return batch.executeAndGetResults()
}

fun <JS, RES> CoroutineRpcClient.executeAsFlow(call: RpcCall<JS, RES>): Flow<RES> = flow {
    emit(execute(call))
}
