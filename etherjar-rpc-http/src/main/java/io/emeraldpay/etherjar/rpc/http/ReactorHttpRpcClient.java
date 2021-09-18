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
package io.emeraldpay.etherjar.rpc.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.emeraldpay.etherjar.rpc.*;
import io.emeraldpay.etherjar.rpc.ResponseJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

public class ReactorHttpRpcClient extends AbstractReactorRpcClient implements ReactorRpcClient {

    private final ReactorRpcTransport transport;

    private ReactorHttpRpcClient(ReactorRpcTransport transport) {
        this.transport = transport;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Flux<RpcCallResponse> execute(ReactorBatch batch) {
        BatchCallContext<ReactorBatch.ReactorBatchItem> context = new BatchCallContext<>();

        Flux<RpcCallResponse> result = batch.getItems()
            .doOnNext(context::add)
            .thenMany(transport.execute(batch.getItems(), context))
            .onErrorResume(ConnectException.class, ReactorHandlers.catchConnectException())
            ;


        FailedBatchProcessor failedBatchProcessor = this.getFailedBatchProcessor();
        if (failedBatchProcessor != null) {
            Function<RpcException, Publisher<RpcCallResponse>> fallback = failedBatchProcessor.createFallback(batch);
            if (fallback != null) {
                result = result.onErrorResume(RpcException.class, fallback);
            }
        }

        result = postProcess(batch, context, result);

        result = result.doOnError((t) -> System.err.println("HTTP Error " + t.getClass() + ": " + t.getMessage()));

        return result;
    }


    public static class ResponseReader implements Function<ByteBuf, Flux<RpcCallResponse>> {
        private RpcConverter rpcConverter;
        private final BatchCallContext<ReactorBatch.ReactorBatchItem> context;
        private ResponseJsonConverter responseJsonConverter = new ResponseJsonConverter();

        public ResponseReader(RpcConverter rpcConverter, BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
            this.rpcConverter = rpcConverter;
            this.context = context;
        }

        @Override
        public Flux<RpcCallResponse> apply(ByteBuf content) {
            List<ResponseJson<Object, Integer>> responses;
            try {
                responses = rpcConverter.parseBatch(new ByteBufInputStream(content), context.getJsonTypes());
            } catch (RpcException e) {
                return Flux.error(e);
            }
            return Flux
                .fromIterable(responses)
                .map(responseJsonConverter.forContext(context));
        }
    }

    public static class Builder {
        private RpcConverter rpcConverter;
        private Mono<String> target;
        private Consumer<HttpHeaders> headers;
        private Consumer<SslProvider.SslContextSpec> sslProviderBuilder;
        private TransportType transportType;

        private enum TransportType {
            BATCH, SEPARATED
        }

        public Builder connectTo(String url) {
            target = Mono.just(url);
            return this;
        }

        public Builder connectTo(Mono<String> url) {
            target = url;
            return this;
        }

        public Builder connectTo(URI url) {
            target = Mono.just(url.toString());
            return this;
        }

        public Builder rpcConverter(RpcConverter rpcConverter) {
            this.rpcConverter = rpcConverter;
            return this;
        }

        /**
         * Setup Basic Auth for RPC calls
         *
         * @param username username
         * @param password password
         * @return builder
         */
        public Builder basicAuth(String username, String password) {
            String authString = username + ":" + password;
            String authBase64 = Base64.getEncoder().encodeToString(authString.getBytes());
            final String auth = "Basic " + authBase64;
            this.headers = (h) -> {
                h.add(HttpHeaderNames.AUTHORIZATION, auth);
            };
            return this;
        }

        public Builder alwaysBatch() {
            transportType = TransportType.BATCH;
            return this;
        }

        public Builder alwaysSeparate() {
            transportType = TransportType.SEPARATED;
            return this;
        }

        /**
         * Provide a trusted x509 certificate expected from RPC server
         *
         * @param certificate input stream to certificate in DER format (binary or base64)
         * @throws GeneralSecurityException if there is a problem with the certificate
         * @throws IOException if unable to read certificate
         * @return builder
         */
        public Builder trustedCertificate(InputStream certificate) throws GeneralSecurityException, IOException {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certificate);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, "".toCharArray());
            ks.setCertificateEntry("server", cert);
            SslContext sslContext = SslContextBuilder.forClient().trustManager(cert).build();

            this.sslProviderBuilder = (spec) -> {
                spec.sslContext(sslContext);
            };

            return this;
        }

        public ReactorHttpRpcClient build() {
            if (transportType == null) {
                transportType = TransportType.BATCH;
            }
            if (target == null) {
                target = Mono.just("http://127.0.0.1:8545");
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter();
            }
            HttpClient clientBuilder = HttpClient.create();
            if (headers != null) {
                clientBuilder = clientBuilder.headers(headers);
            }
            clientBuilder = clientBuilder.headers((h) -> {
                h.add(HttpHeaderNames.CONTENT_TYPE, "application/json");
            });
            if (sslProviderBuilder != null) {
                clientBuilder = clientBuilder.secure(sslProviderBuilder);
            }
            ReactorRpcTransport transport;
            if (transportType == TransportType.BATCH) {
                BatchToString batchToString = new BatchToString(rpcConverter);
                transport = new BatchTransport(clientBuilder, target, rpcConverter, batchToString);
            } else if (transportType == TransportType.SEPARATED) {
                transport = new SeparatedTransport(clientBuilder, target, rpcConverter);
            } else {
                throw new IllegalStateException("Transport type cannot be null");
            }
            return new ReactorHttpRpcClient(transport);
        }

    }
}
