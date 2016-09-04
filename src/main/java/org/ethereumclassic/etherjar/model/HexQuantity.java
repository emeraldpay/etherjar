package org.ethereumclassic.etherjar.model;

import java.math.BigInteger;

/**
 * Parser of Ethereum RPC representation of numbers
 *
 * @author Igor Artamonov
 */
public class HexQuantity {

    private BigInteger value;

    public HexQuantity(BigInteger value) {
        this.value = value;
    }

    /**
     *
     * @param value hex value with '0x' prefix
     * @return corresponding BigInteger
     */
    public static HexQuantity from(String value) {
        return new HexQuantity(new BigInteger(value.substring(2), 16));
    }

    public static HexQuantity from(long value) {
        return new HexQuantity(BigInteger.valueOf(value));
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
