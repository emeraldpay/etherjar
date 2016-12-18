package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Fixed-size elementary types.
 *
 * @see DynamicType
 * @see ReferenceType
 */
public interface StaticType<T> extends Type<T> {

    @Override
    default <V> V visit(Visitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    default boolean isDynamic() {
        return false;
    }

    @Override
    default long getFixedSize() {
        return Hex32.SIZE_BYTES;
    }

    @Override
    default List<? extends Hex32> encode(T obj) {
        return Collections.singletonList(encodeSingle(obj));
    }

    @Override
    default T decode(Collection<? extends Hex32> data) {
        if (data.size() != 1)
            throw new IllegalArgumentException("Not single data length to decode: " + data.size());

        return decodeSingle(data.iterator().next());
    }

    /**
     * Encode an object to a single {@link Hex32}.
     *
     * @param obj an object
     * @return encoded hex
     *
     * @see #encode(Object)
     */
    Hex32 encodeSingle(T obj);

    /**
     * Decode a single {@link Hex32} to an object.
     *
     * @param hex32 a hex32
     * @return decoded object
     *
     * @see #decode(Hex32[])
     */
    T decodeSingle(Hex32 hex32);
}
