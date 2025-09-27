/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.domain;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import org.bouncycastle.util.encoders.Hex;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Objects;

@NullMarked
public class BlockHash extends Hex32 implements Comparable<BlockHash> {

    public static final int SIZE_BYTES = 32;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public BlockHash(byte[] value) {
        super(value);
    }

    /**
     * Create a block hash from its byte representation. The array must be 32-bytes long.
     *
     * @param value byte value
     * @return BlockHash
     */
    public static BlockHash from(byte[] value) {
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Block Hash length: " + value.length);
        }
        return new BlockHash(value);
    }

    /**
     * Create a block hash from its Hex32 representation.
     *
     * @param value Hex32 value
     * @return BlockHash
     */
    public static BlockHash from(Hex32 value) {
        Objects.requireNonNull(value);
        return new BlockHash(value.getBytes());
    }

    /**
     * Create a block hash from its hex representation. The string must start with 0x and contain 64 hexadecimal symbols after that (i.e., 66 in total)
     *
     * @param value byte value
     * @return BlockHash
     */
    public static BlockHash from(String value) {
        Objects.requireNonNull(value);
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Block Hash length: " + value.length());
        }
        return new BlockHash(HexData.from(value).getBytes());
    }

    /**
     *
     * @return an empty block hash with zeros only
     */
    public static BlockHash empty() {
        return new BlockHash(new byte[SIZE_BYTES]);
    }

    @Override
    public int compareTo(BlockHash o) {
        return Arrays.compareUnsigned(this.getBytes(), o.getBytes());
    }
}
