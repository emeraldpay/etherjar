package org.ethereumclassic.etherjar.model;

/**
 * An address, followed by a function selector.
 */
public class Function extends HexData {

    public static final int SIZE_BYTES = 24;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    private Function(byte[] bytes) {
        super(bytes, SIZE_BYTES);
    }

    public static Function from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Function");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Function length: " + value.length);
        }
        return new Function(value);
    }

    public static Function from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Function");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Function length: " + value.length());
        }
        return new Function(HexData.from(value).getBytes());
    }
}
