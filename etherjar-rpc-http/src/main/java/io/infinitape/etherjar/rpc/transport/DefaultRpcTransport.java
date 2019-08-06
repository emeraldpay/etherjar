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

package io.infinitape.etherjar.rpc.transport;

import io.infinitape.etherjar.rpc.Batch;
import io.infinitape.etherjar.rpc.JacksonRpcConverter;
import io.infinitape.etherjar.rpc.RpcConverter;
import io.infinitape.etherjar.rpc.RpcException;
import io.infinitape.etherjar.rpc.json.RequestJson;
import io.infinitape.etherjar.rpc.json.ResponseJson;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultRpcTransport implements RpcTransport {

    private static final Logger log = Logger.getLogger(DefaultRpcTransport.class.getName());
    private static final int MAX_CONNECTIONS = 50;

    private int callSequence = 1;

    private URI host;
    private ExecutorService executorService;
    private RpcConverter rpcConverter;

    private HttpClient httpclient;
    private HttpClientContext context;

    public DefaultRpcTransport(URI host, RpcConverter rpcConverter, ExecutorService executorService, HttpClient httpClient) {
        this.host = host;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
        this.httpclient = httpClient;
        if (this.httpclient == null) {
            this.httpclient = HttpClients.custom()
                .setConnectionManager(createConnectionManager())
                .setConnectionManagerShared(true)
                .build();
            this.context = null;
        }
    }

    public void setBasicAuth(String username, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        AuthCache cache = new BasicAuthCache();
        cache.put(
            new HttpHost(host.getHost(), host.getPort(), host.getScheme()),
            new BasicScheme()
        );

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(provider);
        context.setAuthCache(cache);
        this.context = context;
    }

    public DefaultRpcTransport(URI host, RpcConverter rpcConverter, ExecutorService executorService) {
        this(host, rpcConverter, executorService, null);
    }

    public DefaultRpcTransport(URI host) {
        this(host, new JacksonRpcConverter(), Executors.newCachedThreadPool());
    }

    protected HttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_CONNECTIONS);
        return cm;
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }

    @Override
    public CompletableFuture<BatchStatus> execute(List<Batch.BatchItem<?, ?>> items) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(
                BatchStatus.newBuilder()
                    .withFailed(0)
                    .withSucceed(0)
                    .withTotal(0)
                    .build()
            );
        }
        if (callSequence >= 0x1fffffff) {
            callSequence = 1;
        }
        Map<Integer, Batch.BatchItem> resultMapper = new HashMap<>();
        Map<Integer, Class> jsonTypes = new HashMap<>();
        List<RequestJson<Integer>> rpcRequests = items.stream()
                .map(item -> {
                    int current = callSequence++;
                    resultMapper.put(current, item);
                    jsonTypes.put(current, item.getCall().getJsonType());
                    return item.getCall().toJson(current);
                })
                .collect(Collectors.toList());
        CompletableFuture<BatchStatus> f = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                String json = rpcConverter.toJson(rpcRequests);
                RequestBuilder requestBuilder = RequestBuilder.create("POST")
                        .setUri(host)
                        .addHeader("Content-Type", "application/json")
                        .setEntity(new ByteArrayEntity(json.getBytes(StandardCharsets.UTF_8)));
                HttpResponse rcpResponse = httpclient.execute(requestBuilder.build(), context);
                int statusCode = rcpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Server returned error response: " + statusCode);
                }
                InputStream content = rcpResponse.getEntity().getContent();
                BatchStatus status = processResult(content, items, resultMapper, jsonTypes);
                f.complete(status);
            } catch (Throwable e) {
                RpcException rpcError;
                if (e instanceof RpcException) {
                    rpcError = (RpcException) e;
                } else {
                    rpcError = new RpcException(-32603, e.getMessage(), null, e);
                }
                processError(rpcError, items);
                f.completeExceptionally(rpcError);
            }
        });
        return f;
    }

    /**
     * Called when batch call failed
     *
     * @param t Exception caused to fail execution
     * @param batch batch items
     */
    public void processError(RpcException t, List<Batch.BatchItem<?, ?>> batch) {
        batch.forEach((item) -> {
            item.onError(t);
        });
    }

    /**
     * Called when batch succeeded to execute
     *
     * @param content output from upstream
     * @param batch batch items
     * @param resultMapper mapping from response ids to original ids
     * @param jsonTypes data types for batch items
     *
     * @return status of batch execution
     * @throws IOException when failed to read from input stream
     */
    public BatchStatus processResult(InputStream content,
                                     List<Batch.BatchItem<?, ?>> batch,
                                     Map<Integer, Batch.BatchItem> resultMapper,
                                     Map<Integer, Class> jsonTypes) throws IOException {
        List<ResponseJson<?, Integer>> response = rpcConverter.parseBatch(content, jsonTypes);
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger succeed = new AtomicInteger();
        response.forEach(item -> {
            Batch.BatchItem bi = resultMapper.get(item.getId());
            if (bi == null) {
                return;
            }
            if (item.getError() != null) {
                failed.getAndIncrement();
                bi.onError(item.getError().asException());
            } else {
                succeed.getAndIncrement();
                bi.onComplete(item.getResult());
            }
        });
        return BatchStatus.newBuilder()
                .withTotal(batch.size())
                .withSucceed(succeed.get())
                .withFailed(failed.get())
                .build();
    }
}
