package org.ethereumclassic.etherjar.rpc.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
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
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    public DefaultRpcTransport(URI host, ObjectMapper objectMapper, ExecutorService executorService) {
        this.host = host;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
    }

    public DefaultRpcTransport(URI host) {
        this.host = host;
        this.objectMapper = createJsonMapper();
        this.executorService = createExecutor();
    }

    public ObjectMapper createJsonMapper() {
        SimpleModule module = new SimpleModule("EtherJar");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(2);
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
        HttpClient httpclient = HttpClients.createDefault();
        String json = toJson(buildCall(method, params));
        RequestBuilder requestBuilder = RequestBuilder.create("POST")
            .setUri(host)
            .addHeader("Content-Type", "application/json")
            .setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
        HttpResponse rcpResponse = httpclient.execute(requestBuilder.build());
        if (rcpResponse.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Server returned error response: " + rcpResponse.getStatusLine().getStatusCode());
        }
        ResponseJson responseJson = objectMapper.readValue(rcpResponse.getEntity().getContent(), ResponseJson.class);
        return (T) responseJson.getResult();
    }

    public RequestJson buildCall(String method, List<Object> params) {
        if (callSequence >= 0x1fffffff) {
            callSequence = 1;
        }
        return new RequestJson(method, params, callSequence++);
    }

    public String toJson(RequestJson request) throws JsonProcessingException {
        return objectMapper.writer().writeValueAsString(request);
    }

    public static class RequestJson {
        private String jsonrpc = "2.0";
        private String method;
        private List<Object> params;
        private int id;

        public RequestJson(String method, List<Object> params, int id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }

        public String getJsonrpc() {
            return jsonrpc;
        }

        public String getMethod() {
            return method;
        }

        public List<Object> getParams() {
            return params;
        }

        public int getId() {
            return id;
        }
    }

    public static class ResponseJson<X> {
        private String jsonrpc = "2.0";
        private int id;
        private X result;

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public X getResult() {
            return result;
        }

        public void setResult(X result) {
            this.result = result;
        }
    }
}
