/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.infinitape.etherjar.core.contract.type;

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

    public final static FixedType DEFAULT = new FixedType();

    final static Map<Integer, FixedType> CACHED_INSTANCES =
            Stream.of(8, 16, 32, 64, 128).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), FixedType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "fixed";

    final static Pattern NAME_PATTERN = Pattern.compile("fixed((\\d{1,3})x(\\d{1,3}))?");

    /**
     * Try to parse a {@link FixedType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link FixedType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is {@code null}
     * @throws IllegalArgumentException if a {@link IntType} has invalid input
     * @see #getCanonicalName()
     */
    public static Optional<FixedType> from(String str) {
        if (!str.startsWith(NAME_PREFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'fixed' type format: " + str);

        if (Objects.isNull(matcher.group(1)))
            return Optional.of(DEFAULT);

        int mBits = Integer.parseInt(matcher.group(2));
        int nBits = Integer.parseInt(matcher.group(3));

        return Optional.of(mBits == nBits && CACHED_INSTANCES.containsKey(mBits) ?
                CACHED_INSTANCES.get(mBits) : new FixedType(mBits, nBits));
    }

    private final BigDecimal minValue;

    private final BigDecimal maxValue;

    private final NumericType numericType;

    public FixedType() {
        this(128, 128);
    }

    public FixedType(int bits) {
        this(bits, bits);
    }

    public FixedType(int mBits, int nBits) {
        super(mBits, nBits);

        numericType = new IntType(mBits + nBits);

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
        return String.format("fixed%dx%d", getMBits(), getNBits());
    }
}
