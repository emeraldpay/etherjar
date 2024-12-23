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

import com.fasterxml.jackson.databind.JavaType;

import java.util.HashMap;
import java.util.Map;

public class BatchCallContext<T extends BatchItem> {
    private final Map<Integer, T> sourceMapping = new HashMap<>();
    private final Map<Integer, JavaType> jsonTypes = new HashMap<>();
    private final Map<RpcCall, T> callMapping = new HashMap<>();

    public int add(T item) {
        int current = item.getId();
        sourceMapping.put(current, item);
        jsonTypes.put(current, item.getCall().getJsonType());
        callMapping.put(item.getCall(), item);
        return current;
    }

    public Map<Integer, JavaType> getJsonTypes() {
        return jsonTypes;
    }

    public RpcCall getCall(int id) {
        return sourceMapping.get(id).getCall();
    }

    public T getBatchItem(RpcCall call) {
        return callMapping.get(call);
    }
}
