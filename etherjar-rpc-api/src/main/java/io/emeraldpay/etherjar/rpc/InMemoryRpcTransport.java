/*
 * Copyright (c) 2023 EmeraldPay Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.etherjar.rpc;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * An implementation for the {@link RpcTransport} that uses a provided <code>Function&lt;String, InputStream&gt;</code> to get a response.
 * The function may respond with a hard-coded values, or dispatch the call to actual RPC. It supposed to be used in a testing environment.
 */
@NullMarked
public class InMemoryRpcTransport extends AbstractRpcTransport {

    private final Function<String, InputStream> delegate;

    protected InMemoryRpcTransport(Function<String, InputStream> delegate, ExecutorService executorService, RpcConverter rpcConverter) {
        super(executorService, rpcConverter);
        this.delegate = delegate;
    }

    static public Builder newBuilder() {
        return new Builder();
    }

    @Override
    public InputStream execute(String json) throws IOException {
        return delegate.apply(json);
    }

    @Override
    public void close() throws IOException {
    }

    public static class Builder {
        @Nullable
        private Function<String, InputStream> delegate;
        @Nullable
        private ExecutorService executorService;
        @Nullable
        private RpcConverter rpcConverter;

        public Builder respondWithInputStream(Function<String, InputStream> delegate) {
            this.delegate = delegate;
            return this;
        }

        public Builder respondWithBytes(Function<String, byte[]> delegate) {
            return respondWithInputStream(
                delegate.andThen(ByteArrayInputStream::new)
            );
        }

        public Builder respondWithString(Function<String, String> delegate) {
            return respondWithBytes(
                delegate.andThen((s) -> s.getBytes(StandardCharsets.UTF_8))
            );
        }

        /**
         * By default, it uses a Single Thread executor, but may be configured with other types of executor.
         *
         * @param executorService executor for RPC Transport
         * @return self
         */
        public Builder withExecutor(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * By default, it uses standard {@link JacksonRpcConverter}, but may be configured with another implementation
         *
         * @param rpcConverter converter to write/read RPC JSON
         * @return self
         */
        public Builder withRpcConverter(RpcConverter rpcConverter) {
            this.rpcConverter = rpcConverter;
            return this;
        }

        public InMemoryRpcTransport build() {
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
            }
            if (rpcConverter == null) {
                rpcConverter = new JacksonRpcConverter();
            }
            Objects.requireNonNull(delegate);

            return new InMemoryRpcTransport(delegate, executorService, rpcConverter);
        }
    }
}
