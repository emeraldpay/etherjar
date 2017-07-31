package io.infinitape.etherjar.contract.type;

import io.infinitape.etherjar.core.Hex32;

/**
 * Non-fixed-size dynamic type.
 *
 * @see StaticType
 */
public interface DynamicType<T> extends Type<T> {

    @Override
    default <V> V visit(Visitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    default boolean isDynamic() {
        return true;
    }

    @Override
    default int getFixedSize() {
        return Hex32.SIZE_BYTES;
    }
}
