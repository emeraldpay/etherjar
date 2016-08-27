package org.ethereumclassic.etherjar.model;

import java.math.BigInteger;

/**
 * Parser of Ethereum RPC representation of numbers
 *
 * @author Igor Artamonov
 */
public class HexNumber {

    /**
     *
     * @param value hex value with '0x' prefix
     * @return corresponding BigInteger
     */
    public static BigInteger parse(String value) {
        return new BigInteger(value.substring(2), 16);
    }

}
