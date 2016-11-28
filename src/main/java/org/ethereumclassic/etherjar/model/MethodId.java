package org.ethereumclassic.etherjar.model;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.ethereumclassic.etherjar.contract.ContractMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * The first four bytes of the call data for a function call specifies the function to be called.
 *
 * <p>It is the first (left, high-order in big-endian) four bytes of the Keccak (SHA-3) hash
 * of the signature of the function.
 *
 * @author Igor Artamonov
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#function-selector">Function Selector</a>
 * @see ContractMethod
 */
public class MethodId extends HexData {

    public static final int SIZE_BYTES = 4;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public static MethodId fromSignature(String name, String... types) {
        return fromSignature(name, Arrays.asList(types));
    }

    public static MethodId fromSignature(String name, Collection<String> types) {
        String sign = Objects.requireNonNull(name) +
                '(' + String.join(",", Objects.requireNonNull(types)) + ')';

        byte[] head = new byte[4];
        Keccak.Digest256 digest256 = new Keccak.Digest256();

        digest256.update(sign.getBytes());
        System.arraycopy(digest256.digest(), 0, head, 0, 4);

        return from(head);
    }

    public static MethodId from(byte[] value) {
        if (value == null)
            throw new IllegalArgumentException("Null Hash");

        if (value.length != SIZE_BYTES)
            throw new IllegalArgumentException("Invalid MethodId length: " + value.length);

        return new MethodId(value);
    }

    public static MethodId from(String value) {
        if (value == null)
            throw new IllegalArgumentException("Null Hash");

        if (value.length() != SIZE_HEX)
            throw new IllegalArgumentException("Invalid MethodId length: " + value.length());

        return new MethodId(HexData.from(value).getBytes());
    }

    public MethodId(byte[] value) {
        super(value, SIZE_BYTES);
    }
}
