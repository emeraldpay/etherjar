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

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class BoolType extends NumericType {

    public final static BigInteger FALSE = BigInteger.ZERO;

    public final static BigInteger TRUE = BigInteger.ONE;

    public final static BoolType DEFAULT = new BoolType();

    /**
     * Try to parse a {@link BoolType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link BoolType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is {@code null}
     * @see #getCanonicalName()
     */
    public static Optional<BoolType> from(String str) {
        Objects.requireNonNull(str);

        if (!Objects.equals(str, "bool"))
            return Optional.empty();

        return Optional.of(DEFAULT);
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
