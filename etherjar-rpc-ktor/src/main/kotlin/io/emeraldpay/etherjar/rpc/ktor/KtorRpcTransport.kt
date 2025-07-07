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

package io.emeraldpay.etherjar.rpc.ktor

import com.fasterxml.jackson.databind.JavaType
import io.emeraldpay.etherjar.rpc.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class KtorRpcTransport private constructor(
    private val httpClient: HttpClient,
    private val rpcConverter: RpcConverter,
    private val responseJsonConverter: ResponseJsonConverter,
    private val target: URI
) : CoroutineRpcTransport {

    companion object {
        fun newBuilder(): Builder = Builder()
    }

    override suspend fun execute(items: List<CoroutineBatchItem<*, *>>): List<RpcCallResponse<*, *>> {
        return withContext(Dispatchers.IO) {
            try {
                // Phase 1: Create request mappings (following AbstractRpcTransport pattern)
                val requestsMap = mutableMapOf<Int, CoroutineBatchItem<*, *>>()
                val responseMapping = mutableMapOf<Int, JavaType>()

                val rpcRequests = items.map { item ->
                    val request = RequestJson<Int>(item.call.method, item.call.params, item.id)
                    requestsMap[item.id] = item
                    responseMapping[item.id] = item.call.jsonType
                    request
                }

                // Phase 2: Serialize and send HTTP request
                val jsonRequest = if (rpcRequests.size == 1) {
                    rpcConverter.toJson(rpcRequests[0])
                } else {
                    rpcConverter.toJson(rpcRequests)
                }

                val response = httpClient.post(target.toString()) {
                    contentType(ContentType.Application.Json)
                    setBody(jsonRequest)
                }

                // Check HTTP status
                if (response.status != HttpStatusCode.OK) {
                    return@withContext items.map { item ->
                        createErrorResponse(
                            item,
                            RpcException(
                                RpcResponseError.CODE_INTERNAL_ERROR,
                                "HTTP error: ${response.status.value} ${response.status.description}"
                            )
                        )
                    }
                }

                // Phase 3: Parse response with proper type mapping
                val responseBody = response.bodyAsText()
                val inputStream = java.io.ByteArrayInputStream(responseBody.toByteArray())

                val responses = if (rpcRequests.size == 1) {
                    // For single request, wrap response in array format to use parseBatch
                    val wrappedResponse = "[$responseBody]"
                    val wrappedInputStream = java.io.ByteArrayInputStream(wrappedResponse.toByteArray())
                    rpcConverter.parseBatch(wrappedInputStream, responseMapping)
                } else {
                    rpcConverter.parseBatch(inputStream, responseMapping)
                }

                // Phase 4: Convert to RpcCallResponse objects
                responses.mapNotNull { responseJson ->
                    val item = requestsMap[responseJson.id]
                    if (item != null) {
                        @Suppress("UNCHECKED_CAST")
                        val call = item.call as RpcCall<Any, Any>
                        val castResponse = responseJson.cast(call.jsonType.rawClass as Class<Any>)
                        responseJsonConverter.convert(call, castResponse)
                    } else {
                        null
                    }
                }

            } catch (e: Exception) {
                items.map { item ->
                    createErrorResponse(
                        item,
                        RpcException(
                            RpcResponseError.CODE_INTERNAL_ERROR,
                            "Request failed: ${e.message}"
                        )
                    )
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun createErrorResponse(item: CoroutineBatchItem<*, *>, exception: RpcException): RpcCallResponse<*, *> {
        val call = item.call as io.emeraldpay.etherjar.rpc.RpcCall<Any, Any>
        val error: RpcException = exception
        // Create response using Java reflection to avoid constructor ambiguity
        try {
            val constructor = RpcCallResponse::class.java.getConstructor(
                io.emeraldpay.etherjar.rpc.RpcCall::class.java,
                RpcException::class.java
            )
            return constructor.newInstance(call, error)
        } catch (e: Exception) {
            // Fallback - this should work since we know it's an error case
            throw RuntimeException("Failed to create error response", e)
        }
    }

    override fun close() {
        httpClient.close()
    }

    class Builder {
        private var target: URI = URI.create("http://localhost:8545")
        private var rpcConverter: RpcConverter? = null
        private var httpClient: HttpClient? = null
        private var httpClientBuilder: HttpClientConfig<CIOEngineConfig>.() -> Unit = {}

        fun connectTo(uri: URI): Builder {
            this.target = uri
            return this
        }

        fun connectTo(url: String): Builder {
            this.target = URI.create(url)
            return this
        }

        fun rpcConverter(converter: RpcConverter): Builder {
            this.rpcConverter = converter
            return this
        }

        fun httpClient(client: HttpClient): Builder {
            this.httpClient = client
            return this
        }

        fun configureClient(config: HttpClientConfig<CIOEngineConfig>.() -> Unit): Builder {
            this.httpClientBuilder = config
            return this
        }

        fun build(): KtorRpcTransport {
            val converter = rpcConverter ?: JacksonRpcConverter()
            val responseJsonConverter = ResponseJsonConverter()

            val client = httpClient ?: HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson()
                }
                httpClientBuilder()
            }

            return KtorRpcTransport(client, converter, responseJsonConverter, target)
        }
    }
}
