package io.infinitape.etherjar.model;

/**
 * Fixed-size 32-bytes hex value.
 */
public class Hex32 extends HexData {

    public static final int SIZE_BYTES = 32;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public static final Hex32 EMPTY =
            Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000000");

    public static Hex32 from(HexData data) {
        if (data instanceof Hex32)
            return (Hex32) data;

        if (data.getSize() != Hex32.SIZE_BYTES)
            throw new IllegalArgumentException(
                    String.format("Data length is not %d: %d", Hex32.SIZE_BYTES, data.getSize()));

        return from(data.getBytes());
    }

    public Hex32(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static Hex32 from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hex32");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Hex32 length: " + value.length);
        }
        return new Hex32(value);
    }

    public static Hex32 from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Hex32");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Hex32 length: " + value.length());
        }
        return new Hex32(HexData.from(value).getBytes());
    }
}
