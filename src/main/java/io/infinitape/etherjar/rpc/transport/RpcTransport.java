package io.infinitape.etherjar.rpc.transport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Igor Artamonov
 */
public interface RpcTransport {

    <T> CompletableFuture<T> execute(String method, List params, Class<T> resultType);

}
