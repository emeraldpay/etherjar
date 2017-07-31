package io.infinitape.etherjar.core;


public class Nonce extends HexData {

    public static final int SIZE_BYTES = 8;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public Nonce(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static Nonce from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Nonce length: " + value.length);
        }
        return new Nonce(value);
    }

    public static Nonce from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Nonce length: " + value.length());
        }
        return new Nonce(HexData.from(value).getBytes());
    }
}
