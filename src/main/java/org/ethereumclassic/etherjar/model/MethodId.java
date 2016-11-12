package org.ethereumclassic.etherjar.model;

/**
 * @author Igor Artamonov
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
