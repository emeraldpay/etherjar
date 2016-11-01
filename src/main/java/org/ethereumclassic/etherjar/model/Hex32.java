package org.ethereumclassic.etherjar.model;


public class Hex32 extends HexData {

    public static final int SIZE_BYTES = 32;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public Hex32(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static Hex32 from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Hex32 length: " + value.length);
        }
        return new Hex32(value);
    }

    public static Hex32 from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Hex32 length: " + value.length());
        }
        return new Hex32(HexData.from(value).getBytes());
    }
}
