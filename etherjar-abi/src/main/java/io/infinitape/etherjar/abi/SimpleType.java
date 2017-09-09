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

package io.infinitape.etherjar.abi;

import io.infinitape.etherjar.hex.Hex32;
import io.infinitape.etherjar.hex.HexData;

/**
 * Fixed-size elementary type (32 bytes length only).
 */
public interface SimpleType<T> extends StaticType<T> {

    @Override
    default int getFixedSize() {
        return Hex32.SIZE_BYTES;
    }

    @Override
    default HexData encode(T obj) {
        return encodeSimple(obj);
    }

    @Override
    default T decode(HexData data) {
        if (data.getSize() != getFixedSize())
            throw new IllegalArgumentException(
                    "Wrong hex data length to decode simple type: " + data.getSize());

        return decodeSimple(Hex32.from(data));
    }

    /**
     * Encode an object to a {@link Hex32}.
     *
     * @param obj an object
     * @return encoded hex
     * @see #decodeSimple(Hex32)
     */
    Hex32 encodeSimple(T obj);

    /**
     * Decode a {@link Hex32} to an object.
     *
     * @param hex32 a hex32
     * @return decoded object
     * @see #encodeSimple(Object)
     */
    T decodeSimple(Hex32 hex32);
}
