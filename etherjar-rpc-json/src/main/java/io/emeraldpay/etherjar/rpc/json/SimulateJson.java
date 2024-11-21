package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;

import java.util.*;

/**
 * <code>eth_simulateV1</code> payload.
 * @see <a href="https://geth.ethereum.org/docs/interacting-with-geth/rpc/ns-eth#eth-simulate-v1">eth_simulateV1</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulateJson {

    /**
     * REQUIRED<br/>
     *
     * Definition of blocks that can contain calls and overrides
     */
    private List<BlockStateCalls> blockStateCalls;

    /**
     * When true, the eth_simulateV1 does all the validation that a normal EVM would do, except contract sender and signature checks. When false, eth_simulateV1 behaves like eth_call.
     */
    private Boolean validation;

    /**
     * Adds ETH transfers as ERC20 transfer events to the logs. These transfers have emitter contract parameter set as address(0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee). This allows you to track movements of ETH in your calls.
     */
    private Boolean traceTransfers;

    /**
     * When true, the method returns full transaction objects, otherwise, just hashes are returned.
     */
    private Boolean returnFullTransactions;

    public List<BlockStateCalls> getBlockStateCalls() {
        return blockStateCalls;
    }

    public void setBlockStateCalls(List<BlockStateCalls> blockStateCalls) {
        this.blockStateCalls = blockStateCalls;
    }

    public Boolean getTraceTransfers() {
        return traceTransfers;
    }

    public void setTraceTransfers(Boolean traceTransfers) {
        this.traceTransfers = traceTransfers;
    }

    public Boolean getValidation() {
        return validation;
    }

    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    public Boolean getReturnFullTransactions() {
        return returnFullTransactions;
    }

    public void setReturnFullTransactions(Boolean returnFullTransactions) {
        this.returnFullTransactions = returnFullTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimulateJson that)) return false;
        return Objects.equals(blockStateCalls, that.blockStateCalls)
            && Objects.equals(traceTransfers, that.traceTransfers)
            && Objects.equals(validation, that.validation)
            && Objects.equals(returnFullTransactions, that.returnFullTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockStateCalls, traceTransfers, validation, returnFullTransactions);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BlockStateCalls {
        private BlockOverridesJson blockOverrides;

        private Map<Address, StateOverrideJson> stateOverrides;

        private List<TransactionCallJson> calls;

        public BlockOverridesJson getBlockOverrides() {
            return blockOverrides;
        }

        public void setBlockOverrides(BlockOverridesJson blockOverrides) {
            this.blockOverrides = blockOverrides;
        }

        public Map<Address, StateOverrideJson> getStateOverrides() {
            return stateOverrides;
        }

        public void setStateOverrides(Map<Address, StateOverrideJson> stateOverrides) {
            this.stateOverrides = stateOverrides;
        }

        public List<TransactionCallJson> getCalls() {
            return calls;
        }

        public void setCalls(List<TransactionCallJson> calls) {
            this.calls = calls;
        }

        public BlockStateCalls appendCall(TransactionCallJson call) {
            if (calls == null) {
                calls = new ArrayList<>();
            }
            calls.add(call);
            return this;
        }

        public BlockStateCalls overrideState(Address address, StateOverrideJson state) {
            if (stateOverrides == null) {
                stateOverrides = new HashMap<>();
            }
            stateOverrides.put(address, state);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BlockStateCalls that)) return false;
            return Objects.equals(blockOverrides, that.blockOverrides) && Objects.equals(stateOverrides, that.stateOverrides) && Objects.equals(calls, that.calls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockOverrides, stateOverrides, calls);
        }
    }

}
