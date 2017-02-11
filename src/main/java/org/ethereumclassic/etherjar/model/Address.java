package org.ethereumclassic.etherjar.model;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * Ethereum Wallet address
 *
 * @author Igor Artamonov
 */
public class Address extends HexData {

    public static final int SIZE_BYTES = 20;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public static final Address EMPTY = Address.from("0x0000000000000000000000000000000000000000");

    private static final Pattern ALL_LOW_PATTERN = Pattern.compile("^0x[0-9a-f]{40}$");
    private static final Pattern ALL_CAP_PATTERN = Pattern.compile("^0x[0-9A-F]{40}$");
    private static final Pattern INVARIANT_PATTERN = Pattern.compile("^0x[0-9a-fA-F]{40}$");

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
     * Validate address according to EIP 55
     * https://github.com/ethereum/EIPs/issues/55
     *
     * @param address 0x...
     * @return true if address correct or false otherwise
     */
    public static boolean isAddress(String address) {
        if (!INVARIANT_PATTERN.matcher(address).matches()) {
            // check if it has the basic requirements of an address
            return false;
        } else if (ALL_LOW_PATTERN.matcher(address).matches() ||
                ALL_CAP_PATTERN.matcher(address).matches()) {
            // If it's all small caps or all big caps, return true
            return true;
        } else {
            return isChecksumAddress(address);
        }
    }

    /**
     * Checks if the given string is a checksummed address
     *
     * In other implementation you can find sha3 function call,
     * but actually it is Keccak256.
     * See https://medium.com/@ConsenSys/are-you-really-using-sha-3-or-old-code-c5df31ad2b0
     *
     * @param address
     * @return true if address checksummed
     */
    static boolean isChecksumAddress(String address) {
        address = address.replace("0x", "");
        String addressHash = keccak256(address.toLowerCase());

        for (int i = 0; i < 40; i++) {
            // the nth letter should be uppercase if the nth digit of casemap is 1
            char nthChar = address.charAt(i);
            int nthHashDigit = Character.digit(addressHash.charAt(i), 16);
            if ((nthHashDigit > 7 && Character.toUpperCase(nthChar) != nthChar) ||
                    (nthHashDigit <= 7 && Character.toLowerCase(nthChar) != nthChar)) {
                return false;
            }
        }
        return true;
    }

    static String keccak256(String input) {
        final Keccak.DigestKeccak m = new Keccak.Digest256();
        try {
            m.update(input.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            m.update(input.getBytes());
        }
        return Hex.toHexString(m.digest());
    }
}
