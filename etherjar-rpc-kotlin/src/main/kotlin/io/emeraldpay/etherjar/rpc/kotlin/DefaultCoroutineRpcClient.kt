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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DefaultCoroutineRpcClient(
    private val transport: CoroutineRpcTransport
) : CoroutineRpcClient {

    override suspend fun <JS, RES> execute(call: RpcCall<JS, RES>): RES {
        val batch = createBatch()
        val item = batch.add(call)

        val responses = transport.execute(listOf(item))
        val response = responses.first()

        return if (response.isError) {
            throw response.error
        } else {
            @Suppress("UNCHECKED_CAST")
            response.value as RES
        }
    }

    override suspend fun execute(batch: CoroutineBatch): List<RpcCallResponse<*, *>> {
        return batch.execute()
    }

    override fun <JS, RES> executeFlow(call: RpcCall<JS, RES>): Flow<RES> = flow {
        emit(execute(call))
    }

    override fun executeFlow(batch: CoroutineBatch): Flow<RpcCallResponse<*, *>> = flow {
        val responses = execute(batch)
        responses.forEach { response ->
            emit(response)
        }
    }

    override fun createBatch(): CoroutineBatch {
        return CoroutineBatch(transport)
    }

    override fun close() {
        transport.close()
    }
}
