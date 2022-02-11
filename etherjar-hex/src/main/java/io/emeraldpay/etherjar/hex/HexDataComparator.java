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

import java.util.Comparator;

/**
 * Compares two <strong>same size</strong> HexData object by their byte representation.
 * Can be used for Hex32, TransactionId, BlockHash, MethodId and other types of HexData.
 */
public class HexDataComparator implements Comparator<HexData> {

    /**
     * Default instance. Can be shared and used anywhere.
     */
    public static final HexDataComparator INSTANCE = new HexDataComparator();

    /**
     * @param o1 left side of comparison
     * @param o2 right side of comparison
     * @return -1, 0 or 1 for &lt;, == or &gt;
     * @throws IllegalArgumentException if the compared values have different length
     */
    @Override
    public int compare(HexData o1, HexData o2) {
        if (o1.getSize() != o2.getSize()) {
            throw new IllegalArgumentException("Cannot compare HexData with different lengths. " + o1.getSize() + " and " + o2.getSize());
        }
        byte[] val1 = o1.getBytes();
        byte[] val2 = o2.getBytes();
        for (int i = 0; i < val1.length; i++) {
            // convert to _unsigned_ value
            int a = val1[i] & 0xff;
            int b = val2[i] & 0xff;
            if (a < b) {
                return -1;
            }
            if (a > b) {
                return 1;
            }
        }
        return 0;
    }

}
