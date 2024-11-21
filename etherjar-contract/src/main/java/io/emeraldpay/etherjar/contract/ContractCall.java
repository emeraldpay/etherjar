package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.rpc.json.TransactionCallJson;
import io.emeraldpay.etherjar.tx.Transaction;

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

    /**
     * Convert to a JSON-RPC request, suitable for a <em>read call</em>.
     *
     * @return JSON RPC request
     */
    public TransactionCallJson toJson() {
        TransactionCallJson json = new TransactionCallJson();
        json.setTo(contract);
        json.setInput(data.toData());
        return json;
    }

    /**
     * Convert to an <em>unsigned</em> transaction, without Gas Amount, Gas Price, etc. I.e., only
     * contract data and address are set, and all other fields are supposed to be set by sender.
     *
     * @return pre-filled transaction
     */
    public Transaction toTransaction() {
        Transaction tx = new Transaction();
        tx.setTo(contract);
        tx.setData(data.toData());
        return tx;
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
