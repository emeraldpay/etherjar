package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.rpc.json.RequestJson;

import java.io.IOException;
import java.io.InputStream;

public interface RpcConverter {

    <T> T fromJson(InputStream content, Class<T> clazz) throws IOException;

    String toJson(RequestJson request) throws IOException;

}
