package io.infinitape.etherjar.core;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.util.regex.Pattern;

/**
 * Ethereum Wallet address
 */
public class Address extends HexData {

    public static final int SIZE_BYTES = 20;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public static final Address EMPTY = Address.from("0x0000000000000000000000000000000000000000");

    private static final Pattern CASE_INSENSITIVE_PATTERN = Pattern.compile("0x(?i:[0-9a-f]{40})");
    private static final Pattern CASE_SENSITIVE_PATTERN = Pattern.compile("0x(?:[0-9a-f]{40}|[0-9A-F]{40})");

    private Address(byte[] bytes) {
        super(bytes, SIZE_BYTES);
    }

    public static Address from(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Address");
        }
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Address length: " + value.length);
        }
        return new Address(value);
    }

    public static Address from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null Address");
        }
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Address length: " + value.length());
        }
        return new Address(HexData.from(value).getBytes());
    }

    /**
     * Validate address according to EIP 55.
     *
     * @param address a wallet address ('0x...')
     * @return <code>true</code> if address correct or <code>false</code> otherwise
     * @see <a href="https://github.com/ethereum/EIPs/issues/55">EIP 55</a>
     */
    public static boolean isValidAddress(String address) {
        return CASE_INSENSITIVE_PATTERN.matcher(address).matches()
                && (CASE_SENSITIVE_PATTERN.matcher(address).matches() || isValidChecksum(address));
    }

    /**
     * Checks if the given string is an address with checksum (Keccak256).
     *
     * @param address a wallet address ('0x...')
     * @return <code>true</code> if address with checksum
     */
    static boolean isValidChecksum(String address) {
        Keccak.Digest256 digest256 = new Keccak.Digest256();

        digest256.update(
                address.substring(2).toLowerCase().getBytes());

        String hash = Hex.toHexString(digest256.digest());

        for (int i = 0; i < 40; i++) {
            char ch = address.charAt(i + 2);
            int dg = Character.digit(hash.charAt(i), 16);

            if ((dg > 7 && Character.toUpperCase(ch) != ch)
                    || (dg <= 7 && Character.toLowerCase(ch) != ch))
                return false;
        }

        return true;
    }
}
