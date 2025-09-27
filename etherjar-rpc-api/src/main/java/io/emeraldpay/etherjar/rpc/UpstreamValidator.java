/*
 * Copyright (c) 2016-2018 Infinitape Inc, All Rights Reserved.
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

/**
 *  Validates an upstream URI to conform requirements of the current app
 *
 * @author Igor Artamonov
 */
@NullMarked
public interface UpstreamValidator {

    /**
     * Validates an upstream URI to conform requirements of the current app
     *
     * @param uri upstream JSON RPC host:port
     * @return true if valid
     */
    boolean validate(FuturesRpcClient uri);

}
