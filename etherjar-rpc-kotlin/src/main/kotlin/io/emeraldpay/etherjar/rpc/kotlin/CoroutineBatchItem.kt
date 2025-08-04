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
import io.emeraldpay.etherjar.rpc.RpcException
import kotlinx.coroutines.CompletableDeferred

class CoroutineBatchItem<JS, RES>(
    val call: RpcCall<JS, RES>,
    val id: Int
) {
    private val deferred = CompletableDeferred<RES>()

    fun onResult(value: RES) {
        deferred.complete(value)
    }

    fun onError(exception: RpcException) {
        deferred.completeExceptionally(exception)
    }

    suspend fun getResult(): RES = deferred.await()

    fun cancel() {
        deferred.cancel()
    }

    val isCompleted: Boolean
        get() = deferred.isCompleted
}
