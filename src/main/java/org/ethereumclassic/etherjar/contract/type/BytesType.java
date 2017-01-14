package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;

import java.util.Objects;
import java.util.Optional;

/**
 *  Dynamic sized byte sequence.
 */
public class BytesType implements DynamicType<byte[]> {

    /**
     * Try to parse a {@link BytesType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link BytesType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<BytesType> from(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "bytes") ?
                Optional.of(new BytesType()) : Optional.empty();
    }

    @Override
    public String getCanonicalName() {
        return "bytes";
    }

    @Override
    public HexData encode(byte... bytes) {
        HexData len = Type.encodeLength(bytes.length);

        return len.concat(new HexData(bytes),
                new HexData(new byte[Hex32.SIZE_BYTES - (bytes.length % Hex32.SIZE_BYTES)]));
    }

    @Override
    public byte[] decode(HexData data) {
        int len = Type.decodeLength(data.extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        if ((len == 0 && data.getSize() != Hex32.SIZE_BYTES * 2)
                || (len != 0 && data.getSize() < Hex32.SIZE_BYTES + len))
            throw new IllegalArgumentException("Insufficient data to decode bytes: " + data);

        return data.extract(len, Hex32.SIZE_BYTES).getBytes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        return Objects.equals(getClass(), obj.getClass());
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
