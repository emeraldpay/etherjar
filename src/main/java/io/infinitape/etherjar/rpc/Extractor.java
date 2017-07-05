package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.model.HexQuantity;
import java.util.concurrent.*;

/**
 * @author Igor Artamonov
 */
public class Extractor {

    public CompletableFuture<Long> extractLong(final CompletableFuture<String> result) {
        return result.thenApply(HexQuantity::from)
            .thenApply(HexQuantity::getValue)
            .thenApply((q) -> q != null ? q.longValue() : null);
    }

}
