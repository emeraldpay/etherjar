package org.ethereumclassic.etherjar.model;

import org.ethereumclassic.etherjar.contract.type.MethodType;

/**
 * The first four bytes of the call data for a function call specifies the function to be called.
 *
 * <p>It is the first (left, high-order in big-endian) four bytes of the Keccak (SHA-3) hash of the signature of the function.
 *
 * @author Igor Artamonov
 * @see MethodType
 */
public class MethodId extends HexData {

    public static final int SIZE_BYTES = 4;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public MethodId(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static MethodId from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid MethodId length: " + value.length);
        }
        return new MethodId(value);
    }

    public static MethodId from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid MethodId length: " + value.length());
        }
        return new MethodId(HexData.from(value).getBytes());
    }
}
