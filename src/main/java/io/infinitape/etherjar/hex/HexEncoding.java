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

package io.infinitape.etherjar.hex;

import java.math.BigInteger;

/**
 * Hex-encoded {@link String} for {@link java.math.BigInteger} instances.
 */
public interface HexEncoding {

    /**
     * The prefix used for a full hex encoding format.
     */
    String HEX_PREFIX = "0x";

    /**
     * @param hex hex-encoded {@link String} with optional {@link #HEX_PREFIX}
     * @return {@link BigInteger} instance
     * @see #HEX_PREFIX
     */
    static BigInteger fromHex(String hex) {
        return new BigInteger(
            hex.startsWith(HEX_PREFIX) ?
                hex.substring(HEX_PREFIX.length()) : hex, 16);
    }

    /**
     * @param val a long value
     * @return Hex-encoded {@link String} with {@link #HEX_PREFIX}
     * and padded with zero to an even number of digits
     * @see #HEX_PREFIX
     */
    static String toHex(long val) {
        String str = toNakedHex(val);

        return HEX_PREFIX.concat(str.length() % 2 == 0 ? "" : "0").concat(str);
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Hex-encoded {@link String} with {@link #HEX_PREFIX}
     * and padded with zero to an even number of digits
     * @see #HEX_PREFIX
     */
    static String toHex(BigInteger num) {
        String str = toNakedHex(num);

        return HEX_PREFIX.concat(str.length() % 2 == 0 ? "" : "0").concat(str);
    }

    /**
     * @param val a long value
     * @return Naked hex-encoded {@link String} without {@link #HEX_PREFIX}
     * @see #toHex(long)
     */
    static String toNakedHex(long val) {
        return Long.toHexString(val);
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Naked hex-encoded {@link String} without {@link #HEX_PREFIX}
     * @see #toHex(BigInteger)
     */
    static String toNakedHex(BigInteger num) {
        return num.toString(16);
    }
}
