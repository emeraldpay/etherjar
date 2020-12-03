package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rpc.Commands;
import io.emeraldpay.etherjar.rpc.RpcCall;
import io.emeraldpay.etherjar.rpc.json.BlockTag;

import java.util.function.Function;

public class ContractReadCall<T> extends ContractCall {

    private final Function<HexData, T> processor;

    public ContractReadCall(Address contract, ContractData data, Function<HexData, T> processor) {
        super(contract, data);
        this.processor = processor;
    }

    public Function<HexData, T> getProcessor() {
        return processor;
    }

    public T processResult(HexData result) {
        return processor.apply(result);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getTargetClass() {
        return (Class<T>) Object.class;
    }

    public RpcCall<String, T> toRpcCall() {
        return toRpcCall(BlockTag.LATEST);
    }

    public RpcCall<String, T> toRpcCall(BlockTag block) {
        return Commands.eth()
            .call(this.toJson(), block)
            .converted(getTargetClass(), (hex) -> processResult(HexData.from(hex)));
    }

    public RpcCall<String, T> toRpcCall(Long height) {
        return Commands.eth()
            .call(this.toJson(), height)
            .converted(getTargetClass(), (hex) -> processResult(HexData.from(hex)));
    }
}
