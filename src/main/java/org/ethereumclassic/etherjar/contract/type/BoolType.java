package org.ethereumclassic.etherjar.contract.type;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class BoolType extends NumericType {

    public final static BoolType DEFAULT_TYPE = new BoolType();

    /**
     * Try to parse a {@link BoolType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link BoolType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<BoolType> from(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "bool") ?
                Optional.of(DEFAULT_TYPE) : Optional.empty();
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
}
