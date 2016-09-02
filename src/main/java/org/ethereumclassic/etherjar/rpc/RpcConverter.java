package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.rpc.json.RequestJson;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Igor Artamonov
 */
public interface RpcConverter {

    <T> T fromJson(InputStream content, Class<T> clazz) throws IOException;

    String toJson(RequestJson request) throws IOException;

}
