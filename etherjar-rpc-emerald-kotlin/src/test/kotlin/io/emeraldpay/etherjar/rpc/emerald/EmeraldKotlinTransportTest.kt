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
package io.emeraldpay.etherjar.rpc.emerald

import com.google.protobuf.ByteString
import io.emeraldpay.api.Chain
import io.emeraldpay.api.proto.BlockchainGrpc
import io.emeraldpay.api.proto.BlockchainOuterClass
import io.emeraldpay.etherjar.rpc.Conversion
import io.emeraldpay.etherjar.rpc.RpcCall
import io.emeraldpay.etherjar.rpc.kotlin.CoroutineBatchItem
import io.grpc.stub.StreamObserver
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest

class EmeraldKotlinTransportTest : ShouldSpec({

    should("convert to correct Protobuf") {
        val transport = EmeraldKotlinTransport.newBuilder()
            .connectTo("localhost:2449")
            .chain(Chain.ETHEREUM)
            .build()

        val items = listOf(
            CoroutineBatchItem(RpcCall.create("eth_test", Int::class.java), 1),
            CoroutineBatchItem(RpcCall.create("eth_test2", Int::class.java, listOf("test", 14)), 2)
        )

        val idMapping = mutableMapOf<Int, CoroutineBatchItem<*, *>>()
        val request = transport.convert(items, idMapping)

        val expected = BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChainValue(Chain.ETHEREUM.id)
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_test")
                    .setPayload(ByteString.copyFromUtf8("[]"))
            )
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(2)
                    .setMethod("eth_test2")
                    .setPayload(ByteString.copyFromUtf8("[\"test\",14]"))
            )
            .build()

        request shouldBe expected
        transport.close()
    }

    should("throw exception on invalid param") {
        val transport = EmeraldKotlinTransport.newBuilder()
            .connectTo("localhost:2449")
            .chain(Chain.ETHEREUM)
            .build()

        val items = listOf(
            CoroutineBatchItem(RpcCall.create("eth_test", Int::class.java, listOf(Any())), 1)
        )

        val idMapping = mutableMapOf<Int, CoroutineBatchItem<*, *>>()

        try {
            transport.convert(items, idMapping)
            assert(false) { "Should have thrown exception" }
        } catch (e: Exception) {
            // Expected
        }

        transport.close()
    }

    should("use selector") {
        val selector = BlockchainOuterClass.Selector.newBuilder()
            .setAndSelector(
                BlockchainOuterClass.AndSelector.newBuilder()
                    .addSelectors(
                        BlockchainOuterClass.Selector.newBuilder().setLabelSelector(
                            BlockchainOuterClass.LabelSelector.newBuilder()
                                .setName("archive")
                                .addValue("true")
                        )
                    )
                    .addSelectors(
                        BlockchainOuterClass.Selector.newBuilder().setLabelSelector(
                            BlockchainOuterClass.LabelSelector.newBuilder()
                                .setName("provider")
                                .addValue("parity")
                        )
                    )
            )
            .build()

        val transport = EmeraldKotlinTransport.newBuilder()
            .connectTo("localhost:2449")
            .chain(Chain.ETHEREUM)
            .build()
            .copyWithSelector(selector)

        val items = listOf(
            CoroutineBatchItem(RpcCall.create("eth_test", Int::class.java, listOf("hello")), 1)
        )

        val idMapping = mutableMapOf<Int, CoroutineBatchItem<*, *>>()
        val request = transport.convert(items, idMapping)

        val expected = BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChainValue(Chain.ETHEREUM.id)
            .setSelector(selector)
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_test")
                    .setPayload(ByteString.copyFromUtf8("[\"hello\"]"))
            )
            .build()

        request shouldBe expected
        transport.close()
    }

    should("redefine chain") {
        val transport = EmeraldKotlinTransport.newBuilder()
            .connectTo("localhost:2449")
            .chain(Chain.ETHEREUM)
            .build()
            .copyForChain(Chain.ETHEREUM_CLASSIC)

        val items = listOf(
            CoroutineBatchItem(RpcCall.create("eth_test", Int::class.java), 1)
        )

        val idMapping = mutableMapOf<Int, CoroutineBatchItem<*, *>>()
        val request = transport.convert(items, idMapping)

        val expected = BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChainValue(Chain.ETHEREUM_CLASSIC.id)
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_test")
                    .setPayload(ByteString.copyFromUtf8("[]"))
            )
            .build()

        request shouldBe expected
        transport.close()
    }

    should("make actual call") {
        runTest {
        var actualRequest: BlockchainOuterClass.NativeCallRequest? = null

        val mockImpl = object : BlockchainGrpc.BlockchainImplBase() {
            override fun nativeCall(
                request: BlockchainOuterClass.NativeCallRequest,
                responseObserver: StreamObserver<BlockchainOuterClass.NativeCallReplyItem>
            ) {
                actualRequest = request
                println("requested $request")

                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(true)
                        .setPayload(ByteString.copyFromUtf8("\"0xab5461ca4b100\""))
                        .build()
                )
                responseObserver.onCompleted()
            }
        }

        val channel = MockServer.createChannelFor(mockImpl)
        val transport = EmeraldKotlinTransport.newBuilder()
            .connectUsing(channel)
            .chain(Chain.ETHEREUM)
            .build()

        val items = listOf(
            CoroutineBatchItem(
                RpcCall.create("eth_test").converted(Long::class.java, Conversion.asLong),
                1
            )
        )

        val result = transport.execute(items)

        result.size shouldBe 1
        result[0].error shouldBe null
        result[0].value shouldBe 0xab5461ca4b100L

        actualRequest shouldNotBe null

        transport.close()
        }
    }

    should("handle error response") {
        runTest {
        val mockImpl = object : BlockchainGrpc.BlockchainImplBase() {
            override fun nativeCall(
                request: BlockchainOuterClass.NativeCallRequest,
                responseObserver: StreamObserver<BlockchainOuterClass.NativeCallReplyItem>
            ) {
                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(false)
                        .setErrorMessage("Test error")
                        .build()
                )
                responseObserver.onCompleted()
            }
        }

        val channel = MockServer.createChannelFor(mockImpl)
        val transport = EmeraldKotlinTransport.newBuilder()
            .connectUsing(channel)
            .chain(Chain.ETHEREUM)
            .build()

        val items = listOf(
            CoroutineBatchItem(RpcCall.create("eth_test", String::class.java), 1)
        )

        val result = transport.execute(items)

        result.size shouldBe 1
        result[0].error shouldNotBe null
        result[0].error!!.message shouldBe "RPC Error -32603: Test error"

        transport.close()
        }
    }
})
