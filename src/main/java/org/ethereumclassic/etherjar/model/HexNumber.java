package org.ethereumclassic.etherjar.model;

import java.math.BigInteger;

/**
 * Parser of Ethereum RPC representation of numbers
 *
 * @author Igor Artamonov
 */
public class HexNumber {

    private BigInteger value;

    public HexNumber(BigInteger value) {
        this.value = value;
    }

    /**
     *
     * @param value hex value with '0x' prefix
     * @return corresponding BigInteger
     */
    public static HexNumber parse(String value) {
        return new HexNumber(new BigInteger(value.substring(2), 16));
    }

    public static HexNumber valueOf(long value) {
        return new HexNumber(BigInteger.valueOf(value));
    }


    public BigInteger getValue() {
        return value;
    }

    public String toHex() {
        return "0x" + value.toString(16);
    }

    public String toString() {
        return toHex();
    }
}
