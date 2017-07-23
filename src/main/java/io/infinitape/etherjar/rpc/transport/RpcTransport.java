package io.infinitape.etherjar.rpc.transport;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RpcTransport extends Closeable {

    <T> CompletableFuture<T> execute(String method, List params, Class<T> resultType);
}
