package org.ethereumclassic.etherjar.rpc.json;

import java.util.List;

/**
 * @author Igor Artamonov
 */
public class RequestJson {

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
