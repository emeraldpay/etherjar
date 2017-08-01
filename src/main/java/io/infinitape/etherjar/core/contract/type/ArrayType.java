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

package io.infinitape.etherjar.core.contract.type;

import io.infinitape.etherjar.core.HexData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fixed-size static array with given wrapped static type.
 */
public class ArrayType<T> implements StaticType<T[]> {

    final static String NAME_POSTFIX = "]";

    final static String EX_NAME_POSTFIX = "[]";

    final static Pattern NAME_PATTERN = Pattern.compile("(.+)\\[(\\d+)]");

    /**
     * Try to parse an {@link ArrayType} string representation (either canonical form or not).
     *
     * @param repo a {@link Type} parsers repository
     * @param str a string
     * @return an {@link ArrayType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if an {@link ArrayType} has invalid
     * input or not a {@link StaticType} wrapped type
     * @see #getCanonicalName()
     */
    @SuppressWarnings("unchecked")
    public static Optional<ArrayType> from(Type.Repository repo, String str) {
        if (!str.endsWith(NAME_POSTFIX) || str.endsWith(EX_NAME_POSTFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong array type format: " + str);

        Optional<Type> type = repo.search(matcher.group(1));

        if (!type.isPresent())
            throw new IllegalArgumentException(
                    "Unknown array wrapped type: " + matcher.group(1));

        if (type.get().isDynamic())
            throw new IllegalArgumentException(
                    "Array wrapped type is not static: " + type.get());

        String digits = matcher.group(2);

        return Optional.of(new ArrayType(
                (StaticType) type.get(), Integer.parseInt(digits)));
    }

    private final StaticType<T> type;

    private final int length;

    /**
     * Create an array with a fixed length.
     *
     * @param type an array wrapped {@link StaticType}
     * @param length a fixed number of array elements, should be positive
     */
    public ArrayType(StaticType<T> type, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("Illegal array length: " + length);

        this.type = Objects.requireNonNull(type);
        this.length = length;
    }

    public StaticType<T> getWrappedType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getCanonicalName() {
        return type.getCanonicalName() + '[' + length + ']';
    }

    @Override
    public int getFixedSize() {
        return getWrappedType().getFixedSize() * length;
    }

    @Override
    public HexData encode(T[] arr) {
        if (arr.length != length)
            throw new IllegalArgumentException("Wrong array length to encode: " + arr.length);

        List<HexData> buf = new ArrayList<>(arr.length);

        for (T obj : arr) {
            buf.add(getWrappedType().encode(obj));
        }

        return HexData.combine(buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] decode(HexData data) {
        if (data.getSize() != getFixedSize())
            throw new IllegalArgumentException("Wrong data length to decode array: " + data);

        HexData[] arr = data.split(getWrappedType().getFixedSize());

        return (T[]) Arrays.stream(arr).map(it -> getWrappedType().decode(it)).toArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), type, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        ArrayType other = (ArrayType) obj;

        return Objects.equals(type, other.type)
                && length == other.length;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
