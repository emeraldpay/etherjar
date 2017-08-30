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

import io.infinitape.etherjar.domain.Address;
import io.infinitape.etherjar.domain.Hex32;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Address type, equivalent to 'uint160'.
 *
 * @see Address
 */
public class AddressType implements SimpleType<Address> {

    public final static AddressType DEFAULT = new AddressType();

    final static int OFFSET_BYTES = Hex32.SIZE_BYTES - Address.SIZE_BYTES;

    /**
     * Try to parse a {@link AddressType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link AddressType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is {@code null}
     * @see #getCanonicalName()
     */
    public static Optional<AddressType> from(String str) {
        Objects.requireNonNull(str);

        if (!Objects.equals(str, "address"))
            return Optional.empty();

        return Optional.of(DEFAULT);
    }

    @Override
    public String getCanonicalName() { return "address"; }

    @Override
    public Hex32 encodeSimple(Address obj) {
        byte[] buf = new byte[Hex32.SIZE_BYTES];

        System.arraycopy(obj.getBytes(), 0, buf, OFFSET_BYTES, Address.SIZE_BYTES);

        return new Hex32(buf);
    }

    @Override
    public Address decodeSimple(Hex32 hex32) {
        byte[] buf = Arrays.copyOfRange(hex32.getBytes(), OFFSET_BYTES, Hex32.SIZE_BYTES);

        return Address.from(buf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        return Objects.equals(getClass(), obj.getClass());
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
