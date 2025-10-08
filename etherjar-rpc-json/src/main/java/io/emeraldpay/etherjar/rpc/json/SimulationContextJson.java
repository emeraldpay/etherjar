package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

public class SimulationContextJson {

    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long blockNumber;

    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long transactionIndex;

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimulationContextJson that = (SimulationContextJson) o;
        return Objects.equals(blockNumber, that.blockNumber) && Objects.equals(transactionIndex, that.transactionIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockNumber, transactionIndex);
    }
}
