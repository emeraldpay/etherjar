package io.emeraldpay.etherjar.rpc;

import io.emeraldpay.etherjar.hex.HexQuantity;
import io.emeraldpay.etherjar.rpc.json.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class DebugCommands {

    public RpcCall<ExecutionResultJson, ExecutionResultJson> traceCall(TransactionCallJson call,
                                                           BlockTag block,
                                                           @Nullable TraceCallConfigJson config) {
        return RpcCall.create("debug_traceCall", ExecutionResultJson.class, call, block, config);
    }

    public RpcCall<ExecutionResultJson, ExecutionResultJson> traceCall(TransactionCallJson call,
                                                                       Long block,
                                                                       @Nullable TraceCallConfigJson config) {
        return RpcCall.create("debug_traceCall", ExecutionResultJson.class, call, HexQuantity.from(block), config);
    }
}
