package org.ethereumclassic.etherjar.rpc.transport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.ethereumclassic.etherjar.rpc.JacksonRpcConverter;
import org.ethereumclassic.etherjar.rpc.RpcConverter;
import org.ethereumclassic.etherjar.rpc.json.RequestJson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * @author Igor Artamonov
 */
public class DefaultRpcTransport implements RpcTransport {

    private static final Logger log = Logger.getLogger(DefaultRpcTransport.class.getName());

    private int callSequence = 1;

    private URI host;
    private ExecutorService executorService;
    private RpcConverter rpcConverter;

    private HttpClient httpclient;

    public DefaultRpcTransport(URI host, RpcConverter rpcConverter, ExecutorService executorService, HttpClient httpClient) {
        this.host = host;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
        httpclient = httpClient;
    }

    public DefaultRpcTransport(URI host, RpcConverter rpcConverter, ExecutorService executorService) {
        this.host = host;
        this.rpcConverter = rpcConverter;
        this.executorService = executorService;
        httpclient = HttpClients.createDefault();
    }

    public DefaultRpcTransport(URI host) {
        this.host = host;
        this.rpcConverter = createRpcConverter();
        this.executorService = createExecutor();
        httpclient = HttpClients.createDefault();
    }

    private RpcConverter createRpcConverter() {
        return new JacksonRpcConverter();
    }

    public ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }

    @Override
    public <T> Future<T> execute(final String method, final List params, final Class<T> resultType) throws IOException {
        return executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return executeSync(method, params, resultType);
            }
        });
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

    public RequestJson buildCall(String method, List<Object> params) {
        if (callSequence >= 0x1fffffff) {
            callSequence = 1;
        }
        return new RequestJson(method, params, callSequence++);
    }
}
