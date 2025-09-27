/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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

import io.emeraldpay.etherjar.rpc.*;
import io.emeraldpay.etherjar.rpc.ResponseJson;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@NullMarked
public class HttpRpcTransport extends AbstractRpcTransport {

    private static final Logger log = Logger.getLogger(HttpRpcTransport.class.getName());


    private final URI target;

    private final HttpClient httpclient;
    @Nullable
    private final HttpClientContext context;
    @Nullable
    private final Runnable onClose;

    private HttpRpcTransport(URI target, RpcConverter rpcConverter, ExecutorService executorService, HttpClient httpClient, @Nullable HttpClientContext context, @Nullable Runnable onClose) {
        super(executorService, rpcConverter);
        this.target = target;
        this.httpclient = httpClient;
        this.context = context;
        this.onClose = onClose;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void close() throws IOException {
        if (onClose != null) {
            this.onClose.run();
        }
    }

    @Override
    public InputStream execute(String json) throws IOException {
        RequestBuilder requestBuilder = RequestBuilder.create("POST")
                .setUri(target)
                .addHeader("Content-Type", "application/json")
                .setEntity(new ByteArrayEntity(json.getBytes(StandardCharsets.UTF_8)));
        HttpResponse rcpResponse = httpclient.execute(requestBuilder.build(), this.context);
        int statusCode = rcpResponse.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException("Server returned error response: " + statusCode);
        }
        return rcpResponse.getEntity().getContent();
    }

    public static class Builder {
        @Nullable
        private URI target;
        @Nullable
        private ExecutorService executorService;
        @Nullable
        private RpcConverter rpcConverter;

        @Nullable
        private HttpClientContext context;
        @Nullable
        private SSLContext sslContext;
        @Nullable
        private HttpClient httpClient;

        @Nullable
        private Runnable onClose;

        private int maxConnections = 50;

        /**
         * Setup Basic Auth for RPC calls
         *
         * @param username username
         * @param password password
         * @return builder
         */
        public Builder basicAuth(String username, String password) {
            this.httpClient = null;

            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            AuthCache cache = new BasicAuthCache();
            cache.put(
                new HttpHost(target.getHost(), target.getPort(), target.getScheme()),
                new BasicScheme()
            );

            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(provider);
            context.setAuthCache(cache);
            this.context = context;

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
            this.httpClient = null;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certificate);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, "".toCharArray());
            ks.setCertificateEntry("server", cert);
            this.sslContext = SSLContexts.custom()
                .loadTrustMaterial(ks, (TrustStrategy) (chain, authType) -> Arrays.asList(chain).contains(cert))
                .build();

            return this;
        }

        public Builder connectTo(String url) throws URISyntaxException {
            return this.connectTo(new URI(url));
        }

        public Builder connectTo(URI target) {
            this.httpClient = null;
            this.target = target;
            return this;
        }

        public Builder executor(ExecutorService executorService) {
            this.executorService = executorService;
            if (this.onClose != null) {
                this.onClose.run();
            }
            this.onClose = null;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        protected void initDefaults() {
            if (target == null) {
                try {
                    target = new URI("http://127.0.0.1:8545");
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            if (executorService == null) {
                ExecutorService executorService = Executors.newCachedThreadPool();
                executor(executorService);
                onClose = executorService::shutdown;
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter();
            }
        }

        public HttpRpcTransport build() {
            initDefaults();

            if (this.httpClient == null) {
                httpClient = HttpClients.custom()
                    .setMaxConnTotal(maxConnections)
                    .setConnectionManagerShared(true)
                    .setSSLContext(sslContext)
                    .setDefaultSocketConfig(
                        SocketConfig.custom()
                            .setSoTimeout(1000)
                            .setSoReuseAddress(true)
                            .setSoKeepAlive(true)
                            .build()
                    )
                    .build();
            }
            Objects.requireNonNull(target);
            Objects.requireNonNull(executorService);
            Objects.requireNonNull(rpcConverter);
            Objects.requireNonNull(httpClient);

            return new HttpRpcTransport(target, rpcConverter, executorService, httpClient, context, onClose);
        }
    }

}
