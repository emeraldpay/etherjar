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

import org.jspecify.annotations.NullMarked;

/**
 * Common RPC commands
 */
@NullMarked
public class Commands {

    private static final EthCommands eth = new EthCommands();
    private static final Web3Commands web3 = new Web3Commands();
    private static final ParityCommands parity = new ParityCommands();
    private static final NetCommands net = new NetCommands();

    /**
     *
     * @return standard RPC commands with eth_ prefix
     */
    public static EthCommands eth() {
        return eth;
    }

    /**
     *
     * @return standard RPC commands with web3_ prefix
     */
    public static Web3Commands web3() {
        return web3;
    }

    /**
     *
     * @return standard RPC commands with net_ prefix
     */
    public static NetCommands net() {
        return net;
    }

    /**
     *
     * @return commands specific for Parity Ethereum client
     */
    public static ParityCommands parity() {
        return parity;
    }

}
