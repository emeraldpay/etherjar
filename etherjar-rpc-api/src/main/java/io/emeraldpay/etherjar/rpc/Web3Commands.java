/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import org.jspecify.annotations.NullMarked;

/**
 * Web3 commands
 */
@NullMarked
public class Web3Commands {

    /**
     *
     * @return the current client version.
     */
    public RpcCall<String, String> clientVersion() {
        return RpcCall.create("web3_clientVersion");
    }

    /**
     * @param data input data to hash
     * @return Keccak-256 hash of the given data
     */
    public RpcCall<String, Hex32> sha3(HexData data) {
        return RpcCall.create("web3_sha3", data.toHex()).converted(Hex32.class, Hex32::from);
    }
}
