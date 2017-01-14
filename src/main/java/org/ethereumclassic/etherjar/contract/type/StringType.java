package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.HexData;

import java.util.Objects;
import java.util.Optional;

/**
 * Dynamic sized unicode string assumed to be UTF-8 encoded.
 */
public class StringType implements DynamicType<String> {

    /**
     * Try to parse a {@link StringType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link StringType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<BytesType> from(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "string") ?
                Optional.of(new BytesType()) : Optional.empty();
    }

    @Override
    public String getCanonicalName() {
        return "string";
    }

    @Override
    public HexData encode(String obj) {
        return null;
    }

    @Override
    public String decode(HexData data) {
        return null;
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
