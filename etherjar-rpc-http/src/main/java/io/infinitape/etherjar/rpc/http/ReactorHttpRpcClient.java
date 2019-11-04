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
package io.infinitape.etherjar.rpc.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.infinitape.etherjar.rpc.json.ResponseJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactorHttpRpcClient extends AbstractReactorRpcClient implements ReactorRpcClient {

    private RpcConverter rpcConverter;
    private Mono<String> target;
    private HttpClient httpClient;

    private Function<ReactorBatch.ReactorBatchItem, RequestJson> toRequest =
        (bi) -> new RequestJson<>(bi.getCall().getMethod(), bi.getCall().getParams(), bi.getId());

    private Function<Tuple2<String, Boolean>, Publisher<String>> arrange = (x) -> {
        boolean first = x.getT2();
        if (first) {
            return Mono.just(x.getT1());
        } else {
            return Flux.just(",", x.getT1());
        }
    };

    public ReactorHttpRpcClient(RpcConverter rpcConverter,
                                 Mono<String> target,
                                HttpClient httpClient) {
        this.rpcConverter = rpcConverter;
        this.target = target;
        this.httpClient = httpClient;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    protected Tuple2<Flux<ByteBuf>, BatchCallContext<ReactorBatch.ReactorBatchItem>> convertToJson(ReactorBatch batch) {
        BatchCallContext<ReactorBatch.ReactorBatchItem> context = new BatchCallContext<>();
        Flux<String> items = Flux.from(batch.getItems())
            .doOnNext(context::add)
            .map(toRequest)
            .map(rpcConverter::toJson)
            .zipWith(Flux.range(0, Integer.MAX_VALUE).map(i -> i == 0))
            .flatMap(arrange);
        Flux<ByteBuf> bytes = Flux.concat(Flux.just("["), items, Flux.just("]"))
            .map(String::getBytes)
            .map(Unpooled::wrappedBuffer);
        return Tuples.of(bytes, context);
    }

    @Override
    public Flux<RpcCallResponse> execute(ReactorBatch batch) {
        Tuple2<Flux<ByteBuf>, BatchCallContext<ReactorBatch.ReactorBatchItem>> converted = convertToJson(batch);
        BatchCallContext<ReactorBatch.ReactorBatchItem> context = converted.getT2();
        HttpClient.ResponseReceiver<?> response =
            httpClient
                .post()
                .uri(target)
                .send(converted.getT1());
        Flux<RpcCallResponse> result = response.response((resp, data) -> {
            if (resp.status() == HttpResponseStatus.OK) {
                return data.aggregate().flatMapMany(new ResponseReader(context));
            } else {
                RpcException err = new RpcException(RpcResponseError.CODE_UPSTREAM_INVALID_RESPONSE, "Upstream connection error. Status: " + resp.status().code());
                return Flux.error(err);
            }
        });

        FailedBatchProcessor failedBatchProcessor = this.getFailedBatchProcessor();
        if (failedBatchProcessor != null) {
            Function<RpcException, Publisher<RpcCallResponse>> fallback = failedBatchProcessor.createFallback(batch);
            if (fallback != null) {
                result = result.onErrorResume(RpcException.class, fallback);
            }
        }

        result = result.share();

        batch.withExecution(Flux.from(result));

        return result;
    }


    public class ResponseReader implements Function<ByteBuf, Flux<RpcCallResponse>> {
        private final BatchCallContext<ReactorBatch.ReactorBatchItem> context;

        public ResponseReader(BatchCallContext<ReactorBatch.ReactorBatchItem> context) {
            this.context = context;
        }

        @Override
        public Flux<RpcCallResponse> apply(ByteBuf content) {
            List<ResponseJson<?, Integer>> responses;
            try {
                responses = rpcConverter.parseBatch(new ByteBufInputStream(content), context.getJsonTypes());
            } catch (IOException e) {
                return Flux.error(e);
            }
            return Flux.fromIterable(responses)
                .flatMap(new AbstractReactorRpcClient.ResponseTransformer(context));
        }
    }

    public static class Builder {
        private RpcConverter rpcConverter;
        private Mono<String> target;
        private Consumer<HttpHeaders> headers;
        private Consumer<SslProvider.SslContextSpec> sslProviderBuilder;

        public Builder setTarget(String url) {
            target = Mono.just(url);
            return this;
        }

        public Builder setTarget(Mono<String> url) {
            target = url;
            return this;
        }

        public Builder setTarget(URI url) {
            target = Mono.just(url.toString());
            return this;
        }

        public Builder setRpcConverter(RpcConverter rpcConverter) {
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
        public Builder setBasicAuth(String username, String password) {
            String authString = username + ":" + password;
            String authBase64 = Base64.getEncoder().encodeToString(authString.getBytes());
            final String auth = "Basic " + authBase64;
            this.headers = (h) -> {
                h.add(HttpHeaderNames.AUTHORIZATION, auth);
            };
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
        public Builder setTrustedCertificate(InputStream certificate) throws GeneralSecurityException, IOException {
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
            return new ReactorHttpRpcClient(rpcConverter, target, clientBuilder);
        }

    }
}
