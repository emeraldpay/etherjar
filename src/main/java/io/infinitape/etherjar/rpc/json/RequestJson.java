package io.infinitape.etherjar.rpc.json;

import java.util.List;

public class RequestJson {

    private String jsonrpc = "2.0";
    private String method;
    private List params;
    private int id;

    public RequestJson(String method, List params, int id) {
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

    public List getParams() {
        return params;
    }

    public int getId() {
        return id;
    }
}
