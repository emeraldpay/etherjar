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

package io.infinitape.etherjar.hex;

import java.math.BigInteger;

/**
 * Hex-encoded {@link String} to and from {@link java.math.BigInteger} mixin interface.
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
     * @see #toFullHex(BigInteger)
     */
    static String toHex(BigInteger num) {
        return toFullHex(num);
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Naked (without {@link #HEX_PREFIX}) hex-encoded {@link String}
     */
    static String toNakedHex(BigInteger num) {
        return num.toString(16);
    }

    /**
     * @param num {@link BigInteger} instance
     * @return Full (with {@link #HEX_PREFIX}) hex-encoded {@link String}
     * @see #HEX_PREFIX
     */
    static String toFullHex(BigInteger num) {
        return HEX_PREFIX.concat(num.toString(16));
    }
}
