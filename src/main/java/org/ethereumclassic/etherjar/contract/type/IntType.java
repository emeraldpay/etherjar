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

public class IntType extends NumericType {

    final static Map<Integer, BigInteger> POPULAR_MIN_VALUES =
            Collections.unmodifiableMap(
                    Stream.of(8, 16, 32, 64, 128, 256).collect(
                            Collectors.toMap(Function.identity(), IntType::minValue)));

    final static Map<Integer, BigInteger> POPULAR_MAX_VALUES =
            Collections.unmodifiableMap(
                    Stream.of(8, 16, 32, 64, 128, 256).collect(
                            Collectors.toMap(Function.identity(), IntType::maxValue)));

    final static Pattern NAME_PATTERN = Pattern.compile("^int(\\d*)$");

    static BigInteger minValue(int bits) {
        return maxValue(bits).negate();
    }

    static BigInteger maxValue(int bits) {
        if (bits < 0)
            throw new IllegalArgumentException("Negative number of bits: " + bits);

        return BigInteger.valueOf(2).shiftLeft(bits - 2);
    }

    private final BigInteger minValue;

    private final BigInteger maxValue;

    public IntType() {
        this(256);
    }

    public IntType(int bits) {
        super(bits, false);

        minValue = POPULAR_MIN_VALUES.containsKey(bits) ?
                POPULAR_MIN_VALUES.get(bits) : minValue(bits);

        maxValue = POPULAR_MAX_VALUES.containsKey(bits) ?
                POPULAR_MAX_VALUES.get(bits) : maxValue(bits);
    }

    @Override
    public BigInteger getMinValue() {
        return minValue;
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
    public Optional<IntType> parse(String str) {
        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.find())
            return Optional.empty();

        String digits = matcher.group(1);

        return Optional.of(
                digits.isEmpty() ? new IntType() : new IntType(Integer.parseInt(digits)));
    }

    @Override
    public String getName() { return "int" + getBits(); }

    @Override
    public String toString() {
        return String.format("%s{bytes=%s}", getClass().getSimpleName(), getBytes());
    }
}
