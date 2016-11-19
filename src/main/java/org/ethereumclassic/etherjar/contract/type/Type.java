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
     * @param <V> the type of result object
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern (Wikipedia)</a>
     */
    interface Visitor<V> {

        V visit(UInt uInt);
    }

    /**
     * Returns {@code true} if, and only if, current type is dynamic (non-fixed-size type).
     *
     * <p>Dynamic type has additionally a length as the first {@link Hex32} element.
     *
     * @return {@code true} if current type is dynamic, otherwise {@code false}
     */
    boolean isDynamic();

    /**
     * @return number of bytes, only for static (not dynamic, fixed-size) types.
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

    <V> V visit(Visitor<V> visitor);
}
