package org.ethereumclassic.etherjar.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Lazy related util methods.
 */
public interface Lazy {

    /**
     * Memoize a given {@link Supplier} value.
     *
     * <pre>{@code
     * Supplier<String> str = Lazy.wrap(
     *          () -> String.format("%d", a delayed calculation));
     * }</pre>
     *
     * @param supplier a value supplier
     * @param <T> a java object type is needed to store
     * @return a lazy cached supplier value
     * @throws NullPointerException if a supplier value is {@code null}
     */
    static <T> Supplier<T> wrap(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);

        return new Supplier<T>() {

            private volatile T cached;

            @Override
            public T get() {
                if (cached == null) {
                    synchronized (this) {
                        if (cached == null) {
                            cached = Objects.requireNonNull(supplier.get());
                        }
                    }
                }

                return cached;
            }
        };
    }
}
