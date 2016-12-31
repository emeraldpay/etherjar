package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.util.OptionalInt;

/**
 * A Fixed-size or non-fixed-size reference type with a wrapped {@link Type}.
 *
 * @param <W> a wrapped {@link Type}
 *
 * @see StaticType
 * @see DynamicType
 */
public interface ReferenceType<T, W> extends Type<T> {

    @Override
    default <V> V visit(Visitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    default boolean isDynamic() {
        return getWrappedType().isDynamic() || !getLength().isPresent();
    }

    @Override
    default int getFixedSize() {
        return getLength().isPresent() ?
                getLength().getAsInt() * getWrappedType().getFixedSize() : Hex32.SIZE_BYTES;
    }

    /**
     * @return a wrapped {@link Type}
     */
    Type<W> getWrappedType();

    /**
     * @return a fixed number of wrapped {@link Type} instances as {@link OptionalInt} value,
     * or {@link OptionalInt#empty()} if this type has not fixed number of wrapped elements
     */
    OptionalInt getLength();
}
