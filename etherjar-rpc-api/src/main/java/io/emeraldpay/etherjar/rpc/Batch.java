package io.emeraldpay.etherjar.rpc;

public interface Batch<BI extends BatchItem> {

    public <JS, RES> BI add(RpcCall<JS,RES> call);

}
