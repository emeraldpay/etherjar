package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.rpc.json.TransactionCallJson;

import java.util.Objects;

public class ContractCall {

    private final Address contract;
    private final ContractData data;

    public ContractCall(Address contract, ContractData data) {
        this.contract = contract;
        this.data = data;
    }

    public Address getContract() {
        return contract;
    }

    public ContractData getData() {
        return data;
    }

    public TransactionCallJson toJson() {
        TransactionCallJson json = new TransactionCallJson();
        json.setTo(contract);
        json.setData(data.toData());
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractCall)) return false;
        ContractCall that = (ContractCall) o;
        return Objects.equals(contract, that.contract) &&
            Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contract, data);
    }
}
