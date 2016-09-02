package org.ethereumclassic.etherjar.rpc.json;

/**
 * @author Igor Artamonov
 */
public class ResponseJson<X> {

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
