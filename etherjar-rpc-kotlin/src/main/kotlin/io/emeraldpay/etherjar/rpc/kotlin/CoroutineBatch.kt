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
import io.emeraldpay.etherjar.rpc.RpcCallResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicInteger

class CoroutineBatch(
    private val transport: CoroutineRpcTransport
) {
    private val items = mutableListOf<CoroutineBatchItem<*, *>>()
    private val idGenerator = AtomicInteger(1)

    fun <JS, RES> add(call: RpcCall<JS, RES>): CoroutineBatchItem<JS, RES> {
        val item = CoroutineBatchItem(call, idGenerator.getAndIncrement())
        items.add(item)
        return item
    }

    suspend fun execute(): List<RpcCallResponse<*, *>> {
        val responses = transport.execute(items)
        processResponses(responses)
        return responses
    }

    suspend fun executeAndGetResults(): List<Any?> = coroutineScope {
        execute()
        items.map { item ->
            async { item.getResult() }
        }.awaitAll()
    }

    private fun processResponses(responses: List<RpcCallResponse<*, *>>) {
        val responseMap = responses.associateBy { response ->
            // We'll need to match by call content since RpcCallResponse doesn't have id
            // For now, match by index
            responses.indexOf(response)
        }

        items.forEachIndexed { index, item ->
            val response = responseMap[index]
            if (response != null) {
                if (response.isError) {
                    item.onError(response.error!!)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    (item as CoroutineBatchItem<Any, Any>).onResult(response.value!!)
                }
            }
        }
    }

    fun cancel() {
        items.forEach { it.cancel() }
    }

    val size: Int
        get() = items.size

    val isEmpty: Boolean
        get() = items.isEmpty()
}
