package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;

import java.util.Objects;

/**
 * @see <a href="https://github.com/ethereum/execution-apis/blob/main/src/schemas/withdrawal.yaml">https://github.com/ethereum/execution-apis/blob/main/src/schemas/withdrawal.yaml</a>
 */
public class WithdrawalJson {
    @JsonDeserialize(using = HexLongDeserializer.class)
    private Long index;

    @JsonDeserialize(using = HexLongDeserializer.class)
    private Long validatorIndex;

    @JsonDeserialize(using = AddressDeserializer.class)
    private Address address;

    @JsonDeserialize(using = WeiDeserializer.class)
    private Wei amount;

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Long getValidatorIndex() {
        return validatorIndex;
    }

    public void setValidatorIndex(Long validatorIndex) {
        this.validatorIndex = validatorIndex;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Wei getAmount() {
        return amount;
    }

    public void setAmount(Wei amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WithdrawalJson)) return false;
        WithdrawalJson that = (WithdrawalJson) o;
        return Objects.equals(index, that.index) && Objects.equals(validatorIndex, that.validatorIndex) && Objects.equals(address, that.address) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, validatorIndex, amount);
    }
}
