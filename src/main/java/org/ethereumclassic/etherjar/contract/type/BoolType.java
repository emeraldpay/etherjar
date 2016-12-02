package org.ethereumclassic.etherjar.contract.type;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class BoolType extends NumericType {

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
    public Optional<BoolType> parse(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "bool") ?
                Optional.of(new BoolType()) : Optional.empty();
    }

    @Override
    public String getName() {
        return "bool";
    }

    @Override
    public String toString() {
        return String.format("%s{}", getClass().getSimpleName());
    }
}
