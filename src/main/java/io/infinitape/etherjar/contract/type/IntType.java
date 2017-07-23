package io.infinitape.etherjar.contract.type;

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

    public final static IntType DEFAULT = new IntType();

    final static Map<Integer, IntType> CACHED_INSTANCES =
            Stream.of(8, 16, 32, 64, 128, 256).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), IntType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "int";

    final static Pattern NAME_PATTERN = Pattern.compile("int(\\d*)");

    /**
     * Try to parse a {@link IntType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link IntType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if a {@link IntType} has invalid input
     * @see #getCanonicalName()
     */
    public static Optional<IntType> from(String str) {
        if (!str.startsWith(NAME_PREFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'int' type format: " + str);

        String digits = matcher.group(1);

        if (digits.isEmpty())
            return Optional.of(DEFAULT);

        int bits = Integer.parseInt(digits);

        return Optional.of(CACHED_INSTANCES.containsKey(bits) ?
                CACHED_INSTANCES.get(bits) : new IntType(bits));
    }

    private final BigInteger minValue;

    private final BigInteger maxValue;

    public IntType() {
        this(256);
    }

    public IntType(int bits) {
        super(bits, true);

        minValue = powerOfTwo(bits - 1).negate();
        maxValue = powerOfTwo(bits - 1);
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
    public String getCanonicalName() { return "int" + getBits(); }
}
