package io.emeraldpay.etherjar.domain;

/**
 * Transaction reference
 */
public interface TransactionRef {

    /**
     *
     * @return hash of the transaction
     */
    TransactionId getHash();

}
