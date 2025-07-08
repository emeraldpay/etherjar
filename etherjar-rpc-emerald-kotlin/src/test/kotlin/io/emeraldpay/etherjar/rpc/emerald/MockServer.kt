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

import io.emeraldpay.api.proto.BlockchainGrpc
import io.emeraldpay.api.proto.BlockchainGrpcKt
import io.grpc.Channel
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder

object MockServer {
    
    fun createStubFor(impl: BlockchainGrpc.BlockchainImplBase): BlockchainGrpcKt.BlockchainCoroutineStub {
        val serverName = InProcessServerBuilder.generateName()
        val server = InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(impl)
            .build()
            .start()
            
        val channel = InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build()
            
        return BlockchainGrpcKt.BlockchainCoroutineStub(channel)
    }
    
    fun createChannelFor(impl: BlockchainGrpc.BlockchainImplBase): Channel {
        val serverName = InProcessServerBuilder.generateName()
        val server = InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(impl)
            .build()
            .start()
            
        return InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build()
    }
}