package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.rpc.json.TransactionLogJson;

abstract public class ContractEvent {

    /**
     * Write event details to log. Note that it replaces existing values in Topics and Data in the provided log.
     *
     * @param log target log
     */
    abstract void writeTo(TransactionLogJson log);

    /**
     * Factory that can read Event details from a {@link TransactionLogJson}
     * @param <T> type of event
     */
    public interface Factory<T extends ContractEvent> {
        T readFrom(TransactionLogJson log);
    }

}
