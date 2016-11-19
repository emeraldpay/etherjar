package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.HexData;

/**
 * A general type is used to convert java object to and from {@link HexData}.
 *
 * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">Ethereum Contract ABI</a>
 */
public interface Type<T> {

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern (Wikipedia)</a>
     */
    interface Visitor<R> {

        R visit(MethodType method);
    }

    /**
     * Returns {@code true} if, and only if, current type is dynamic.
     *
     * @return {@code true} if current type is dynamic, otherwise {@code false}
     */
    boolean isDynamic();

    /**
     * Encode an object to {@link HexData}
     *
     * @param obj an object
     * @return encoded call
     */
    HexData encode(T obj);

    /**
     * Decode {@link HexData} to an object
     *
     * @param data a hex data
     * @return encoded call
     */
    T decode(HexData data);

    <R> R visit(Visitor<R> visitor);
}
