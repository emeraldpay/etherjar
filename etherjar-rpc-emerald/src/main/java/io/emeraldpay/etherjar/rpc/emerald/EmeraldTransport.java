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
package io.emeraldpay.etherjar.rpc.emerald;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.emeraldpay.api.proto.BlockchainGrpc;
import io.emeraldpay.api.proto.BlockchainOuterClass;
import io.emeraldpay.api.proto.Common;
import io.emeraldpay.api.Chain;
import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.emeraldpay.etherjar.rpc.*;
import io.emeraldpay.etherjar.rpc.ResponseJson;
import io.emeraldpay.etherjar.rpc.RpcTransport;
import io.netty.handler.ssl.SslContextBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.emeraldpay.api.proto.BlockchainOuterClass.*;

/**
 * RPC Transport over gRPC for Emerald API compatible servers (such as Emerald Dshackle)
 * <br>
 *
 * Example usage:
 * <pre><code>
 * RpcTransport transport = EmeraldTransport.newBuilder()
 *                 .forAddress("dshackle-server:9001")
 *                 .setThreadsCount(8)
 *                 .setChain(Chain.ETHEREUM)
 *                 .build();
 * RpcClient client = new DefaultRpcClient(transport);
 * </code></pre>
 */
@NullMarked
public class EmeraldTransport implements RpcTransport<DefaultBatch.FutureBatchItem> {

    private final BlockchainGrpc.BlockchainBlockingStub blockingStub;

    private final ResponseJsonConverter responseJsonConverter = new ResponseJsonConverter();


    private final ObjectMapper objectMapper;
    private final JacksonRpcConverter rpcConverter;
    private final ExecutorService executorService;
    private final Common.ChainRef chainRef;

    private BlockchainOuterClass.@Nullable  Selector  selector;

    public EmeraldTransport(BlockchainGrpc.BlockchainBlockingStub stub,
                            ObjectMapper objectMapper,
                            JacksonRpcConverter rpcConverter,
                            ExecutorService executorService,
                            Common.ChainRef chainRef) {
        this.objectMapper = objectMapper;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
        this.chainRef = chainRef;
        blockingStub = stub;
    }

    /**
     * Start new configuration builder
     *
     * @return new default configuration
     */
    public static EmeraldTransport.Builder newBuilder() {
        return new EmeraldTransport.Builder();
    }

    /**
     * Reuse same transport and channel for a new client configured for a different chain.
     * It copies current configuration and shares channel with the new copy.
     *
     * @param chain chain for new calls through this transport
     * @return new instance of EmeraldGrpcTransport configured for new chain
     */
    public EmeraldTransport copyForChain(Chain chain) {
        return new EmeraldTransport(blockingStub, objectMapper, rpcConverter, executorService, Common.ChainRef.forNumber(chain.getId()));
    }

    /**
     * Reuse same transport and channel for a new client that will select a particular nodes
     * to execute.<br>
     *
     * Example:
     * <pre><code>
     * BlockchainOuterClass.Selector selector = BlockchainOuterClass.Selector.newBuilder().setAndSelector(
     *         BlockchainOuterClass.AndSelector.newBuilder()
     *                 .addSelectors(
     *                         BlockchainOuterClass.Selector.newBuilder().setLabelSelector(
     *                                 BlockchainOuterClass.LabelSelector.newBuilder()
     *                                         .setName("archive")
     *                                         .addValue("true")
     *                                         .build()
     *                         )
     *                 )
     *                 .addSelectors(
     *                         BlockchainOuterClass.Selector.newBuilder().setLabelSelector(
     *                                 BlockchainOuterClass.LabelSelector.newBuilder()
     *                                         .setName("provider")
     *                                         .addValue("parity")
     *                                         .build()
     *                         )
     *                 )
     *                 .build()
     * ).build()
     *
     * RpcClient archiveClient = new DefaultRpcClient(transport.copyWithSelector(selector));
     * </code></pre>
     *
     * @param selector node selector (may be null, to copy without selector)
     *
     * @return new instance of EmeraldGrpcTransport configured with new selector
     */
    public EmeraldTransport copyWithSelector(@Nullable Selector selector) {
        EmeraldTransport copy = new EmeraldTransport(blockingStub, objectMapper, rpcConverter, executorService, chainRef);
        copy.selector = selector;
        return copy;
    }

    public NativeCallRequest convert(List<DefaultBatch.FutureBatchItem> items, Map<Integer, DefaultBatch.FutureBatchItem> idMapping) {
        final NativeCallRequest.Builder req = NativeCallRequest.newBuilder();
        req.setChain(chainRef);
        if (selector != null) {
            req.setSelector(selector);
        }
        final AtomicInteger i = new AtomicInteger(1); // because for lambda, must be final
        items.forEach( item -> {
            int id = i.getAndIncrement();
            String json;
            try {
                json = objectMapper.writeValueAsString(item.getCall().getParams());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            req.addItems(
                NativeCallItem.newBuilder()
                    .setId(id)
                    .setMethod(item.getCall().getMethod())
                    .setPayload(ByteString.copyFromUtf8(json))
                    .build()
            );
            idMapping.put(id, item);
        });
        return req.build();
    }

    @Override
    public CompletableFuture<Iterable<RpcCallResponse>> execute(List<DefaultBatch.FutureBatchItem> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<Iterable<RpcCallResponse>> f = new CompletableFuture<>();
        executorService.execute(() -> {
            final Map<Integer, DefaultBatch.FutureBatchItem> idMapping = new HashMap<>(items.size());
            NativeCallRequest req;
            try {
                req = convert(items, idMapping);
            } catch (Exception e) {
                f.completeExceptionally(e);
                return;
            }
            List<RpcCallResponse> result = new ArrayList<>();
            Iterator<NativeCallReplyItem> responses;
            try {
                responses = blockingStub.nativeCall(req);
                responses.forEachRemaining((resp) -> {
                    int id = resp.getId();
                    DefaultBatch.FutureBatchItem request = idMapping.get(id);
                    RpcCallResponse callResponse = convertUnchecked(request, resp);
                    result.add(callResponse);
                });
            } catch (StatusRuntimeException e) {
                if (result.isEmpty()) {
                    f.completeExceptionally(e);
                    return;
                }
            }
            f.complete(result);
        });
        return f;
    }

    @SuppressWarnings("unchecked")
    private RpcCallResponse convertUnchecked(DefaultBatch.FutureBatchItem request, NativeCallReplyItem resp) {
        return convertToRpcResponse(request, resp);
    }

    public <JS, RES> RpcCallResponse<JS, RES> convertToRpcResponse(DefaultBatch.FutureBatchItem<JS, RES> request, NativeCallReplyItem resp) {
        ResponseJson<JS, Integer> responseJson = convertToResponseJson(request, resp);
        return responseJsonConverter.convert(request.getCall(), responseJson);
    }

    public <JS, RES> ResponseJson<JS, Integer> convertToResponseJson(DefaultBatch.FutureBatchItem<JS, RES> request, NativeCallReplyItem resp) {
        ResponseJson<JS, Integer> responseJson = new ResponseJson<>();
        if (resp.getSucceed()) {
            try {
                JS value = read(resp.getPayload(), request);
                responseJson.setId(resp.getId());
                responseJson.setResult(value);
            } catch (RpcException e) {
                responseJson.setResult(null);
                responseJson.setError(e.getError());
            }
        } else {
            responseJson.setResult(null);
            responseJson.setError(new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, resp.getErrorMessage()).getError());
        }
        return responseJson;
    }

    public <JS, RES> JS read(ByteString bytes, DefaultBatch.FutureBatchItem<JS, RES> request) throws RpcException {
        return rpcConverter.fromJsonResult(bytes.newInput(), request.getCall().getJsonType());
    }

    @Override
    public void close() throws IOException {
        Channel channel = this.blockingStub.getChannel();
        if (channel instanceof ManagedChannel) {
            ((ManagedChannel) channel).shutdownNow();
            try {
                ((ManagedChannel) channel).awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IOException("Channel was not closed", e);
            }
        }
    }

    public static class Builder {

        @Nullable
        private NettyChannelBuilder channelBuilder;

        @Nullable
        private Function<NettyChannelBuilder, ManagedChannelBuilder<?>> channelUpdate;

        private boolean useLoadBalancing = true;

        @Nullable
        private SslContextBuilder sslContextBuilder;
        @Nullable
        private Channel channel;
        private BlockchainGrpc.@Nullable BlockchainBlockingStub stub;
        @Nullable
        private ClientInterceptor[] interceptors;

        @Nullable
        private ObjectMapper objectMapper;
        @Nullable
        private JacksonRpcConverter rpcConverter;
        @Nullable
        private ExecutorService executorService;

        @Nullable
        private Chain chain;

        /**
         * Setup for an existing channel
         *
         * @param channel existing channel
         * @return builder
         */
        public Builder connectUsing(Channel channel) {
            this.channel = channel;
            channelBuilder = null;
            sslContextBuilder = null;
            return this;
        }

        /**
         * Setup with an existing stub. All other settings related to connection will be ignored
         *
         * @param stub existing stub
         * @return builder
         */
        public Builder connectUsing(BlockchainGrpc.BlockchainBlockingStub stub) {
            this.stub = stub;
            this.channel = null;
            this.channelBuilder = null;
            this.sslContextBuilder = null;
            return this;
        }

        /**
         * Apply a custom modification for the default NettyChannelBuilder
         *
         * @param customChannel function to update the Channel Builder
         * @return builder
         */
        public Builder withChannelBuilder(Function<NettyChannelBuilder, ManagedChannelBuilder<?>> customChannel) {
            this.channelUpdate = customChannel;
            return this;
        }

        /**
         * By default, connection uses a Round Robin load balancing when it's available if a host name specified for the connection,
         * and it resolves to multiple IPs. This method disables the load balancing.
         *
         * @return builder
         */
        public Builder disableLoadBalancing() {
            useLoadBalancing = false;
            return this;
        }

        /**
         * Setup for address formatted as host:port
         *
         * @param hostPort address in host:port format
         * @return builder
         */
        public Builder connectTo(String hostPort) {
            String[] parts = hostPort.split(":");
            if (parts.length == 1) {
                return connectTo(hostPort, 9001);
            } else {
                return connectTo(parts[0], Integer.parseInt(parts[1]));
            }
        }

        /**
         *
         * @param host host
         * @param port port
         * @return builder
         */
        public Builder connectTo(String host, int port) {
            channelBuilder = NettyChannelBuilder.forAddress(host, port).usePlaintext();
            channel = null;
            return this;
        }

        /**
         *
         * @param uri uri (only host:port are used, could be anything like grpc://dshakle-server:9001)
         * @return builder
         */
        public Builder connectTo(URI uri) {
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 9001;
            }
            return connectTo(host, port);
        }

        /**
         * Setup x509 certificate for target server
         *
         * @param certificate x509 certificate
         * @return builder
         */
        public Builder trustedCertificate(InputStream certificate) {
            if (sslContextBuilder == null) {
                sslContextBuilder = SslContextBuilder.forClient();
                channelBuilder.useTransportSecurity();
            }
            sslContextBuilder = sslContextBuilder.trustManager(certificate);
            return this;
        }

        /**
         * Setup x509 certificate for target server
         *
         * @param certificate x509 certificate
         * @return builder
         */
        public Builder trustedCertificate(File certificate) {
            if (sslContextBuilder == null) {
                sslContextBuilder = SslContextBuilder.forClient();
                channelBuilder.useTransportSecurity();
            }
            sslContextBuilder = sslContextBuilder.trustManager(certificate);
            return this;
        }

        /**
         * Setup client certificate
         *
         * @param certificate x509 certificate
         * @param key private key for the certificate in PKCS8 format
         * @return builder
         */
        public Builder clientCertificate(InputStream certificate, InputStream key) {
            if (sslContextBuilder == null) {
                sslContextBuilder = SslContextBuilder.forClient();
                channelBuilder.useTransportSecurity();
            }
            sslContextBuilder = sslContextBuilder.keyManager(certificate, key);
            return this;
        }

        /**
         * Setup client certificate
         *
         * @param certificate x509 certificate
         * @param key private key for the certificate in PKCS8 format
         * @return builder
         */
        public Builder clientCertificate(File certificate, File key) {
            if (sslContextBuilder == null) {
                sslContextBuilder = SslContextBuilder.forClient();
                channelBuilder.useTransportSecurity();
            }
            sslContextBuilder = sslContextBuilder.keyManager(certificate, key);
            return this;
        }

        /**
         *
         * @param objectMapper custom Object Mapper
         * @return builder
         */
        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         *
         * @param rpcConverter custom RpcConverter
         * @return builder
         */
        public Builder rpcConverter(JacksonRpcConverter rpcConverter) {
            this.rpcConverter = rpcConverter;
            return this;
        }

        /**
         * By default GrpcTransport uses a fixed thread executor with 2 threads.
         *
         * @param executorService custom execute service
         * @return builder
         */
        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * Setup threads count for default thread executor. Default configuration is 2 threads.
         *
         * @param threads fixed threads count
         * @return builder
         */
        public Builder threadsCount(int threads) {
            executorService = Executors.newFixedThreadPool(threads, r -> new Thread(r,"emerald-grpc"));
            return this;
        }

        /**
         *
         * @param chain chain
         * @return builder
         */
        public Builder chain(Chain chain) {
            this.chain = chain;
            return this;
        }

        /**
         * Add interceptors to the client calls
         *
         * @param interceptors interceptors
         * @return builder
         */
        public Builder interceptors(ClientInterceptor... interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        /**
         * Validates configuration and builds GrpcTransport
         *
         * @return configured grpc transport
         * @throws SSLException if problem with TLS certificates
         */
        public EmeraldTransport build() throws SSLException {
            if (stub == null) {
                if (channel == null) {
                    NettyChannelBuilder nettyBuilder = channelBuilder;
                    if (sslContextBuilder != null) {
                        nettyBuilder = nettyBuilder.useTransportSecurity()
                            .sslContext(sslContextBuilder.build());
                    }
                    if (useLoadBalancing) {
                        String policy = "round_robin";
                        if (LoadBalancerRegistry.getDefaultRegistry().getProvider(policy) != null) {
                            nettyBuilder = nettyBuilder.defaultLoadBalancingPolicy(policy);
                        }
                    }
                    ManagedChannelBuilder<?> finalBuilder;
                    if (this.channelUpdate != null) {
                        finalBuilder = this.channelUpdate.apply(nettyBuilder);
                    } else {
                        finalBuilder = nettyBuilder;
                    }
                    channel = finalBuilder.build();
                }
                stub = BlockchainGrpc.newBlockingStub(channel);
                if (interceptors != null) {
                    stub = stub.withInterceptors(interceptors);
                }
            }
            Objects.requireNonNull(stub);
            if (executorService == null) {
                threadsCount(2);
            }
            if (objectMapper == null) {
                if (rpcConverter != null) {
                    objectMapper = rpcConverter.getObjectMapper();
                } else {
                    objectMapper = JacksonRpcConverter.createJsonMapper();
                }
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter(objectMapper);
            }
            if (chain == null) {
                chain = Chain.UNSPECIFIED;
            }
            Common.ChainRef chainRef = Common.ChainRef.forNumber(chain.getId());
            return new EmeraldTransport(stub, objectMapper, rpcConverter, executorService, chainRef);
        }
    }
}
