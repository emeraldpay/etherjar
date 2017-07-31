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

package io.infinitape.etherjar.contract.type;

import io.infinitape.etherjar.core.Hex32;
import io.infinitape.etherjar.core.HexData;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A general type is used to convert java object to and from {@link Hex32} array.
 *
 * <p>Immutable arbitrary-precision types, with provided thread safety guarantees
 *
 * @param <T> a java object type is needed to convert
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public interface Type<T> {

    /**
     * A {@link Type} instances repository.
     *
     * <p>Here are some examples of how type repositories can be created:
     *
     * <pre>{@code
     * Type.Repository repo1 = () ->
     *          Arrays.asList(UIntType::from, IntType::from, ...)
     *
     * Type.Repository repo2 = ((Repository) () ->
     *          Arrays.asList(UIntType::from, IntType::from, ...))
     *                  .append(ArrayType::from);
     * }</pre>
     */
    @FunctionalInterface
    interface Repository {

        /**
         * Search appropriate {@link Type} instance for a given {@link String}.
         *
         * @param str a {@link Type} string representation (either canonical or not)
         * @return a {@link Type} instance is packed as {@link Optional} value,
         * or {@link Optional#empty()} instead
         * @see #getTypeParsers()
         */
        @SuppressWarnings("unchecked")
        default Optional<Type> search(String str) {
            if (str.isEmpty())
                throw new IllegalArgumentException("Empty type string representation");

            return getTypeParsers().stream()
                    .map(t -> (Optional<Type>) t.apply(str))
                    .filter(Optional::isPresent).map(Optional::get).findFirst();
        }

        /**
         * Append a complex type parser to current repository.
         *
         * <p>A repository as a first function parameter will be fixed to the current call state.
         * Subsequent parsers will not be included and therefore used by this complex type parser.
         *
         * <pre>{@code
         * Type.Repository repo = ((Repository) () ->
         *          Arrays.asList(parser1, parser2, parser3))
         *                  .append(complexParser1).append(complexParser2);
         * }</pre>
         *
         * <p>In this example <code>complexParser1</code> don't know anything about
         * <code>complexParse2</code>, but it's not right visa versa.
         *
         * @param parser a {@link BiFunction} complex type parser
         * @return an extended type repository
         * @see #append(Function)
         * @see #getTypeParsers()
         */
        default Repository append(BiFunction<Repository, String, Optional<? extends Type>> parser) {
            Supplier<Repository> suppl = () -> append(parser);

            return append(str -> parser.apply(suppl.get(), str));
        }

        /**
         * Append a simple type parser to current repository.
         *
         * @param parser a {@link BiFunction} simple type parser
         * @return an extended type repository
         * @see #append(BiFunction)
         * @see #getTypeParsers()
         */
        default Repository append(Function<String, Optional<? extends Type>> parser) {
            Objects.requireNonNull(parser);

            List<Function<String, Optional<? extends Type>>> list =
                    Stream.concat(getTypeParsers().stream(), Stream.of(parser))
                            .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));

            return () -> list;
        }

        /**
         * Get {@link Type} string parsers in order to build an appropriate {@link Type} instances.
         *
         * <p>Parser can throw {@link NullPointerException} if a {@code str} is <code>null</code>.
         *
         * <p>Parser can throw {@link IllegalArgumentException} if a {@link Type} has invalid input.
         *
         * @return a list of {@link Type} parsers
         */
        List<Function<String, Optional<? extends Type>>> getTypeParsers();
    }

    /**
     * Find appropriate {@link Type} instance for a given {@link String}.
     *
     * @param repo a {@link Repository} a repository of {@link Type} parsers
     * @param str a {@link Type} string representation (either canonical or not)
     * @return a {@link Type} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @see Repository#search(String)
     */
    static Optional<Type> search(Repository repo, String str) {
        return repo.search(str);
    }

    /**
     * Encode a {@link DynamicType} length.
     *
     * @param val a length
     * @return a length encoded as {@link Hex32}
     * @see #encodeLength(BigInteger)
     * @see #decodeLength(Hex32)
     */
    static Hex32 encodeLength(long val) {
        return encodeLength(BigInteger.valueOf(val));
    }

    /**
     * Encode a {@link DynamicType} length.
     *
     * @param val a {@link BigInteger} instance
     * @return a length encoded as {@link Hex32}
     * @see #encodeLength(long)
     * @see #decodeLength(Hex32)
     */
    static Hex32 encodeLength(BigInteger val) {
        return new UIntType().encodeSimple(val);
    }

    /**
     * Decode a {@link DynamicType} length.
     *
     * @param hex32 a length encoded as {@link Hex32}
     * @return a {@link BigInteger} instance
     * @see #encodeLength(long)
     * @see #encodeLength(BigInteger)
     */
    static BigInteger decodeLength(Hex32 hex32) {
        return new UIntType().decodeSimple(hex32);
    }

    /**
     * Returns {@code true} if, and only if, current type is static (fixed-size type).
     *
     * @return {@code true} if current type is static, otherwise {@code false}
     * @see #isDynamic()
     */
    default boolean isStatic() {
        return !isDynamic();
    }

    /**
     * @param <V> the type of result object
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern (Wikipedia)</a>
     */
    interface Visitor<V> {

        <T> V visit(StaticType<T> type);

        <T> V visit(DynamicType<T> type);
    }

    /**
     * {@link Visitor} default implementation.
     *
     * @param <V> the type of result object
     * @see Visitor
     */
    class VisitorImpl<V> implements Type.Visitor<V> {

        @Override
        public <T> V visit(StaticType<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> V visit(DynamicType<T> type) {
            throw new UnsupportedOperationException();
        }
    }

    <V> V visit(Visitor<V> visitor);

    /**
     * Get type's canonical string representation
     *
     * @return a string
     */
    String getCanonicalName();

    /**
     * Returns {@code true} if, and only if, current type is dynamic (non-fixed-size type).
     *
     * <p>Dynamic type has additionally a length as the first {@link Hex32} element.
     *
     * @return {@code true} if current type is dynamic, otherwise {@code false}
     * @see #isStatic()
     */
    boolean isDynamic();

    /**
     * Get a fixed-size bytes required by the head part encoding.
     *
     * @return number of fixed-size bytes for static types, or
     * {@link Hex32#SIZE_BYTES} offset for dynamic types
     *
     * @see #isDynamic()
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#formal-specification-of-the-encoding">Formal Specification of the Encoding</a>
     */
    int getFixedSize();

    /**
     * Encode an object to a hex data.
     *
     * @param obj an object
     * @return an encoded hex data
     */
    HexData encode(T obj);

    /**
     * Decode a hex data to an object.
     *
     * @param data an encoded hex data
     * @return a decoded object
     */
    T decode(HexData data);
}
