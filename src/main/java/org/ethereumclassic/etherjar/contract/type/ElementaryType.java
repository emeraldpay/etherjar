package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

public interface ElementaryType<T> extends Type<T> {

    @Override
    default <V> V visit(Visitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    default boolean isDynamic() {
        return false;
    }

    @Override
    default int getEncodedSize() {
        return Hex32.SIZE_BYTES;
    }

    @Override
    default Hex32[] encode(T obj) {
        return new Hex32[] { singleEncode(obj) };
    }

    @Override
    default T decode(Hex32[] data) {
        if (data.length == 0 || data.length > 1)
            throw new IllegalArgumentException("Not single input data length: " + data.length);

        return singleDecode(data[0]);
    }

    /**
     * Encode an object to a single {@link Hex32}.
     *
     * @param obj an object
     * @return encoded hex
     * @see #encode(Object)
     */
    Hex32 singleEncode(T obj);

    /**
     * Decode a single {@link Hex32} to an object.
     *
     * @param hex32 a hex data
     * @return decoded object
     * @see #decode(Hex32[])
     */
    T singleDecode(Hex32 hex32);
}
