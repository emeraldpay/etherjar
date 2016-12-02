package org.ethereumclassic.etherjar.contract.type;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class BoolType extends NumericType {

    /**
     * Try to parse a {@link BoolType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link BoolType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @see #getCanonicalName()
     */
    public static Optional<BoolType> from(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "bool") ?
                Optional.of(new BoolType()) : Optional.empty();
    }

    public BoolType() {
        super(8, false);
    }

    @Override
    public BigInteger getMinValue() {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getMaxValue() {
        return BigInteger.valueOf(2);
    }

    @Override
    public String getCanonicalName() {
        return "bool";
    }

    @Override
    public String toString() {
        return String.format("%s{}", getClass().getSimpleName());
    }
}
