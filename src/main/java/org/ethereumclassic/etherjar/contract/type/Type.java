package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

/**
 * A general type is used to convert java object to and from {@link Hex32} array.
 *
 * @param <T> the type of java object is needed to convert
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public interface Type<T> {

    /**
     * Number of bytes to store fixed-size offset of the beginning of dynamic tail part.
     *
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#formal-specification-of-the-encoding">Formal Specification of the Encoding</a>
     */
    int DYNAMIC_OFFSET_FIXED_SIZE_BYTES = Hex32.SIZE_BYTES;

    /**
     * @param <V> the type of result object
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern (Wikipedia)</a>
     */
    interface Visitor<V> {

        V visit(UInt uInt);
    }

    <V> V visit(Visitor<V> visitor);

    /**
     * Returns {@code true} if, and only if, current type is dynamic (non-fixed-size type).
     *
     * <p>Dynamic type has additionally a length as the first {@link Hex32} element.
     *
     * @return {@code true} if current type is dynamic, otherwise {@code false}
     */
    boolean isDynamic();

    /**
     * @return number of fixed-size bytes, for dynamic types it must be offset with {@link #DYNAMIC_OFFSET_FIXED_SIZE_BYTES}.
     * @see #isDynamic()
     */
    int getBytesFixedSize();

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
