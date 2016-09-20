package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.rpc.json.TraceItemJson;

import java.util.List;

/**
 * Workaround to operate List of TraceItemJson (a Class passed to RpcConverter, type is lost in runtime, need a way
 * how to distinguish this List from other classes)
 *
 * @author Igor Artamonov
 */
public interface TraceList extends List<TraceItemJson> {

}
