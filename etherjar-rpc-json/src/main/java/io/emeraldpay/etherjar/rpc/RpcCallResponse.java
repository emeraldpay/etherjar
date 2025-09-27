/*
 * Copyright (c) 2016-2019 Igor Artamonov
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class RpcCallResponse<JS, RES> {

    @NonNull
    private final RpcCall<JS, RES> source;
    @Nullable
    private RES value;
    @Nullable
    private RpcException error;

    public RpcCallResponse(@NonNull RpcCall<JS, RES> source, @Nullable RES value) {
        this.source = source;
        this.value = value;
    }

    public RpcCallResponse(@NonNull RpcCall<JS, RES> source, @NonNull RpcException error) {
        this.source = source;
        this.error = error;
    }

    @NonNull
    public RpcCall<JS, RES> getSource() {
        return source;
    }

    @Nullable
    public RES getValue() {
        return value;
    }

    @Nullable
    public RpcException getError() {
        return error;
    }

    public boolean isSuccessful() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <T> RpcCallResponse<JS, T> cast(Class<T> clazz) {
        if (value == null || clazz.isAssignableFrom(value.getClass())) {
            return (RpcCallResponse<JS, T>) this;
        }
        throw new ClassCastException("Value of " + value.getClass() + " is not assignable to " + clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcCallResponse<?, ?> that = (RpcCallResponse<?, ?>) o;
        return Objects.equals(source, that.source) &&
            Objects.equals(value, that.value) &&
            Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, error);
    }
}
