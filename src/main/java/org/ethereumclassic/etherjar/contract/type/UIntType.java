package org.ethereumclassic.etherjar.contract.type;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIntType extends NumericType {

    final static Map<Integer, BigInteger> POPULAR_MAX_VALUES =
            Collections.unmodifiableMap(
                    Stream.of(8, 16, 32, 64, 128, 256).collect(
                            Collectors.toMap(Function.identity(), UIntType::maxValue)));

    final static Pattern NAME_PATTERN = Pattern.compile("^uint(\\d*)$");

    static BigInteger maxValue(int bits) {
        if (bits < 0)
            throw new IllegalArgumentException("Negative number of bits: " + bits);

        return BigInteger.valueOf(2).shiftLeft(bits - 1);
    }

    private final BigInteger maxValue;

    public UIntType() {
        this(256);
    }

    public UIntType(int bits) {
        super(bits, false);

        maxValue = POPULAR_MAX_VALUES.containsKey(bits) ?
                POPULAR_MAX_VALUES.get(bits) : maxValue(bits);
    }

    @Override
    public BigInteger getMinValue() {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getMaxValue() {
        return maxValue;
    }

    @Override
    public <R> R visit(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Optional<UIntType> parse(String str) {
        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.find())
            return Optional.empty();

        String digits = matcher.group(1);

        return Optional.of(
                digits.isEmpty() ? new UIntType() : new UIntType(Integer.parseInt(digits)));
    }

    @Override
    public String getName() { return "uint" + getBits(); }

    @Override
    public String toString() {
        return String.format("%s{bytes=%s}", getClass().getSimpleName(), getBytes());
    }
}
