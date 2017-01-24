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

public class UFixedType extends DecimalType {

    public final static UFixedType DEFAULT = new UFixedType();

    final static Map<Integer, UFixedType> CACHED_TYPES =
            Stream.of(8, 16, 32, 64, 128).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), UFixedType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "ufixed";

    final static Pattern NAME_PATTERN = Pattern.compile("ufixed(<(\\d{1,3})>x<(\\d{1,3})>)?");

    /**
     * Try to parse a {@link UFixedType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link UFixedType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if a {@link IntType} has invalid input
     *
     * @see #getCanonicalName()
     */
    public static Optional<UFixedType> from(String str) {
        if (!str.startsWith(NAME_PREFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'ufixed' type format: " + str);

        if (Objects.isNull(matcher.group(1)))
            return Optional.of(DEFAULT);

        int mBits = Integer.parseInt(matcher.group(2));
        int nBits = Integer.parseInt(matcher.group(3));

        return Optional.of(mBits == nBits && CACHED_TYPES.containsKey(mBits) ?
                CACHED_TYPES.get(mBits) : new UFixedType(mBits, nBits));
    }

    static BigDecimal maxValue(int mBits) { return powerOfTwo(mBits); }

    private final BigDecimal maxValue;

    public UFixedType() {
        this(128, 128);
    }

    public UFixedType(int bits) {
        this(bits, bits);
    }

    public UFixedType(int mBits, int nBits) {
        super(mBits, nBits, false);

        maxValue = maxValue(mBits);
    }

    @Override
    public BigDecimal getMinValue() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    @Override
    public String getCanonicalName() {
        return String.format("ufixed<%d>x<%d>", getMBits(), getNBits());
    }
}
