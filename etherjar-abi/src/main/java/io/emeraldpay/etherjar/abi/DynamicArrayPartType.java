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
package io.emeraldpay.etherjar.abi;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

/**
 * DynamicArray as a part of a larger data blob
 *
 * @param <T> type of data
 */
public class DynamicArrayPartType<T> extends DynamicArrayType<T> {
    /**
     * Create a dynamic array for predefined {@link StaticType}.
     *
     * @param type an array wrapped {@link StaticType}
     */
    public DynamicArrayPartType(StaticType<T> type) {
        super(type);
    }

    @Override
    public T[] decode(HexData data) {
        int len = Type.decodeLength(
            data.extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        int size = Hex32.SIZE_BYTES + getWrappedType().getFixedSize() * len;

        if (data.getSize() < size)
            throw new IllegalArgumentException("Wrong data length to decode dynamic array: " + data);

        return super.decode(data.extract(size));
    }
}
