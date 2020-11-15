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

public class NetCommands {

    /**
     *
     * @return call for the current network id
     */
    public RpcCall<String, Integer> version() {
        return RpcCall.create("net_version").converted(Integer.class, Integer::valueOf);
    }

    /**
     * Returns true if client is actively listening for network connections
     *
     * @return call for net_listening
     */
    public RpcCall<Boolean, Boolean> listening() {
        return RpcCall.create("net_listening", Boolean.class);
    }

    /**
     * Returns number of peers currently connected to the client.
     *
     * @return call for net_peerCount
     */
    public RpcCall<String, Integer> peerCount() {
        return RpcCall.create("net_peerCount").converted(Integer.class, Integer::decode);
    }

}
