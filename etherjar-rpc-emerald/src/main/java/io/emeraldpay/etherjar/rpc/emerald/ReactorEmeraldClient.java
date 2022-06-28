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
import io.emeraldpay.api.proto.BlockchainOuterClass;
import io.emeraldpay.api.proto.Common;
import io.emeraldpay.api.proto.ReactorBlockchainGrpc;
import io.emeraldpay.grpc.Chain;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.emeraldpay.etherjar.rpc.*;
import io.emeraldpay.etherjar.rpc.ResponseJson;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContextBuilder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class ReactorEmeraldClient extends AbstractReactorRpcClient implements ReactorRpcClient {

    private final Channel channel;
    private final ReactorBlockchainGrpc.ReactorBlockchainStub stub;

    private final ObjectMapper objectMapper;
    private final JacksonRpcConverter rpcConverter;
    private final Common.ChainRef chainRef;
    private BlockchainOuterClass.Selector selector;

    ResponseJsonConverter responseJsonConverter = new ResponseJsonConverter();

    public ReactorEmeraldClient(Channel channel, ObjectMapper objectMapper, JacksonRpcConverter rpcConverter, Common.ChainRef chainRef) {
        this.channel = channel;
        this.stub = ReactorBlockchainGrpc.newReactorStub(channel);
        this.objectMapper = objectMapper;
        this.rpcConverter = rpcConverter;
        this.chainRef = chainRef;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Reuse same channel for a new client configured for a different chain.
     * It copies current configuration and shares channel with the new copy.
     *
     * @param chain chain for new calls through this transport
     * @return new instance of ReactorEmeraldClient configured for new chain
     */
    public ReactorEmeraldClient copyForChain(Chain chain) {
        return new ReactorEmeraldClient(channel, objectMapper, rpcConverter, Common.ChainRef.forNumber(chain.getId()));
    }

    /**
     * Reuse same channel for a new client that will select a particular nodes
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
     * ReactorEmeraldClient archiveClient = client.copyWithSelector(selector);
     * </code></pre>
     *
     * @param selector node selector (may be null, to copy without selector)
     *
     * @return new instance of ReactorEmeraldClient configured with new selector
     */
    public ReactorEmeraldClient copyWithSelector(BlockchainOuterClass.Selector selector) {
        ReactorEmeraldClient copy = new ReactorEmeraldClient(channel, objectMapper, rpcConverter, chainRef);
        copy.selector = selector;
        return copy;
    }

    @Override
    public Flux<RpcCallResponse> execute(ReactorBatch batch) {
        BlockchainOuterClass.NativeCallRequest.Builder requestBuilder = BlockchainOuterClass.NativeCallRequest.newBuilder();
        requestBuilder.setChain(chainRef);
        if (selector != null) {
            requestBuilder.setSelector(selector);
        }

        BatchCallContext<ReactorBatch.ReactorBatchItem> context = new BatchCallContext<>();
        Mono<BlockchainOuterClass.NativeCallRequest> request = batch.getItems()
            .doOnNext(context::add)
            .map(this::asNative)
            .reduce(requestBuilder, BlockchainOuterClass.NativeCallRequest.Builder::addItems)
            .map(BlockchainOuterClass.NativeCallRequest.Builder::build);

        Flux<RpcCallResponse> result = stub.nativeCall(request)
            .flatMap((item) -> {
                RpcCall<?, ?> call = null;
                try {
                    call = context.getCall(item.getId());
                } catch (Exception e) {
                    System.err.println("Invalid id returned from upstream: " + item.getId());
                }
                return read(item, call);
            })
            .onErrorResume(StatusRuntimeException.class, (e) -> {
              if (e.getStatus().getCode() == Status.Code.CANCELLED) {
                  return Mono.empty();
              }
              return Mono.error(new RpcException(
                  RpcResponseError.CODE_UPSTREAM_CONNECTION_ERROR,
                  "gRPC connection error. Status: " + e.getStatus(),
                  null,
                  e
              ));
            })
            .map(responseJsonConverter.forContext(context));


        FailedBatchProcessor failedBatchProcessor = this.getFailedBatchProcessor();
        if (failedBatchProcessor != null) {
            Function<RpcException, Publisher<RpcCallResponse>> fallback = failedBatchProcessor.createFallback(batch);
            if (fallback != null) {
                result = result.onErrorResume(RpcException.class, fallback);
            }
        }

        result = result.doOnError((t) -> System.err.println("Client error " + t.getClass() + ": " + t.getMessage()));

        result = postProcess(batch, context, result);

        return result;
    }

    public <JS, RES> Mono<ResponseJson<JS, Integer>> read(BlockchainOuterClass.NativeCallReplyItem item, RpcCall<JS, RES> call) {
        ResponseJson<JS, Integer> result = new ResponseJson<>();
        if (call != null) {
            result.setId(item.getId());
        }
        if (!item.getSucceed()) {
            result.setError(new RpcResponseError(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, item.getErrorMessage()));
        } else if (call == null) {
            result.setError(new RpcResponseError(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Unknown id returned from upstream"));
        } else {
            try {
                JS value = rpcConverter.fromJsonResult(item.getPayload().newInput(), call.getJsonType());
                result.setResult(value);
            } catch (RpcException e) {
                return Mono.error(e);
            }
        }
        return Mono.just(result);
    }

    public BlockchainOuterClass.NativeCallItem asNative(ReactorBatch.ReactorBatchItem<?, ?> item) {
        try {
            return BlockchainOuterClass.NativeCallItem.newBuilder()
                .setId(item.getId())
                .setMethod(item.getCall().getMethod())
                .setPayload(
                    ByteString.copyFromUtf8(objectMapper.writeValueAsString(item.getCall().getParams()))
                ).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        private NettyChannelBuilder channelBuilder;
        private SslContextBuilder sslContextBuilder;
        private Channel channel;

        private ObjectMapper objectMapper;
        private JacksonRpcConverter rpcConverter;

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

        protected SslContextBuilder startSslContextBuilder() {
            // use a GRPC based SSL Context, which enables ALPN and HTTP2,
            // otherwise GrpcSslContexts.ensureAlpnAndH2Enabled may throw "ALPN must be enabled and list HTTP/2 as a supported protocol" if misconfigured
            return GrpcSslContexts.forClient();
        }

        /**
         * Setup x509 certificate for target server
         *
         * @param certificate x509 certificate
         * @return builder
         */
        public Builder trustedCertificate(InputStream certificate) {
            if (sslContextBuilder == null) {
                sslContextBuilder = startSslContextBuilder();
                channelBuilder = channelBuilder.useTransportSecurity();
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
                sslContextBuilder = startSslContextBuilder();
                channelBuilder = channelBuilder.useTransportSecurity();
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
                sslContextBuilder = startSslContextBuilder();
                channelBuilder = channelBuilder.useTransportSecurity();
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
                sslContextBuilder = startSslContextBuilder();
                channelBuilder = channelBuilder.useTransportSecurity();
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
         * @param rpcConverter custom RpcConverter
         * @return builder
         */
        public Builder rpcConverter(JacksonRpcConverter rpcConverter) {
            this.rpcConverter = rpcConverter;
            return this;
        }

        public Builder withSslContextBuilder(Function<SslContextBuilder, SslContextBuilder> changes) {
            this.sslContextBuilder = changes.apply(this.sslContextBuilder);
            return this;
        }

        public Builder executor(Executor executor) {
            channelBuilder.executor(executor);
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
         * Validates configuration and builds client
         *
         * @return configured grpc transport
         * @throws SSLException if problem with TLS certificates
         */
        public ReactorEmeraldClient build() throws SSLException {
            if (channel == null) {
                if (sslContextBuilder != null) {
                    channelBuilder.useTransportSecurity()
                        .sslContext(sslContextBuilder.build());
                }
                channel = channelBuilder.build();
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
            return new ReactorEmeraldClient(channel, objectMapper, rpcConverter, chainRef);
        }
    }

}
