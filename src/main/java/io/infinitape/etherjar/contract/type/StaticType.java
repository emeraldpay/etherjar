package io.infinitape.etherjar.contract.type;

/**
 * A fixed-size static type.
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
