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

    final static Map<Integer, UFixedType> CACHED_INSTANCES =
            Stream.of(8, 16, 32, 64, 128).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), UFixedType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "ufixed";

    final static Pattern NAME_PATTERN = Pattern.compile("ufixed((\\d{1,3})x(\\d{1,3}))?");

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

        return Optional.of(mBits == nBits && CACHED_INSTANCES.containsKey(mBits) ?
                CACHED_INSTANCES.get(mBits) : new UFixedType(mBits, nBits));
    }

    private final BigDecimal minValue;

    private final BigDecimal maxValue;

    private final NumericType numericType;

    public UFixedType() {
        this(128, 128);
    }

    public UFixedType(int bits) {
        this(bits, bits);
    }

    public UFixedType(int mBits, int nBits) {
        super(mBits, nBits);

        numericType = new UIntType(mBits + nBits);

        minValue = new BigDecimal(
                numericType.getMinValue().shiftRight(nBits));
        maxValue = new BigDecimal(
                numericType.getMaxValue().shiftRight(nBits));
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
    public NumericType getNumericType() {
        return numericType;
    }

    @Override
    public String getCanonicalName() {
        return String.format("ufixed%dx%d", getMBits(), getNBits());
    }
}
