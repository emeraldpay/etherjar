package io.emeraldpay.etherjar.domain;

import org.jspecify.annotations.NonNull;

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
