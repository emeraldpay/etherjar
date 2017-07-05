package io.infinitape.etherjar.rpc.transport;

import io.infinitape.etherjar.rpc.JacksonRpcConverter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import io.infinitape.etherjar.rpc.RpcConverter;
import io.infinitape.etherjar.rpc.json.RequestJson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * @author Igor Artamonov
 */
public class DefaultRpcTransport implements RpcTransport {

    private static final Logger log = Logger.getLogger(DefaultRpcTransport.class.getName());
    private static final int MAX_CONNECTIONS = 50;

    private int callSequence = 1;

    private URI host;
    private ExecutorService executorService;
    private RpcConverter rpcConverter;

    private HttpClient httpclient;

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
        }
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
    public <T> CompletableFuture<T> execute(final String method, final List params, final Class<T> resultType) {
        CompletableFuture<T> f = new CompletableFuture<T>();
        executorService.submit(() -> {
            try {
                f.complete(executeSync(method, params, resultType));
            } catch (IOException e) {
                f.completeExceptionally(e);
            }
        });
        return f;
    }

    public <T> T executeSync(String method, List params, Class<T> resultType) throws IOException {
        String json = rpcConverter.toJson(buildCall(method, params));
        RequestBuilder requestBuilder = RequestBuilder.create("POST")
            .setUri(host)
            .addHeader("Content-Type", "application/json")
            .setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
        HttpResponse rcpResponse = httpclient.execute(requestBuilder.build());
        if (rcpResponse.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Server returned error response: " + rcpResponse.getStatusLine().getStatusCode());
        }
        InputStream content = rcpResponse.getEntity().getContent();
        return rpcConverter.fromJson(content, resultType);
    }

    public RequestJson buildCall(String method, List params) {
        if (callSequence >= 0x1fffffff) {
            callSequence = 1;
        }
        return new RequestJson(method, params, callSequence++);
    }
}
