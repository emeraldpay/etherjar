package io.infinitape.etherjar.util;

import java.math.BigInteger;

/**
 * Hex-encoded {@link String} to and from {@link java.math.BigInteger} mixin interface.
 */
public interface HexEncoding {

    /**
     * The prefix used for a full hex encoding format.
     */
    String HEX_PREFIX = "0x";

    /**
     * @param hex hex-encoded {@link String} with optional {@link #HEX_PREFIX}
     * @return {@link BigInteger} instance
     * @see #HEX_PREFIX
     */
    static BigInteger fromHex(String hex) {
        return new BigInteger(
            hex.startsWith(HEX_PREFIX) ?
                hex.substring(HEX_PREFIX.length()) : hex, 16);
    }

    /**
     * @see #toFullHex(BigInteger)
     */
    static String toHex(BigInteger num) {
        return toFullHex(num);
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Full (with {@link #HEX_PREFIX}) hex-encoded {@link String}
     * @see #HEX_PREFIX
     */
    static String toFullHex(BigInteger num) {
        return HEX_PREFIX.concat(num.toString(16));
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Naked (without {@link #HEX_PREFIX}) hex-encoded {@link String}
     */
    static String toNakedHex(BigInteger num) {
        return num.toString(16);
    }
}
