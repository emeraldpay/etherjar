/*
 * Copyright (c) 2011-2017 Infinitape Inc, All Rights Reserved.
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

        private BigInteger factor;

        /**
         * @param name a unit name
         * @param degreeOfTen a wei base multiplication factor expressed as a degree of power ten
         */
        Unit(String name, int degreeOfTen) {
            this.name = name;
            this.factor = BigInteger.TEN.pow(degreeOfTen);
        }

        /**
         * @return a unit name
         */
        public String getName() {
            return name;
        }

        /**
         * @return a wei base multiplication factor
         */
        public BigInteger getFactor() {
            return factor;
        }
    }

    /**
     * @param value amount in {@link Unit#ETHER}
     * @return corresponding amount in wei
     * @see #fromCustom(BigDecimal, Unit)
     */
    public static Wei fromEther(BigDecimal value) {
        return fromCustom(value, Unit.ETHER);
    }

    /**
     * @param value amount in some custom denomination {@link Unit}
     * @return corresponding amount in wei
     */
    public static Wei fromCustom(BigDecimal value, Unit unit) {
        return new Wei(value.multiply(new BigDecimal(unit.getFactor())).toBigInteger());
    }

    private BigInteger amount;

    /**
     * @param value an amount in wei
     */
    public Wei(BigInteger value) {
        this.amount = Objects.requireNonNull(value);
    }

    /**
     * @return an amount in wei
     */
    public BigInteger getAmount() {
        return amount;
    }

    /**
     * @return corresponding amount in {@link Unit#ETHER}
     * @see #toCustom(Unit)
     */
    public BigDecimal toEther() {
        return toCustom(Unit.ETHER);
    }

    /**
     * @return corresponding amount in custom denomination {@link Unit}
     */
    public BigDecimal toCustom(Unit unit) {
        return new BigDecimal(amount).divide(new BigDecimal(unit.factor), BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), amount.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        Wei other = (Wei) obj;

        return amount.equals(other.amount);
    }

    @Override
    public String toString() {
        return String.format("%s wei", amount.toString());
    }
}
