package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

public class TraceCallConfigJson {

    /**
     * Overrides for the state data (accounts/storage) for the call
     */
    private StateOverrideJson stateOverrides;

    /**
     * Overrides for the block data (number, timestamp etc) for the call
     */
    private BlockOverridesJson blockOverrides;

    /**
     * If set, the state at the given transaction index will be used to tracing
     * (default = the last transaction index in the block)
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long txIndex;

    public StateOverrideJson getStateOverrides() {
        return stateOverrides;
    }

    public void setStateOverrides(StateOverrideJson stateOverrides) {
        this.stateOverrides = stateOverrides;
    }

    public BlockOverridesJson getBlockOverrides() {
        return blockOverrides;
    }

    public void setBlockOverrides(BlockOverridesJson blockOverrides) {
        this.blockOverrides = blockOverrides;
    }

    public Long getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(Long txIndex) {
        this.txIndex = txIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TraceCallConfigJson that = (TraceCallConfigJson) o;
        return Objects.equals(stateOverrides, that.stateOverrides)
            && Objects.equals(blockOverrides, that.blockOverrides)
            && Objects.equals(txIndex, that.txIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateOverrides, blockOverrides, txIndex);
    }
}
