package org.ethereumclassic.etherjar.model;

import java.math.BigInteger;

/**
 * Fixed size value, such as Wallet Address, represented in Hex
 *
 * @author Igor Artamonov
 */
public class HexValue {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private final byte[] value;

    public HexValue(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Empty value");
        }
        this.value = value;
    }

    public static HexValue from(long value) {
        return new HexValue(BigInteger.valueOf(value).toByteArray());
    }

    /**
     * Parse ethereum hex representation for a number. Value should start with 0x
     *
     * @param value hex value
     */
    public HexValue(String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Empty value");
        }
        if (!value.startsWith("0x")) {
            throw new IllegalArgumentException("Invalid Ethereum Hex format: " + value);
        }
        value = value.substring(2);
        if (value.length() > 0) {
            byte[] bytes = new BigInteger(value, 16).toByteArray();
            int valueLength = value.length() / 2;

            if (bytes.length == valueLength) {
                this.value = bytes;
            } else {
                this.value = new byte[valueLength];
                // for values like 0xffffff it produces extra 0 byte in the beginning, we need to skip it
                int pos = bytes.length > this.value.length ? bytes.length - this.value.length : 0;
                System.arraycopy(bytes, pos, this.value, this.value.length - bytes.length + pos, bytes.length - pos);
            }
        } else {
            this.value = new byte[0];
        }
    }

    public String toHex() {
        final char[] hex = new char[value.length * 2 + 2];
        hex[0] = '0';
        hex[1] = 'x';
        for(int i = 0, j = 2; i < value.length; i++){
            hex[j++] = HEX_DIGITS[(0xF0 & value[i]) >>> 4];
            hex[j++] = HEX_DIGITS[0x0F & value[i]];
        }
        return new String(hex);
    }

    public String toString() {
        return toHex();
    }

    public byte[] getBytes() {
        return value.clone();
    }
}
