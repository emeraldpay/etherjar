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
import com.google.protobuf.Message
import io.emeraldpay.api.proto.BlockchainGrpc
import io.emeraldpay.api.proto.BlockchainOuterClass
import io.emeraldpay.api.Chain
import io.grpc.stub.StreamObserver
import io.emeraldpay.etherjar.rpc.Conversion
import io.emeraldpay.etherjar.rpc.DefaultBatch
import io.emeraldpay.etherjar.rpc.RpcCall
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class EmeraldTransportSpec extends Specification {

    def "Convert to correct Protobuf"() {
        setup:
        def transport = EmeraldTransport.newBuilder().connectTo("localhost:2449").chain(Chain.ETHEREUM).build()
        when:
        def batch = new DefaultBatch()
        batch.add(RpcCall.create("eth_test", Integer))
        batch.add(RpcCall.create("eth_test2", Integer, ["test", 14]))
        def act = transport.convert(batch.getItems(), [:])
        then:
        act == BlockchainOuterClass.NativeCallRequest.newBuilder()
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
                    .setPayload(ByteString.copyFromUtf8('["test",14]'))
            )
            .build()

        cleanup:
        transport.close()
    }

    def "Throws exception on invalid param"() {
        setup:
        def transport = EmeraldTransport.newBuilder().connectTo("localhost:2449").chain(Chain.ETHEREUM).build()
        when:
        def batch = new DefaultBatch()
        batch.add(RpcCall.create("eth_test", Integer, [new Object()]))
        def act = transport.convert(batch.getItems(), [:])
        then:
        thrown(RuntimeException)

        cleanup:
        transport.close()
    }

    def "Uses selector"() {
        setup:
        Message.Builder selector = BlockchainOuterClass.Selector.newBuilder().setAndSelector(
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
        def transport = EmeraldTransport.newBuilder().connectTo("localhost:2449").chain(Chain.ETHEREUM).build()
            .copyWithSelector(selector.build())
        when:
        def batch = new DefaultBatch()
        batch.add(RpcCall.create("eth_test", Integer, ["hello"]))
        def act = transport.convert(batch.getItems(), [:])
        then:
        act == BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChainValue(Chain.ETHEREUM.id)
            .setSelector(selector.build())
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_test")
                    .setPayload(ByteString.copyFromUtf8('["hello"]'))
            )
            .build()

        cleanup:
        transport.close()
    }

    def "Redefines chain"() {
        setup:
        def transport = EmeraldTransport.newBuilder().connectTo("localhost:2449")
            .chain(Chain.ETHEREUM)
            .build()
            .copyForChain(Chain.ETHEREUM_CLASSIC)
        when:
        def batch = new DefaultBatch()
        batch.add(RpcCall.create("eth_test", Integer))
        def act = transport.convert(batch.getItems(), [:])
        then:
        act == BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChainValue(Chain.ETHEREUM_CLASSIC.id)
            .addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(1)
                    .setMethod("eth_test")
                    .setPayload(ByteString.copyFromUtf8("[]"))
            )
            .build()

        cleanup:
        transport.close()
    }

    def "Makes actual call"() {
        setup:
        BlockchainOuterClass.NativeCallRequest actRequest = null
        def server = MockServer.createFor(new BlockchainGrpc.BlockchainImplBase() {
            @Override
            void nativeCall(BlockchainOuterClass.NativeCallRequest request, StreamObserver<BlockchainOuterClass.NativeCallReplyItem> responseObserver) {
                actRequest = request
                println("requested $request")

                responseObserver.onNext(
                    BlockchainOuterClass.NativeCallReplyItem.newBuilder()
                        .setId(1)
                        .setSucceed(true)
                        .setPayload(ByteString.copyFromUtf8('"0xab5461ca4b100"'))
                        .build()
                )
                responseObserver.onCompleted()
            }
        })
        def transport = EmeraldTransport.newBuilder()
            .connectUsing(server.channel)
            .chain(Chain.ETHEREUM)
            .build()

        when:
        def batch = new DefaultBatch()
        batch.add(RpcCall.create("eth_test").converted(Long.class, Conversion.asLong))
        def act = transport.execute(batch.getItems()).get(300, TimeUnit.SECONDS).toList()

        then:
        act.size() == 1
        act[0].getError() == null
        act[0].getValue() == 0xab5461ca4b100

        cleanup:
        transport.close()
    }
}
