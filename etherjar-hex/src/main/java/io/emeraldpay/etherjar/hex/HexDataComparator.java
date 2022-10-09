/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.hex;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Compares two <strong>same size</strong> HexData object by their byte representation.
 *
 * Most of the final types implements Comparable. This one is created as a separate class for intermediary
 * classes (i.e., HexData itself, and Hex32) because they cannot be Comparable to avoid an interface clash.
 */
public class HexDataComparator<T extends HexData> implements Comparator<T> {

    /**
     * Default instance. Can be shared and used anywhere.
     */
    public static final HexDataComparator<HexData> INSTANCE = HexData.COMPARATOR;

    /**
     * @param o1 left side of comparison
     * @param o2 right side of comparison
     * @return -1, 0 or 1 for &lt;, == or &gt;
     * @throws IllegalArgumentException if the compared values have different length
     */
    @Override
    public int compare(T o1, T o2) {
        if (o1.getSize() != o2.getSize()) {
            throw new IllegalArgumentException("Cannot compare HexData with different lengths. " + o1.getSize() + " and " + o2.getSize());
        }
        return Arrays.compareUnsigned(o1.getBytes(), o2.getBytes());
    }

}
