package org.ethereumclassic.etherjar.contract.type;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FixedType extends DecimalType {

    public final static FixedType DEFAULT_TYPE = new FixedType();

    final static Map<Integer, FixedType> CACHED_TYPES =
            Stream.of(8, 16, 32, 64, 128).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), FixedType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "fixed";

    final static Pattern NAME_PATTERN = Pattern.compile("fixed(<(\\d{1,3})>x<(\\d{1,3})>)?");

    /**
     * Try to parse a {@link FixedType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link FixedType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if a {@link IntType} has invalid input
     *
     * @see #getCanonicalName()
     */
    public static Optional<FixedType> from(String str) {
        if (!str.startsWith(NAME_PREFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'fixed' type format: " + str);

        if (Objects.isNull(matcher.group(1)))
            return Optional.of(DEFAULT_TYPE);

        int mBits = Integer.parseInt(matcher.group(2));
        int nBits = Integer.parseInt(matcher.group(3));

        return Optional.of(mBits == nBits && CACHED_TYPES.containsKey(mBits) ?
                CACHED_TYPES.get(mBits) : new FixedType(mBits, nBits));
    }

    static BigDecimal minValue(int mBits) {
        return powerOfTwo(mBits-1).negate();
    }

    static BigDecimal maxValue(int mBits, int nBits) {
        return powerOfTwo(mBits-1);
    }


    private final BigDecimal minValue;

    private final BigDecimal maxValue;

    public FixedType() {
        this(128, 128);
    }

    public FixedType(int bits) {
        this(bits, bits);
    }

    public FixedType(int mBits, int nBits) {
        super(mBits, nBits, true);

        minValue = minValue(mBits);
        maxValue = maxValue(mBits, nBits);
    }

    @Override
    public BigDecimal getMinValue() {
        return minValue;
    }

    @Override
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    @Override
    public String getCanonicalName() {
        return String.format("fixed<%d>x<%d>", getMBits(), getNBits());
    }
}
