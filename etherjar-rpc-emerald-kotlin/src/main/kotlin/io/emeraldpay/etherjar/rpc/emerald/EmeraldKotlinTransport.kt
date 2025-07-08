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

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ByteString
import io.emeraldpay.api.proto.BlockchainGrpcKt
import io.emeraldpay.api.proto.BlockchainOuterClass
import io.emeraldpay.api.proto.Common
import io.emeraldpay.api.Chain
import io.emeraldpay.etherjar.rpc.*
import io.emeraldpay.etherjar.rpc.ktor.CoroutineRpcTransport
import io.emeraldpay.etherjar.rpc.ktor.CoroutineBatchItem
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder
import io.netty.handler.ssl.SslContextBuilder
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/**
 * <p>RPC Transport using Kotlin coroutines over gRPC for Emerald API compatible servers.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * val transport = EmeraldKotlinTransport.newBuilder()
 *     .connectTo("dshackle-server:9001")
 *     .chain(Chain.ETHEREUM)
 *     .build()
 * }</pre>
 */
class EmeraldKotlinTransport(
    private val stub: BlockchainGrpcKt.BlockchainCoroutineStub,
    private val objectMapper: ObjectMapper,
    private val rpcConverter: JacksonRpcConverter,
    private val chainRef: Common.ChainRef
) : CoroutineRpcTransport {

    private var selector: BlockchainOuterClass.Selector? = null

    /**
     * Reuse the same transport and channel for a new client configured for a different chain.
     * It copies the current configuration and shares the channel with the new copy.
     *
     * @param chain chain for new calls through this transport
     * @return new instance of EmeraldKotlinTransport configured for a new chain
     */
    fun copyForChain(chain: Chain): EmeraldKotlinTransport {
        return EmeraldKotlinTransport(stub, objectMapper, rpcConverter, Common.ChainRef.forNumber(chain.id))
    }

    /**
     * Reuse the same transport and channel for a new client that will select a particular nodes
     * to execute.
     *
     * @param selector node selector (it may be null, to copy without a selector)
     * @return new instance of EmeraldKotlinTransport configured with new selector
     */
    fun copyWithSelector(selector: BlockchainOuterClass.Selector?): EmeraldKotlinTransport {
        val copy = EmeraldKotlinTransport(stub, objectMapper, rpcConverter, chainRef)
        copy.selector = selector
        return copy
    }

    internal fun convert(
        items: List<CoroutineBatchItem<*, *>>,
        idMapping: MutableMap<Int, CoroutineBatchItem<*, *>>
    ): BlockchainOuterClass.NativeCallRequest {
        val requestBuilder = BlockchainOuterClass.NativeCallRequest.newBuilder()
            .setChain(chainRef)

        selector?.let { requestBuilder.selector = it }

        items.forEach { item ->
            val json = objectMapper.writeValueAsString(item.call.params)
            requestBuilder.addItems(
                BlockchainOuterClass.NativeCallItem.newBuilder()
                    .setId(item.id)
                    .setMethod(item.call.method)
                    .setPayload(ByteString.copyFromUtf8(json))
                    .build()
            )
            idMapping[item.id] = item
        }

        return requestBuilder.build()
    }

    override suspend fun execute(items: List<CoroutineBatchItem<*, *>>): List<RpcCallResponse<*, *>> {
        if (items.isEmpty()) {
            return emptyList()
        }

        val idMapping = mutableMapOf<Int, CoroutineBatchItem<*, *>>()
        val request = convert(items, idMapping)

        val result = mutableListOf<RpcCallResponse<*, *>>()

        stub.nativeCall(request).collect { response ->
            val batchItem = idMapping[response.id]
            if (batchItem != null) {
                val callResponse = convertToRpcResponse(batchItem, response)
                result.add(callResponse)
            }
        }

        return result
    }

    private fun <JS, RES> convertToRpcResponse(
        request: CoroutineBatchItem<JS, RES>,
        response: BlockchainOuterClass.NativeCallReplyItem
    ): RpcCallResponse<JS, RES> {
        val responseJson = convertToResponseJson(request, response)
        return ResponseJsonConverter().convert(request.call, responseJson)
    }

    private fun <JS, RES> convertToResponseJson(
        request: CoroutineBatchItem<JS, RES>,
        response: BlockchainOuterClass.NativeCallReplyItem
    ): ResponseJson<JS, Int> {
        val responseJson = ResponseJson<JS, Int>()
        responseJson.id = response.id

        if (response.succeed) {
            try {
                val value: JS = rpcConverter.fromJsonResult(response.payload.newInput(), request.call.jsonType)
                responseJson.result = value
            } catch (e: RpcException) {
                responseJson.result = null
                responseJson.error = e.error
            }
        } else {
            responseJson.result = null
            responseJson.error = RpcException(
                RpcResponseError.CODE_INTERNAL_ERROR,
                response.errorMessage
            ).error
        }

        return responseJson
    }

    @Throws(IOException::class)
    override fun close() {
        val channel = stub.channel
        if (channel is ManagedChannel) {
            channel.shutdownNow()
            try {
                if (!channel.awaitTermination(60, TimeUnit.SECONDS)) {
                    throw IOException("Channel was not closed within timeout")
                }
            } catch (e: InterruptedException) {
                throw IOException("Channel was not closed", e)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private var channelBuilder: NettyChannelBuilder? = null
        private var sslContextBuilder: SslContextBuilder? = null
        private var channel: Channel? = null
        private var stub: BlockchainGrpcKt.BlockchainCoroutineStub? = null
        private var interceptors: Array<ClientInterceptor>? = null
        private var objectMapper: ObjectMapper? = null
        private var rpcConverter: JacksonRpcConverter? = null
        private var chain: Chain? = null

        /**
         * Setup for an existing channel
         *
         * @param channel existing channel
         * @return builder
         */
        fun connectUsing(channel: Channel): Builder {
            this.channel = channel
            this.channelBuilder = null
            this.sslContextBuilder = null
            return this
        }

        /**
         * Setup with an existing stub. All other settings related to connection will be ignored
         *
         * @param stub existing stub
         * @return builder
         */
        fun connectUsing(stub: BlockchainGrpcKt.BlockchainCoroutineStub): Builder {
            this.stub = stub
            this.channel = null
            this.channelBuilder = null
            this.sslContextBuilder = null
            return this
        }

        /**
         * Setup for address formatted as host:port
         *
         * @param hostPort address in host:port format
         * @return builder
         */
        fun connectTo(hostPort: String): Builder {
            val parts = hostPort.split(":")
            return if (parts.size == 1) {
                connectTo(hostPort, 9001)
            } else {
                connectTo(parts[0], parts[1].toInt())
            }
        }

        /**
         * Setup connection to specified host and port
         *
         * @param host host
         * @param port port
         * @return builder
         */
        fun connectTo(host: String, port: Int): Builder {
            channelBuilder = NettyChannelBuilder.forAddress(host, port).usePlaintext()
            channel = null
            return this
        }

        /**
         * Setup connection using URI
         *
         * @param uri uri (only host:port are used)
         * @return builder
         */
        fun connectTo(uri: URI): Builder {
            val host = uri.host
            val port = if (uri.port == -1) 9001 else uri.port
            return connectTo(host, port)
        }

        /**
         * Setup custom ObjectMapper
         *
         * @param objectMapper custom Object Mapper
         * @return builder
         */
        fun objectMapper(objectMapper: ObjectMapper): Builder {
            this.objectMapper = objectMapper
            return this
        }

        /**
         * Setup custom RpcConverter
         *
         * @param rpcConverter custom RpcConverter
         * @return builder
         */
        fun rpcConverter(rpcConverter: JacksonRpcConverter): Builder {
            this.rpcConverter = rpcConverter
            return this
        }

        /**
         * Setup target chain
         *
         * @param chain chain
         * @return builder
         */
        fun chain(chain: Chain): Builder {
            this.chain = chain
            return this
        }

        /**
         * Add interceptors to the client calls
         *
         * @param interceptors interceptors
         * @return builder
         */
        fun interceptors(vararg interceptors: ClientInterceptor): Builder {
            this.interceptors = arrayOf(*interceptors)
            return this
        }

        /**
         * Validates configuration and builds transport
         *
         * @return configured transport
         * @throws SSLException if problem with TLS certificates
         */
        @Throws(SSLException::class)
        fun build(): EmeraldKotlinTransport {
            val finalStub = stub ?: run {
                val finalChannel = channel ?: run {
                    val builder = channelBuilder ?: throw IllegalStateException("No connection configured")
                    sslContextBuilder?.let { ssl ->
                        builder.useTransportSecurity().sslContext(ssl.build())
                    }
                    builder.build()
                }

                var newStub = BlockchainGrpcKt.BlockchainCoroutineStub(finalChannel)
                interceptors?.let { newStub = newStub.withInterceptors(*it) }
                newStub
            }

            val finalObjectMapper = objectMapper ?: run {
                rpcConverter?.objectMapper ?: JacksonRpcConverter.createJsonMapper()
            }

            val finalRpcConverter = rpcConverter ?: JacksonRpcConverter(finalObjectMapper)
            val finalChain = chain ?: Chain.UNSPECIFIED
            val chainRef = Common.ChainRef.forNumber(finalChain.id)

            return EmeraldKotlinTransport(finalStub, finalObjectMapper, finalRpcConverter, chainRef)
        }
    }
}
