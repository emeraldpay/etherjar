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
package io.emeraldpay.etherjar.rpc.emerald

import com.google.protobuf.ByteString
import io.emeraldpay.api.Chain
import io.emeraldpay.api.proto.BlockchainGrpc
import io.emeraldpay.api.proto.BlockchainOuterClass
import io.emeraldpay.api.proto.Common
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.rpc.Commands
import io.emeraldpay.etherjar.rpc.json.BlockTag
import io.grpc.stub.StreamObserver
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class ReactorEmeraldClientSpec extends Specification {

    def "Simple call"() {
        setup:

        BlockchainOuterClass.NativeCallRequest actRequest = null

        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request

                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(true)
                        .setPayload(ByteString.copyFromUtf8('"0xab5461ca4b100000"'))
                        .build()
                )
                responseObserver.onCompleted()
            }
        })

        ReactorEmeraldClient client = ReactorEmeraldClient.newBuilder()
            .connectUsing(server.channel)
            .build()

        when:
        def act = client.execute(Commands.eth().getBalance(Address.from("0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983"), BlockTag.LATEST))

        then:
        StepVerifier.create(act)
            .expectNext(Wei.ofEthers(12.3456))
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        actRequest == BlockchainOuterClass.NativeCallRequest.newBuilder()
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_getBalance")
                    .setPayload(ByteString.copyFromUtf8('["0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983","latest"]'))
            )
            .build()
    }

    def "Server error"() {
        setup:

        BlockchainOuterClass.NativeCallRequest actRequest = null

        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request
                responseObserver.onError(new Exception("Test error"))
            }
        })

        ReactorEmeraldClient client = ReactorEmeraldClient.newBuilder()
            .connectUsing(server.channel)
            .build()

        when:
        def act = client.execute(Commands.eth().getBalance(Address.from("0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983"), BlockTag.LATEST))

        then:
        StepVerifier.create(act)
            .expectError()
            .verify(Duration.ofSeconds(3))
    }

    def "Uses chain"() {
        setup:

        BlockchainOuterClass.NativeCallRequest actRequest = null

        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request

                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(true)
                        .setPayload(ByteString.copyFromUtf8('"0xab5461ca4b100000"'))
                        .build()
                )
                responseObserver.onCompleted()
            }
        })

        ReactorEmeraldClient client = ReactorEmeraldClient.newBuilder()
            .connectUsing(server.channel)
            .build()
            .copyForChain(Chain.ETHEREUM_CLASSIC)

        when:
        def act = client.execute(Commands.eth().getBalance(Address.from("0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983"), BlockTag.LATEST))

        then:
        StepVerifier.create(act)
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        actRequest == BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChain(Common.ChainRef.CHAIN_ETHEREUM_CLASSIC)
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_getBalance")
                    .setPayload(ByteString.copyFromUtf8('["0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983","latest"]'))
            )
            .build()
    }

    def "Uses selector"() {
        setup:

        BlockchainOuterClass.NativeCallRequest actRequest = null

        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request

                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(true)
                        .setPayload(ByteString.copyFromUtf8('"0xab5461ca4b100000"'))
                        .build()
                )
                responseObserver.onCompleted()
            }
        })

        ReactorEmeraldClient client = ReactorEmeraldClient.newBuilder()
            .connectUsing(server.channel)
            .build()
            .copyForChain(Chain.ETHEREUM_CLASSIC)
            .copyWithSelector(
                BlockchainOuterClass.Selector.newBuilder()
                .setLabelSelector(
                    BlockchainOuterClass.LabelSelector.newBuilder()
                    .setName("test")
                    .addValue("baz")
                )
                .build())

        when:
        def act = client.execute(Commands.eth().getBalance(Address.from("0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983"), BlockTag.LATEST))

        then:
        StepVerifier.create(act)
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        actRequest == BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChain(Common.ChainRef.CHAIN_ETHEREUM_CLASSIC)
            .setSelector(
                BlockchainOuterClass.Selector.newBuilder()
                    .setLabelSelector(
                        BlockchainOuterClass.LabelSelector.newBuilder()
                            .setName("test")
                            .addValue("baz")
                    )
            )
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_getBalance")
                    .setPayload(ByteString.copyFromUtf8('["0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983","latest"]'))
            )
            .build()
    }

    def "Fail call if execution doesn't return result"() {
        setup:

        BlockchainOuterClass.NativeCallRequest actRequest = null

        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request
                responseObserver.onCompleted()
            }
        })

        ReactorEmeraldClient client = ReactorEmeraldClient.newBuilder()
            .connectUsing(server.channel)
            .build()

        when:
        def act = client.execute(Commands.eth().getBalance(Address.from("0x1a9ce518a4a2d7a908f22547ef5e3aa29946f983"), BlockTag.LATEST))

        then:
        StepVerifier.create(act)
            .expectError()
            .verify(Duration.ofSeconds(3))
    }

    def "Setup with TLS enables ALPN and HTTP2"() {
        // GrpcSslContexts.ensureAlpnAndH2Enabled may throw exceptions with "ALPN must be enabled and list HTTP/2 as a supported protocol."
        setup:
        def dir = new File("src/test/resources/")
        when:
        def act = ReactorEmeraldClient.newBuilder()
            .connectTo("localhost", 2448)
            .clientCertificate(new File(dir,"cert.pem"), new File(dir, "key_p8.pem"))
            .build()

        then:
        act != null
    }
}
