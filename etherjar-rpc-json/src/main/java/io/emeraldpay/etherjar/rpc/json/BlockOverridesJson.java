package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The fields of this object customize the block as part of which a call is simulated. This object can be passed to <code>eth_call</code>, <code>eth_simulateV1</code> as well as <code>debug_traceCall</code> methods.
 *
 * @see <a href="https://geth.ethereum.org/docs/interacting-with-geth/rpc/objects#block-overrides">eth_simulateV1</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockOverridesJson {

    /**
     * Block number
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long number;

    /**
     * The previous value of randomness beacon
     */
    private Hex32 prevRandao;

    /**
     * Block timestamp
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long time;

    /**
     * Gas limit.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gasLimit;

    /**
     * Fee recipient (also known as coinbase).
     */
    private Address feeRecipient;

    /**
     * Withdrawals made by validators.
     */
    private List<WithdrawalJson> withdrawals;

    /**
     * Base fee per unit of gas (see EIP-1559).
     */
    private Wei baseFeePerGas;

    /**
     * Base fee per unit of blob gas (see EIP-4844).
     */
    private Wei blobBaseFee;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Hex32 getPrevRandao() {
        return prevRandao;
    }

    public void setPrevRandao(Hex32 prevRandao) {
        this.prevRandao = prevRandao;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Address getFeeRecipient() {
        return feeRecipient;
    }

    public void setFeeRecipient(Address feeRecipient) {
        this.feeRecipient = feeRecipient;
    }

    public List<WithdrawalJson> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<WithdrawalJson> withdrawals) {
        this.withdrawals = withdrawals;
    }

    public BlockOverridesJson appendWithdrawal(WithdrawalJson withdrawal) {
        if (withdrawals == null) {
            withdrawals = new ArrayList<>();
        }
        withdrawals.add(withdrawal);
        return this;
    }

    public Wei getBaseFeePerGas() {
        return baseFeePerGas;
    }

    public void setBaseFeePerGas(Wei baseFeePerGas) {
        this.baseFeePerGas = baseFeePerGas;
    }

    public Wei getBlobBaseFee() {
        return blobBaseFee;
    }

    public void setBlobBaseFee(Wei blobBaseFee) {
        this.blobBaseFee = blobBaseFee;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockOverridesJson that)) return false;
        return Objects.equals(number, that.number) && Objects.equals(prevRandao, that.prevRandao) && Objects.equals(time, that.time) && Objects.equals(gasLimit, that.gasLimit) && Objects.equals(feeRecipient, that.feeRecipient) && Objects.equals(withdrawals, that.withdrawals) && Objects.equals(baseFeePerGas, that.baseFeePerGas) && Objects.equals(blobBaseFee, that.blobBaseFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, prevRandao, time, gasLimit, feeRecipient, withdrawals, baseFeePerGas, blobBaseFee);
    }
}
