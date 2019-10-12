/*
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

package io.infinitape.etherjar.rpc.http;

import io.infinitape.etherjar.rpc.*;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.infinitape.etherjar.rpc.json.ResponseJson;
import io.infinitape.etherjar.rpc.transport.RpcTransport;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HttpRpcTransport implements RpcTransport {

    private static final Logger log = Logger.getLogger(HttpRpcTransport.class.getName());

    private final URI target;
    private final ExecutorService executorService;
    private final RpcConverter rpcConverter;

    private final HttpClient httpclient;
    private final HttpClientContext context;
    private final Runnable onClose;

    private HttpRpcTransport(URI target, RpcConverter rpcConverter, ExecutorService executorService, HttpClient httpClient, HttpClientContext context, Runnable onClose) {
        this.target = target;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
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
    public CompletableFuture<Iterable<RpcResponse>> execute(List<RpcRequest> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(
                Collections.emptyList()
            );
        }
        Map<Integer, RpcRequest> requests = new HashMap<>(items.size());
        Map<Integer, Class> responseMapping = new HashMap<>(items.size());
        List<RequestJson<Integer>> rpcRequests = items.stream()
                .map(item -> {
                    Integer id = (Integer) item.getPayload().getId();
                    requests.put(id, item);
                    responseMapping.put(id, item.getType());
                    return (RequestJson<Integer>)item.getPayload();
                }).collect(Collectors.toList());
        CompletableFuture<Iterable<RpcResponse>> f = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                String json = rpcConverter.toJson(rpcRequests);
                RequestBuilder requestBuilder = RequestBuilder.create("POST")
                        .setUri(target)
                        .addHeader("Content-Type", "application/json")
                        .setEntity(new ByteArrayEntity(json.getBytes(StandardCharsets.UTF_8)));
                HttpResponse rcpResponse = httpclient.execute(requestBuilder.build(), this.context);
                int statusCode = rcpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Server returned error response: " + statusCode);
                }
                InputStream content = rcpResponse.getEntity().getContent();
                List<ResponseJson<?, Integer>> response = rpcConverter.parseBatch(content, responseMapping);
                List<RpcResponse> result = response.stream().map((resp) ->
                    requests.get(resp.getId()).asResponse(resp)
                ).collect(Collectors.toList());
                f.complete(result);
            } catch (Throwable e) {
                RpcException rpcError;
                if (e instanceof RpcException) {
                    rpcError = (RpcException) e;
                } else {
                    rpcError = new RpcException(RpcResponseError.CODE_INTERNAL_ERROR, e.getMessage(), null, e);
                }
                f.complete(processError(rpcError, items));
            }
        });
        return f;
    }

    /**
     * Called when batch call failed, marks all bath calls as failed
     *
     * @param t Exception caused to fail execution
     * @param batch batch items
     * @return list of responses to original batch, all marked as errored
     */
    public List<RpcResponse> processError(RpcException t, List<RpcRequest> batch) {
        return batch.stream().map((item) ->
            item.asError(t)
        ).collect(Collectors.toList());
    }


    public static class Builder {
        private URI target;
        private ExecutorService executorService;
        private RpcConverter rpcConverter;

        private HttpClientContext context;
        private SSLContext sslContext;
        private HttpClient httpClient;

        private Runnable onClose;

        private int maxConnections = 50;

        /**
         * Setup Basic Auth for RPC calls
         *
         * @param username username
         * @param password password
         * @return builder
         */
        public Builder setBasicAuth(String username, String password) {
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
        public Builder setTrustedCertificate(InputStream certificate) throws GeneralSecurityException, IOException {
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

        public Builder setTarget(String url) throws URISyntaxException {
            return this.setTarget(new URI(url));
        }

        public Builder setTarget(URI target) {
            this.httpClient = null;
            this.target = target;
            return this;
        }

        public Builder setExecutor(ExecutorService executorService) {
            this.executorService = executorService;
            if (this.onClose != null) {
                this.onClose.run();
            }
            this.onClose = null;
            return this;
        }

        public Builder setHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        protected void setDefaults() {
            if (httpClient == null && target == null) {
                try {
                    setTarget("http://127.0.0.1:8545");
                } catch (URISyntaxException e) { }
            }
            if (executorService == null) {
                ExecutorService executorService = Executors.newCachedThreadPool();
                setExecutor(executorService);
                onClose = executorService::shutdown;
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter();
            }
        }

        public HttpRpcTransport build() {
            setDefaults();

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

            return new HttpRpcTransport(target, rpcConverter, executorService, httpClient, context, onClose);
        }
    }

}
