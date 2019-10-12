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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.emeraldpay.api.proto.BlockchainGrpc;
import io.emeraldpay.api.proto.BlockchainOuterClass;
import io.emeraldpay.api.proto.Common;
import io.emeraldpay.grpc.Chain;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.transport.RpcTransport;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
public class EmeraldTransport implements RpcTransport {

    private final Channel channel;
    private final BlockchainGrpc.BlockchainBlockingStub blockingStub;

    private ObjectMapper objectMapper;
    private RpcConverter rpcConverter;
    private ExecutorService executorService;
    private Common.ChainRef chainRef;
    private BlockchainOuterClass.Selector selector;

    public EmeraldTransport(Channel channel,
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
        return new EmeraldTransport(channel, objectMapper, rpcConverter, executorService, Common.ChainRef.forNumber(chain.getId()));
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
    public EmeraldTransport copyWithSelector(BlockchainOuterClass.Selector selector) {
        EmeraldTransport copy = new EmeraldTransport(channel, objectMapper, rpcConverter, executorService, chainRef);
        copy.selector = selector;
        return copy;
    }

    @Override
    public CompletableFuture<Iterable<RpcResponse>> execute(List<RpcTransport.RpcRequest> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<Iterable<RpcResponse>> f = new CompletableFuture<>();
        executorService.execute(() -> {
            final BlockchainOuterClass.NativeCallRequest.Builder req = BlockchainOuterClass.NativeCallRequest.newBuilder();
            req.setChain(chainRef);
            if (selector != null) {
                req.setSelector(selector);
            }
            final AtomicInteger i = new AtomicInteger(0); // because for lambda, must be final
            final Map<Integer, RpcRequest> requestWithId = new HashMap<>(items.size());
            items.forEach( item -> {
                int id = i.getAndIncrement();
                String json = rpcConverter.toJson(item.getPayload());
                req.addItems(
                    BlockchainOuterClass.NativeCallItem.newBuilder()
                        .setId(id)
                        .setMethod(item.getMethod())
                        .setPayload(ByteString.copyFromUtf8(json))
                        .build()
                );
                requestWithId.put(id, item);
            });
            List<RpcTransport.RpcResponse> result = new ArrayList<>();
            Iterator<BlockchainOuterClass.NativeCallReplyItem> responses;
            try {
                responses = blockingStub.nativeCall(req.build());
                responses.forEachRemaining((resp) -> {
                    int id = resp.getId();
                    RpcRequest request = requestWithId.get(id);
                    result.add(process(request, resp));
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

    protected <T> RpcResponse<T> process(RpcRequest<T> request, BlockchainOuterClass.NativeCallReplyItem resp) {
        if (resp.getSucceed()) {
            try {
                return request.asResponse(read(resp.getPayload(), request));
            } catch (RpcException e) {
                return request.asError(e);
            }
        } else {
            return request.asError(new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, resp.getErrorMessage()));
        }
    }

    protected <T> T read(ByteString bytes, RpcRequest<T> request) throws RpcException {
        return rpcConverter.fromJson(bytes.newInput(), request.getType());
    }

    @Override
    public void close() throws IOException {
    }

    public static class Builder {

        private NettyChannelBuilder channelBuilder;
        private SslContextBuilder sslContextBuilder;
        private Channel channel;

        private ObjectMapper objectMapper;
        private RpcConverter rpcConverter;
        private ExecutorService executorService;

        private Chain chain;

        /**
         * Setup for an existing channel
         *
         * @param channel existing channel
         * @return builder
         */
        public Builder forChannel(Channel channel) {
            this.channel = channel;
            channelBuilder = null;
            sslContextBuilder = null;
            return this;
        }

        /**
         * Setup for address formatted as host:port
         *
         * @param hostPort address in host:port format
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
            channel = null;
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
        public EmeraldTransport build() throws SSLException {
            if (channel == null) {
                if (sslContextBuilder != null) {
                    channelBuilder.useTransportSecurity()
                        .sslContext(sslContextBuilder.build());
                }
                channel = channelBuilder.build();
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
            return new EmeraldTransport(channel, objectMapper, rpcConverter, executorService, chainRef);
        }
    }
}
