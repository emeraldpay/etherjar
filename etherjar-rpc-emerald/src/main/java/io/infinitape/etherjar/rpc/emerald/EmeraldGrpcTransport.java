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
package io.infinitape.etherjar.rpc.emerald;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.emeraldpay.api.proto.BlockchainGrpc;
import io.emeraldpay.api.proto.BlockchainOuterClass;
import io.emeraldpay.api.proto.Common;
import io.emeraldpay.grpc.Chain;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.transport.BatchStatus;
import io.infinitape.etherjar.rpc.transport.RpcTransport;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC Transport over gRPC for Emerald API compatible servers (such as Emerald Dshackle)
 * <br>
 *
 * Example usage:
 * <pre><code>
 * RpcTransport transport = EmeraldGrpcTransport.newBuilder()
 *                 .forAddress("dshackle-server:9001")
 *                 .setThreadsCount(8)
 *                 .setChain(Chain.ETHEREUM)
 *                 .build();
 * RpcClient client = new DefaultRpcClient(transport);
 * </code></pre>
 */
public class EmeraldGrpcTransport implements RpcTransport {

    private final ManagedChannel channel;
    private final BlockchainGrpc.BlockchainBlockingStub blockingStub;

    private ObjectMapper objectMapper;
    private RpcConverter rpcConverter;
    private ExecutorService executorService;
    private Common.ChainRef chainRef;
    private BlockchainOuterClass.Selector selector;

    public EmeraldGrpcTransport(ManagedChannel channel,
                                ObjectMapper objectMapper,
                                RpcConverter rpcConverter,
                                ExecutorService executorService,
                                Common.ChainRef chainRef) {
        this.channel = channel;
        this.objectMapper = objectMapper;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
        this.chainRef = chainRef;
        blockingStub = BlockchainGrpc.newBlockingStub(channel);
    }

    /**
     * Start new configuration builder
     *
     * @return new default configuration
     */
    public static EmeraldGrpcTransport.Builder newBuilder() {
        return new EmeraldGrpcTransport.Builder();
    }

    /**
     * Reuse same transport and channel for a new client configured for a different chain.
     * It copies current configuration and shares channel with the new copy.
     *
     * @param chain chain for new calls through this transport
     * @return new instance of EmeraldGrpcTransport configured for new chain
     */
    public EmeraldGrpcTransport copyForChain(Chain chain) {
        return new EmeraldGrpcTransport(channel, objectMapper, rpcConverter, executorService, Common.ChainRef.forNumber(chain.getId()));
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
     *                                         .setName("trace")
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
    public EmeraldGrpcTransport copyWithSelector(BlockchainOuterClass.Selector selector) {
        EmeraldGrpcTransport copy = new EmeraldGrpcTransport(channel, objectMapper, rpcConverter, executorService, chainRef);
        copy.selector = selector;
        return copy;
    }

    @Override
    public CompletableFuture<BatchStatus> execute(List<Batch.BatchItem<?, ?>> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(BatchStatus.empty());
        }
        CompletableFuture<BatchStatus> result = new CompletableFuture<>();
        executorService.execute(() -> {
            BlockchainOuterClass.NativeCallRequest.Builder req = BlockchainOuterClass.NativeCallRequest.newBuilder();
            req.setChain(chainRef);
            if (selector != null) {
                req.setSelector(selector);
            }
            AtomicInteger i = new AtomicInteger(0);
            items.forEach( item -> {
                try {
                    String jsonStr = objectMapper.writeValueAsString(item.getCall().getParams());
                    req.addItems(
                        BlockchainOuterClass.NativeCallItem.newBuilder()
                            .setId(i.getAndIncrement())
                            .setMethod(item.getCall().getMethod())
                            .setPayload(ByteString.copyFromUtf8(jsonStr))
                            .build()
                    );
                } catch (JsonProcessingException e) {
                    item.onError(new RpcException(RpcResponseError.CODE_INVALID_METHOD_PARAMS, "Unsupported request params"));
                }
            });
            Iterator<BlockchainOuterClass.NativeCallReplyItem> responses;
            AtomicInteger succeeded = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            try {
                responses = blockingStub.nativeCall(req.build());
                responses.forEachRemaining((resp) -> {
                    int id = resp.getId();
                    Batch.BatchItem item = items.get(id);
                    if (resp.getSucceed()) {
                        try {
                            Object parsed = rpcConverter.fromJson(resp.getPayload().newInput(), item.getCall().getJsonType());
                            succeeded.getAndIncrement();
                            item.onComplete(parsed);
                        } catch (IOException e) {
                            failed.getAndIncrement();
                            item.onError(new RpcException(RpcResponseError.CODE_INVALID_JSON, e.getMessage()));
                        }
                    } else {
                        failed.getAndIncrement();
                        item.onError(new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, resp.getErrorMessage()));
                    }
                });
            } catch (StatusRuntimeException e) {
                if (succeeded.get() == 0) {
                    result.completeExceptionally(e);
                    return;
                }
            }
            result.complete(BatchStatus.newBuilder()
                .withFailed(failed.get())
                .withSucceed(succeeded.get())
                .withTotal(items.size())
                .build());
        });
        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) { }
    }

    public static class Builder {

        private NettyChannelBuilder channelBuilder;
        private SslContextBuilder sslContextBuilder;

        private ObjectMapper objectMapper;
        private RpcConverter rpcConverter;
        private ExecutorService executorService;

        private Chain chain;

        /**
         * Setup for address formatted as host:port
         * @param hostPort
         * @return builder
         */
        public Builder forAddress(String hostPort) {
            String[] parts = hostPort.split(":");
            if (parts.length == 1) {
                return forAddress(hostPort, 9001);
            } else {
                return forAddress(parts[0], Integer.parseInt(parts[1]));
            }
        }

        /**
         *
         * @param host host
         * @param port port
         * @return builder
         */
        public Builder forAddress(String host, int port) {
            channelBuilder = NettyChannelBuilder.forAddress(host, port).usePlaintext();
            return this;
        }

        /**
         *
         * @param uri uri (only host:port are used, could be anything like grpc://dshakle-server:9001)
         * @return builder
         */
        public Builder forUri(URI uri) {
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 9001;
            }
            return forAddress(host, port);
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
        public Builder setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         *
         * @param rpcConverter custom RpcConverter
         * @return builder
         */
        public Builder setRpcConverter(RpcConverter rpcConverter) {
            this.rpcConverter = rpcConverter;
            return this;
        }

        /**
         * By default GrpcTransport uses a fixed thread executor with 2 threads.
         *
         * @param executorService custom execute service
         * @return builder
         */
        public Builder setExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * Setup threads count for default thread executor. Default configuration is 2 threads.
         *
         * @param threads fixed threads count
         * @return builder
         */
        public Builder setThreadsCount(int threads) {
            executorService = Executors.newFixedThreadPool(threads, r -> new Thread(r,"emerald-grpc"));
            return this;
        }

        /**
         *
         * @param chain chain
         * @return builder
         */
        public Builder setChain(Chain chain) {
            this.chain = chain;
            return this;
        }

        /**
         * Validates configuration and builds GrpcTransport
         *
         * @return configured grpc transport
         * @throws SSLException if problem with TLS certificates
         */
        public EmeraldGrpcTransport build() throws SSLException {
            if (sslContextBuilder != null) {
                channelBuilder.useTransportSecurity()
                    .sslContext(sslContextBuilder.build());
            }
            if (executorService == null) {
                setThreadsCount(2);
            }
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter(objectMapper);
            }
            if (chain == null) {
                chain = Chain.UNSPECIFIED;
            }
            Common.ChainRef chainRef = Common.ChainRef.forNumber(chain.getId());
            return new EmeraldGrpcTransport(channelBuilder.build(), objectMapper, rpcConverter, executorService, chainRef);
        }
    }
}
