package io.infinitape.etherjar.model;

public class BlockHash extends HexData {

    public static final int SIZE_BYTES = 32;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public BlockHash(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static BlockHash from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Block Hash length: " + value.length);
        }
        return new BlockHash(value);
    }

    public static BlockHash from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hash");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Block Hash length: " + value.length());
        }
        return new BlockHash(HexData.from(value).getBytes());
    }
}
