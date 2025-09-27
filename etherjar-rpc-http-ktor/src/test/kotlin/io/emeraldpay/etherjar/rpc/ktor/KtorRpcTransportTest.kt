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

import io.emeraldpay.etherjar.rpc.*
import io.emeraldpay.etherjar.rpc.kotlin.CoroutineBatchItem
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.URI

class KtorRpcTransportTest : ShouldSpec({

    fun createMockHttpClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseBody,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                jackson()
            }
        }
    }

    should("execute single RPC call successfully") {
        // Given
        val responseBody = """{"jsonrpc":"2.0","id":1,"result":"Geth/v1.10.0-stable"}"""
        val mockClient = createMockHttpClient(responseBody)

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call = Commands.web3().clientVersion()
        val batchItem = CoroutineBatchItem(call, 1)

        // When
        val responses = transport.execute(listOf(batchItem))

        // Then
        responses shouldHaveSize 1
        responses[0].isSuccessful shouldBe true
        responses[0].value shouldBe "Geth/v1.10.0-stable"

        transport.close()
        mockClient.close()
    }

    should("execute batch of RPC calls successfully") {
        // Given
        val responseBody = """[
            {"jsonrpc":"2.0","id":1,"result":"Geth/v1.10.0"},
            {"jsonrpc":"2.0","id":2,"result":"Parity/v2.5.0"}
        ]"""
        val mockClient = createMockHttpClient(responseBody)

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call1 = Commands.web3().clientVersion()
        val call2 = Commands.web3().clientVersion()
        val batchItem1 = CoroutineBatchItem(call1, 1)
        val batchItem2 = CoroutineBatchItem(call2, 2)

        // When
        val responses = transport.execute(listOf(batchItem1, batchItem2))

        // Then
        responses shouldHaveSize 2
        responses[0].isSuccessful shouldBe true
        responses[0].value shouldBe "Geth/v1.10.0"
        responses[1].isSuccessful shouldBe true
        responses[1].value shouldBe "Parity/v2.5.0"

        transport.close()
        mockClient.close()
    }

    should("handle RPC error response") {
        // Given
        val responseBody = """{"jsonrpc":"2.0","id":1,"error":{"code":-32601,"message":"Method not found"}}"""
        val mockClient = createMockHttpClient(responseBody)

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call = Commands.web3().clientVersion()
        val batchItem = CoroutineBatchItem(call, 1)

        // When
        val responses = transport.execute(listOf(batchItem))

        // Then
        responses shouldHaveSize 1
        responses[0].isError shouldBe true
        responses[0].error!!.error.code shouldBe RpcResponseError.CODE_METHOD_NOT_EXIST
        responses[0].error!!.rpcMessage shouldBe "Method not found"

        transport.close()
        mockClient.close()
    }

    should("handle HTTP error status") {
        // Given
        val mockClient = createMockHttpClient("Internal Server Error", HttpStatusCode.InternalServerError)

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call = Commands.web3().clientVersion()
        val batchItem = CoroutineBatchItem(call, 1)

        // When
        val responses = transport.execute(listOf(batchItem))

        // Then
        responses shouldHaveSize 1
        responses[0].isError shouldBe true
        responses[0].error!!.error.code shouldBe RpcResponseError.CODE_INTERNAL_ERROR
        responses[0].error!!.rpcMessage shouldBe "HTTP error: 500 Internal Server Error"

        transport.close()
        mockClient.close()
    }

    should("handle mixed success and error responses in batch") {
        // Given
        val responseBody = """[
            {"jsonrpc":"2.0","id":1,"result":"Geth/v1.10.0"},
            {"jsonrpc":"2.0","id":2,"error":{"code":-32602,"message":"Invalid params"}}
        ]"""
        val mockClient = createMockHttpClient(responseBody)

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call1 = Commands.web3().clientVersion()
        val call2 = Commands.web3().clientVersion()
        val batchItem1 = CoroutineBatchItem(call1, 1)
        val batchItem2 = CoroutineBatchItem(call2, 2)

        // When
        val responses = transport.execute(listOf(batchItem1, batchItem2))

        // Then
        responses shouldHaveSize 2
        responses[0].isSuccessful shouldBe true
        responses[0].value shouldBe "Geth/v1.10.0"
        responses[1].isError shouldBe true
        responses[1].error!!.error.code shouldBe RpcResponseError.CODE_INVALID_METHOD_PARAMS
        responses[1].error!!.rpcMessage shouldBe "Invalid params"

        transport.close()
        mockClient.close()
    }

    should("handle network exception") {
        // Given
        val mockClient = HttpClient(MockEngine) {
            engine {
                addHandler {
                    throw RuntimeException("Network connection failed")
                }
            }
            install(ContentNegotiation) {
                jackson()
            }
        }

        val transport = KtorRpcTransport.newBuilder()
            .httpClient(mockClient)
            .build()

        val call = Commands.web3().clientVersion()
        val batchItem = CoroutineBatchItem(call, 1)

        // When
        val responses = transport.execute(listOf(batchItem))

        // Then
        responses shouldHaveSize 1
        responses[0].isError shouldBe true
        responses[0].error!!.error.code shouldBe RpcResponseError.CODE_INTERNAL_ERROR
        responses[0].error!!.rpcMessage shouldBe "Request failed: Network connection failed"

        transport.close()
        mockClient.close()
    }

    should("configure custom URI in builder") {
        // Given & When
        val transport = KtorRpcTransport.newBuilder()
            .connectTo("https://mainnet.infura.io/v3/YOUR-PROJECT-ID")
            .build()

        // Then - Just verify it builds without error and can be closed
        transport.close()
    }

    should("configure custom URI from URI object in builder") {
        // Given & When
        val uri = URI.create("https://mainnet.infura.io/v3/YOUR-PROJECT-ID")
        val transport = KtorRpcTransport.newBuilder()
            .connectTo(uri)
            .build()

        // Then - Just verify it builds without error and can be closed
        transport.close()
    }

    should("build with default HttpClient when none provided") {
        // Given & When
        val transport = KtorRpcTransport.newBuilder()
            .build()

        // Then - Just verify it builds without error and can be closed
        transport.close()
    }
})
