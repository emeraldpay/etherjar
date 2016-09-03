package org.ethereumclassic.etherjar.model;

/**
 * Ethereum Wallet address
 *
 * @author Igor Artamonov
 */
public class Address extends HexData {

    public static final int SIZE_BYTES = 20;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    private Address(byte[] bytes) {
        super(bytes);
    }
    private Address(String hex) {
        super(hex);
    }

    public static Address from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Address");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Address length: " + value.length);
        }
        return new Address(value);
    }

    public static Address from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Address");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Address length: " + value.length());
        }
        return new Address(value);
    }
}
