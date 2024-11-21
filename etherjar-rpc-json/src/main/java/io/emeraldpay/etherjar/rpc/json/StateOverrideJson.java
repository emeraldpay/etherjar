package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.util.Map;
import java.util.Objects;

/**
 * The state override set is an optional address-to-state mapping, used in <code>eth_call</code> and <code>eth_simulateV1</code>, where each entry specifies some state to be ephemerally overridden prior to executing the call.
 * <br/>
 * The goal of the state override set is manyfold:
 * <ul>
 *     <li>It can be used by websites to reduce the amount of contract code needed to be deployed on chain. Code that simply returns internal state or does pre-defined validations can be kept off chain and fed to the node on-demand.</li>
 *     <li>It can be used for smart contract analysis by extending the code deployed on chain with custom methods and invoking them. This avoids having to download and reconstruct the entire state in a sandbox to run custom code against.</li>
 *     <li>It can be used to debug smart contracts in an already deployed large suite of contracts by selectively overriding some code or state and seeing how execution changes. Specialized tooling will probably be necessary.</li>
 *     <li>It can be used to override <code>ecrecover</code> precompile to spoof signatures</li>
 * </ul>
 *
 * @see <a href="https://geth.ethereum.org/docs/interacting-with-geth/rpc/objects#state-override-set">State Override Set</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateOverrideJson {

    /**
     * Fake balance to set for the account before executing the call.
     */
    private Wei balance;

    /**
     * Fake nonce to set for the account before executing the call.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long nonce;

    /**
     * Fake EVM bytecode to inject into the account before executing the call.
     */
    private HexData code;

    /**
     * Fake key-value mapping to override <strong>all</strong> slots in the account storage before executing the call.
     */
    private Map<Hex32, Hex32> state;

    /**
     * Fake key-value mapping to override <strong>individual</strong> slots in the account storage before executing the call.
     */
    private Map<Hex32, Hex32> stateDiff;

    /**
     * Moves precompile to given address
     */
    private Address movePrecompileToAddress;

    public Wei getBalance() {
        return balance;
    }

    public void setBalance(Wei balance) {
        this.balance = balance;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public HexData getCode() {
        return code;
    }

    public void setCode(HexData code) {
        this.code = code;
    }

    public Map<Hex32, Hex32> getState() {
        return state;
    }

    public void setState(Map<Hex32, Hex32> state) {
        this.state = state;
    }

    public Map<Hex32, Hex32> getStateDiff() {
        return stateDiff;
    }

    public void setStateDiff(Map<Hex32, Hex32> stateDiff) {
        this.stateDiff = stateDiff;
    }

    public Address getMovePrecompileToAddress() {
        return movePrecompileToAddress;
    }

    public void setMovePrecompileToAddress(Address movePrecompileToAddress) {
        this.movePrecompileToAddress = movePrecompileToAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateOverrideJson that)) return false;
        return Objects.equals(balance, that.balance) && Objects.equals(nonce, that.nonce) && Objects.equals(code, that.code) && Objects.equals(state, that.state) && Objects.equals(stateDiff, that.stateDiff) && Objects.equals(movePrecompileToAddress, that.movePrecompileToAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balance, nonce, code, state, stateDiff, movePrecompileToAddress);
    }
}
