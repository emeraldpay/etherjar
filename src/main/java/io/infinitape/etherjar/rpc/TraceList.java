package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.rpc.json.TraceItemJson;

import java.util.List;

/**
 * Workaround to operate List of TraceItemJson (a Class passed to RpcConverter, type is lost in runtime, need a way
 * how to distinguish this List from other classes)
 */
public interface TraceList extends List<TraceItemJson> {

}
