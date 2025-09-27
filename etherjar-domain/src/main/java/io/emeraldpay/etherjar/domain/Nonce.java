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


import io.emeraldpay.etherjar.hex.HexData;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Nonce extends HexData implements Comparable<Nonce> {

    public static final int SIZE_BYTES = 8;
    public static final int SIZE_HEX = 2 + SIZE_BYTES * 2;

    public Nonce(byte[] value) {
        super(value, SIZE_BYTES);
    }

    public static Nonce from(byte[] value) {
        if (value.length != SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid Nonce length: " + value.length);
        }
        return new Nonce(value);
    }

    public static Nonce from(String value) {
        if (value.length() != SIZE_HEX) {
            throw new IllegalArgumentException("Invalid Nonce length: " + value.length());
        }
        return new Nonce(HexData.from(value).getBytes());
    }

    @Override
    public int compareTo(Nonce o) {
        return this.asQuantity().compareTo(o.asQuantity());
    }
}
