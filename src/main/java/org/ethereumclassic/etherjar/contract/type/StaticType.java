package org.ethereumclassic.etherjar.contract.type;

/**
 * A Fixed-size or non-fixed-size reference type with a wrapped {@link Type}.
 *
 * @param <W> a wrapped {@link Type}
 *
 * @see DynamicType
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
}
