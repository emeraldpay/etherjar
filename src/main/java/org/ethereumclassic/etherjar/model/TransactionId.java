package org.ethereumclassic.etherjar.model;

/**
 * Transaction Hash value
 *
 * @author Igor Artamonov
 */
public class TransactionId extends HexValue {

    public static final int SIZE_BYTES = 32;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    protected TransactionId(byte[] value) {
        super(value);
    }

    protected TransactionId(String value) {
        super(value);
    }

    /**
     * Parse value from bytes representation. Value must be 32 bytes long.
     *
     * @param value bytes representation
     * @return TransactionId
     */
    public static TransactionId from(byte[] value) {
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Tx length: " + value.length);
        }
        return new TransactionId(value);
    }

    /**
     * Parse value from hex representation. Value must be 64 characters long.
     *
     * @param value bytes representation
     * @return TransactionId
     */
    public static TransactionId from(String value) {
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Tx length: " + value.length());
        }
        return new TransactionId(value);
    }
}
