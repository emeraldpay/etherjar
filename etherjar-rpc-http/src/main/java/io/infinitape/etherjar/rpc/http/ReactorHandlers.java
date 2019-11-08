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
package io.infinitape.etherjar.rpc.http;

import io.infinitape.etherjar.rpc.RpcException;
import io.infinitape.etherjar.rpc.RpcResponseError;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Common functions to process Reactor triggers
 */
public class ReactorHandlers {

    /**
     * Catch and transform ConnectionException into RpcException. Original message from ConnectionException
     * is put into details as {message: ...}
     *
     * @param <T> any type expected by actual subscription
     * @return function
     */
    static <T> Function<ConnectException, Publisher<T>> catchConnectException() {
        return (t) -> {
            Map<String, String> details = new HashMap<>();
            details.put("message", t.getMessage());
            return Mono.error(new RpcException(RpcResponseError.CODE_UPSTREAM_CONNECTION_ERROR, "Connection error", details));
        };
    }
}
