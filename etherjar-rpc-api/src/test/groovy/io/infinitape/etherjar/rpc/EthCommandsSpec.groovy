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
package io.infinitape.etherjar.rpc

import io.infinitape.etherjar.domain.Address
import io.infinitape.etherjar.domain.BlockHash
import io.infinitape.etherjar.domain.Nonce
import io.infinitape.etherjar.domain.TransactionId
import io.infinitape.etherjar.domain.Wei
import io.infinitape.etherjar.hex.Hex32
import io.infinitape.etherjar.hex.HexData
import io.infinitape.etherjar.rpc.json.BlockJson
import io.infinitape.etherjar.rpc.json.BlockTag
import io.infinitape.etherjar.rpc.json.TransactionCallJson
import io.infinitape.etherjar.rpc.json.TransactionJson
import io.infinitape.etherjar.rpc.json.TransactionReceiptJson
import spock.lang.Specification

class EthCommandsSpec extends Specification {

    def getBlockNumber() {
        when:
        def call = Commands.eth().getBlockNumber()

        then:
        call.method == "eth_blockNumber"
        call.params == []
        call.jsonType == String
        call.resultType == Long
    }

    def getBalance() {
        when:
        def call = Commands.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 1000)

        then:
        call.method == "eth_getBalance"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x3e8']
        call.jsonType == String
        call.resultType == Wei

        when:
        call = Commands.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST)

        then:
        call.method == "eth_getBalance"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest']
        call.jsonType == String
        call.resultType == Wei
    }

    def getBlock() {
        when:
        def call = Commands.eth().getBlock(1000)

        then:
        call.method == "eth_getBlockByNumber"
        call.params == ['0x3e8', false]
        call.jsonType == BlockJson
        call.resultType == BlockJson

        when:
        call = Commands.eth().getBlockWithTransactions(1000)

        then:
        call.method == "eth_getBlockByNumber"
        call.params == ['0x3e8', true]
        call.jsonType == BlockJson
        call.resultType == BlockJson

        when:
        call = Commands.eth().getBlock(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"))

        then:
        call.method == "eth_getBlockByHash"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c', false]
        call.jsonType == BlockJson
        call.resultType == BlockJson

        when:
        call = Commands.eth().getBlockWithTransactions(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"))

        then:
        call.method == "eth_getBlockByHash"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c', true]
        call.jsonType == BlockJson
        call.resultType == BlockJson
    }

    def getTransaction() {
        when:
        def call = Commands.eth().getTransaction(TransactionId.from("0x18c3ba292e0388fdbcb3789feabc7312fba679f2a7ddc0f5611ce187b32a1d2b"))

        then:
        call.method == "eth_getTransactionByHash"
        call.params == ['0x18c3ba292e0388fdbcb3789feabc7312fba679f2a7ddc0f5611ce187b32a1d2b']
        call.jsonType == TransactionJson
        call.resultType == TransactionJson

        when:
        call = Commands.eth().getTransaction(1000, 5)

        then:
        call.method == "eth_getTransactionByBlockNumberAndIndex"
        call.params == ['0x3e8', '0x5']
        call.jsonType == TransactionJson
        call.resultType == TransactionJson

        when:
        call = Commands.eth().getTransaction(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"), 5)

        then:
        call.method == "eth_getTransactionByBlockHashAndIndex"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c', '0x5']
        call.jsonType == TransactionJson
        call.resultType == TransactionJson
    }

    def getTransactionReceipt() {
        when:
        def call = Commands.eth().getTransactionReceipt(TransactionId.from("0x18c3ba292e0388fdbcb3789feabc7312fba679f2a7ddc0f5611ce187b32a1d2b"))

        then:
        call.method == "eth_getTransactionReceipt"
        call.params == ['0x18c3ba292e0388fdbcb3789feabc7312fba679f2a7ddc0f5611ce187b32a1d2b']
        call.jsonType == TransactionReceiptJson
        call.resultType == TransactionReceiptJson
    }

    def getTransactionCount() {
        when:
        def call = Commands.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 1000)

        then:
        call.method == "eth_getTransactionCount"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x3e8']
        call.jsonType == String
        call.resultType == Long

        when:
        call = Commands.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.EARLIEST)

        then:
        call.method == "eth_getTransactionCount"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'earliest']
        call.jsonType == String
        call.resultType == Long

        when:
        call = Commands.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'))

        then:
        call.method == "eth_getTransactionCount"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911']
        call.jsonType == String
        call.resultType == Long

        when:
        call = Commands.eth().getBlockTransactionCount(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"))

        then:
        call.method == "eth_getBlockTransactionCountByHash"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c']
        call.jsonType == String
        call.resultType == Long

        when:
        call = Commands.eth().getBlockTransactionCount(1000)

        then:
        call.method == "eth_getBlockTransactionCountByNumber"
        call.params == ['0x3e8']
        call.jsonType == String
        call.resultType == Long
    }

    def getUncleCount() {
        when:
        def call = Commands.eth().getUncleCount(1000)

        then:
        call.method == "eth_getUncleCountByBlockNumber"
        call.params == ['0x3e8']
        call.jsonType == String
        call.resultType == Long

        when:
        call = Commands.eth().getUncleCount(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"))

        then:
        call.method == "eth_getUncleCountByBlockHash"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c']
        call.jsonType == String
        call.resultType == Long
    }

    def getUncle() {
        when:
        def call = Commands.eth().getUncle(1000, 2)

        then:
        call.method == "eth_getUncleByBlockNumberAndIndex"
        call.params == ['0x3e8', '0x2']
        call.jsonType == BlockJson
        call.resultType == BlockJson

        when:
        call = Commands.eth().getUncle(BlockHash.from("0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"), 1)

        then:
        call.method == "eth_getUncleByBlockHashAndIndex"
        call.params == ['0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c', '0x1']
        call.jsonType == BlockJson
        call.resultType == BlockJson
    }

    def getCode() {
        when:
        def call = Commands.eth().getCode(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 1000)

        then:
        call.method == "eth_getCode"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x3e8']
        call.jsonType == String
        call.resultType == HexData

        when:
        call = Commands.eth().getCode(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST)

        then:
        call.method == "eth_getCode"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest']
        call.jsonType == String
        call.resultType == HexData
    }

    def getWork() {
        when:
        def call = Commands.eth().getWork()

        then:
        call.method == "eth_getWork"
        call.params == []
        call.jsonType == String[]
        call.resultType == HexData[]
    }

    def submitWork() {
        when:
        def call = Commands.eth().submitWork(Nonce.from('0x0000000000000001'),
                Hex32.from('0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef'),
                Hex32.from('0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000'))

        then:
        call.method == "eth_submitWork"
        call.params == ["0x0000000000000001",
                        "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
                        "0xd1fe5700000000000000000000000000d1fe5700000000000000000000000000"]
        call.jsonType == Boolean
        call.resultType == Boolean
    }

    def submitHashrate() {
        when:
        def call = Commands.eth().submitHashrate(
                Hex32.from('0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef'),
                Hex32.from('0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000'))

        then:
        call.method == "eth_submitHashrate"
        call.params == ["0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
                        "0xd1fe5700000000000000000000000000d1fe5700000000000000000000000000"]
        call.jsonType == Boolean
        call.resultType == Boolean
    }

    def getCoinbase() {
        when:
        def call = Commands.eth().getCoinbase()

        then:
        call.method == "eth_coinbase"
        call.params == []
        call.jsonType == String
        call.resultType == Address
    }

    def getHashrate() {
        when:
        def call = Commands.eth().getHashrate()

        then:
        call.method == "eth_hashrate"
        call.params == []
        call.jsonType == String
        call.resultType == Long
    }

    def isMining() {
        when:
        def call = Commands.eth().isMining()

        then:
        call.method == "eth_mining"
        call.params == []
        call.jsonType == Boolean
        call.resultType == Boolean
    }

    def getGetGasPrice() {
        when:
        def call = Commands.eth().getGasPrice()

        then:
        call.method == "eth_gasPrice"
        call.params == []
        call.jsonType == String
        call.resultType == Wei
    }

    def getAccounts() {
        when:
        def call = Commands.eth().getAccounts()

        then:
        call.method == "eth_accounts"
        call.params == []
        call.jsonType == String[]
        call.resultType == Address[]
    }

    def getChainId() {
        when:
        def call = Commands.eth().getChainId()

        then:
        call.method == "eth_chainId"
        call.params == []
        call.jsonType == String
        call.resultType == Long
    }

    def call() {
        def tx = new TransactionCallJson(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), Wei.ofEthers(1))
        when:
        def call = Commands.eth().call(tx, BlockTag.LATEST)

        then:
        call.method == "eth_call"
        call.params == [tx, 'latest']
        call.jsonType == String
        call.resultType == HexData
    }

    def sendTransaction() {
        def tx = new TransactionCallJson(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), Wei.ofEthers(1))
        when:
        def call = Commands.eth().sendTransaction(tx)

        then:
        call.method == "eth_sendTransaction"
        call.params == [tx]
        call.jsonType == String
        call.resultType == TransactionId

        when:
        call = Commands.eth().sendTransaction(HexData.from("0x00"))

        then:
        call.method == "eth_sendRawTransaction"
        call.params == ['0x00']
        call.jsonType == String
        call.resultType == TransactionId
    }

    def sign() {
        when:
        def call = Commands.eth().sign(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), HexData.from("0x1234"))

        then:
        call.method == "eth_sign"
        call.params == ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1234']
        call.jsonType == String
        call.resultType == HexData
    }
}
