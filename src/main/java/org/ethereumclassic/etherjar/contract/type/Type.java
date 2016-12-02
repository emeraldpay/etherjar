package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A general type is used to convert java object to and from {@link Hex32} array.
 *
 * <p>Immutable arbitrary-precision types, with provided thread safety guarantees.
 *
 * @param <T> the type of java object is needed to convert
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public interface Type<T> {

    @FunctionalInterface
    interface Repository {

        /**
         * Search appropriate {@link Type} instance for a given {@link String}.
         *
         * @param str a {@link Type} string representation (either canonical or not)
         * @return a {@link Type} instance is packed as {@link Optional} value,
         * or {@link Optional#empty()} instead
         */
        @SuppressWarnings("unchecked")
        default Optional<Type> search(String str) {
            return getAllTypes().stream().map(t -> (Optional<Type>) t.parse(str))
                    .filter(Optional::isPresent).map(Optional::get).findFirst();
        }

        /**
         * Get all existing {@link Type}.
         *
         * @return a list of all existing {@link Type}
         */
        List<Type> getAllTypes();
    }

    Type[] ALL_TYPES = new Type[] { new UIntType(), new IntType(), new BoolType() };

    Repository REPOSITORY = () -> Arrays.asList(ALL_TYPES);

    /**
     * Find appropriate {@link Type} instance for a given {@link String}.
     *
     * @param str a {@link Type} string representation (either canonical or not)
     * @return a {@link Type} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @see Repository#search(String)
     */
    static Optional<Type> from(String str) {
        return REPOSITORY.search(str);
    }

    /**
     * @param <V> the type of result object
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern (Wikipedia)</a>
     */
    interface Visitor<V> {

        V visit(NumericType type);

        V visit(UIntType type);

        V visit(IntType type);

        V visit(BoolType type);
    }

    /**
     * {@link Visitor} default implementation.
     *
     * @param <V> the type of result object
     * @see Visitor
     */
    class VisitorImpl<V> implements Type.Visitor<V> {

        @Override
        public V visit(NumericType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V visit(UIntType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V visit(IntType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V visit(BoolType type) {
            throw new UnsupportedOperationException();
        }
    }

    <V> V visit(Visitor<V> visitor);

    /**
     * Try to parse a {@link Type} string representation to an {@link Type} instance.
     *
     * @return a {@link Type} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if a detected {@link Type} has invalid input
     */
    Optional<? extends Type<T>> parse(String str);

    /**
     * Get type's canonical string representation
     *
     * @return a string
     */
    String getName();

    /**
     * Returns {@code true} if, and only if, current type is dynamic (non-fixed-size type).
     *
     * <p>Dynamic type has additionally a length as the first {@link Hex32} element.
     *
     * @return {@code true} if current type is dynamic, otherwise {@code false}
     */
    boolean isDynamic();

    /**
     * @return number of fixed-size bytes, or {@link Hex32#SIZE_BYTES} offset for dynamic types
     * @see #isDynamic()
     */
    int getEncodedSize();

    /**
     * Encode an object to {@link Hex32} array.
     *
     * @param obj an object
     * @return encoded call
     */
    Hex32[] encode(T obj);

    /**
     * Decode {@link Hex32} to an object.
     *
     * @param data a hex data
     * @return encoded call
     */
    T decode(Hex32[] data);
}
