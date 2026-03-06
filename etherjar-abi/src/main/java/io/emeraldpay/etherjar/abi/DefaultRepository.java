/*
 * Copyright (c) 2026 EmeraldPay Ltd, All Rights Reserved.
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
package io.emeraldpay.etherjar.abi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class DefaultRepository {

    private DefaultRepository() {
    }

    /**
     * Lazy holder ensures repository initialization happens only on the first call.
     */
    private static class Holder {
        private static final Type.Repository INSTANCE = create();
    }

    public static Type.Repository getInstance() {
        return Holder.INSTANCE;
    }

    private static Type.Repository create() {
        final Type.Repository[] self = new Type.Repository[1];

        List<Function<String, Optional<? extends Type>>> parsers = Collections.unmodifiableList(Arrays.asList(
                // Array parsers go first so names like "uint[]" are not consumed by simple numeric parsers.
                str -> ArrayType.from(self[0], str),
                str -> DynamicArrayType.from(self[0], str),
                BoolType::from,
                UIntType::from,
                IntType::from,
                AddressType::from,
                FunctionType::from,
                FixedType::from,
                UFixedType::from,
                DynamicBytesType::from,
                StringType::from,
                BytesType::from
        ));

        self[0] = () -> parsers;

        return self[0];
    }
}
