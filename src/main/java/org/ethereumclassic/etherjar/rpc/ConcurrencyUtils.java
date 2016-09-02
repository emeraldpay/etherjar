package org.ethereumclassic.etherjar.rpc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Concurrency related utils, similar to classes found in Java 8
 *
 * @author Igor Artamonov
 */
public class ConcurrencyUtils {

    public interface Function<F,T> {
        T apply(F value);
    }

    public static class FutureMap<F,T> implements Future<T> {

        private final Future<F> future;
        private final Function<F,T> function;

        public FutureMap(Future<F> future, Function<F, T> function) {
            this.future = future;
            this.function = function;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return function.apply(future.get());
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return function.apply(future.get(timeout, unit));
        }
    }

    public static class CompletedFuture<T> implements Future<T> {

        private final T result;

        public CompletedFuture(final T result) {
            this.result = result;
        }

        @Override
        public boolean cancel(final boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return this.result;
        }

        @Override
        public T get(final long l, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
