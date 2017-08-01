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

package io.infinitape.etherjar.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Wei amount.
 */
public class Wei {

    /**
     * Wei denomination units.
     */
    public enum Unit {

        WEI("wei", 0),
        KWEI("Kwei", 3),
        MWEI("Mwei", 6),
        GWEI("Gwei", 9),
        SZABO("szabo", 12),
        FINNEY("finney", 15),
        ETHER("ether", 18),
        KETHER("Kether", 21),
        METHER("Mether", 24);

        private String name;

        private int scale;

        /**
         * @param name  a unit name
         * @param scale a wei base multiplication factor expressed as a degree of power ten
         */
        Unit(String name, int scale) {
            this.name = name;
            this.scale = scale;
        }

        /**
         * @return a unit name
         */
        public String getName() {
            return name;
        }

        /**
         * @return a wei base multiplication factor expressed as a degree of power ten
         */
        public int getScale() {
            return scale;
        }
    }

    public final static Wei ZERO = Wei.ZERO;

    /**
     * @param val amount in {@link Unit#ETHER}
     * @return corresponding amount in wei
     * @see #fromCustom(double, Unit)
     */
    public static Wei fromEther(double val) {
        return fromCustom(val, Unit.ETHER);
    }

    /**
     * @param num amount in {@link Unit#ETHER}
     * @return corresponding amount in wei
     * @see #fromCustom(BigDecimal, Unit)
     */
    public static Wei fromEther(BigDecimal num) {
        return fromCustom(num, Unit.ETHER);
    }

    /**
     * @param val amount in some custom denomination {@link Unit}
     * @return corresponding amount in wei
     */
    public static Wei fromCustom(double val, Unit unit) {
        return fromCustom(BigDecimal.valueOf(val), unit);
    }

    /**
     * @param num amount in some custom denomination {@link Unit}
     * @return corresponding amount in wei
     */
    public static Wei fromCustom(BigDecimal num, Unit unit) {
        return new Wei(num.scaleByPowerOfTen(unit.getScale()).toBigInteger());
    }

    private final BigInteger amount;

    /**
     * Create zero wei amount.
     */
    public Wei() {
        this(BigInteger.ZERO);
    }

    /**
     * @param val an amount in wei
     */
    public Wei(long val) {
        this(BigInteger.valueOf(val));
    }

    /**
     * @param num an amount in wei
     */
    public Wei(BigInteger num) {
        this.amount = Objects.requireNonNull(num);
    }

    /**
     * @return an amount in wei
     */
    public BigInteger getAmount() {
        return amount;
    }

    /**
     * @param scale scale of the {@code BigDecimal} value to be returned
     * @return corresponding amount in {@link Unit#ETHER}
     * @see #toCustom(Unit, int)
     */
    public BigDecimal toEther(int scale) {
        return toCustom(Unit.ETHER, scale);
    }

    /**
     * @return corresponding amount in {@link Unit#ETHER}
     * @see #toCustom(Unit)
     */
    public BigDecimal toEther() {
        return toCustom(Unit.ETHER);
    }

    /**
     * @param scale scale of the {@code BigDecimal} value to be returned
     * @return corresponding amount in custom denomination {@link Unit}
     * @see #toCustom(Unit, int)
     */
    public BigDecimal toCustom(Unit unit, int scale) {
        return toCustom(unit).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * @return corresponding amount in custom denomination {@link Unit}
     * @see #toCustom(Unit)
     */
    public BigDecimal toCustom(Unit unit) {
        return new BigDecimal(amount).scaleByPowerOfTen(-unit.getScale());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), Objects.hashCode(amount));
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Wei)) return false;

        Wei other = (Wei) obj;

        return Objects.equals(amount, other.amount);
    }

    @Override
    public String toString() {
        return String.format("%s wei", amount.toString());
    }
}
