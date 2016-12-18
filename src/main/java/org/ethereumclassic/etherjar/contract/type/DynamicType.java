package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

/**
 * Non-fixed-size advanced types.
 *
 * @see StaticType
 * @see ReferenceType
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
    default long getFixedSize() {
        return Hex32.SIZE_BYTES;
    }
}
