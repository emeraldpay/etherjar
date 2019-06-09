/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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

import io.infinitape.etherjar.domain.*
import io.infinitape.etherjar.hex.Hex32
import io.infinitape.etherjar.hex.HexData
import io.infinitape.etherjar.rpc.json.*
import io.infinitape.etherjar.test.MockRpcTransport
import spock.lang.Specification

import java.math.RoundingMode

class DefaultRpcClientDeprecatedSpec extends Specification {

    DefaultRpcClient defaultRpcClient

    MockRpcTransport rpcTransport

    def setup() {
        rpcTransport = new MockRpcTransport()
        defaultRpcClient = new DefaultRpcClient(rpcTransport)
    }

    def "Load current height"() {
        setup:
        rpcTransport.mock("eth_blockNumber", [], String,"0x1f47d0")
        when:
        def act = defaultRpcClient.eth().getBlockNumber().get()

        then:
        act == 2050000
    }

    def "Get balance"() {
        setup:
        rpcTransport.mock("eth_getBalance", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'],"0x0234c8a3397aab58")
        rpcTransport.mock("eth_getBalance", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1f47d0'], String, "0x0234c8a3397aab58")

        when:
        def act = defaultRpcClient.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()

        then:
        act.toEthers().setScale(3, RoundingMode.HALF_UP) == 0.159

        when:
        act = defaultRpcClient.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2050000).get()

        then:
        act.toEthers().setScale(3, RoundingMode.HALF_UP) == 0.159
    }

    def "Get block by number"() {
        def json = new BlockJson()
        json.number = 2050000
        rpcTransport.mock("eth_getBlockByNumber", ['0x1f47d0', false], BlockJson, json)
        rpcTransport.mock("eth_getBlockByNumber", ['0x1f47d0', true], BlockJson, json)

        when:
        def act = defaultRpcClient.eth().getBlock(2050000, false)

        then:
        act.get() == json

        when:
        act = defaultRpcClient.eth().getBlock(2050000, true)

        then:
        act.get() == json
    }

    def "Get block by hash"() {
        def json = new BlockJson()
        json.number = 2050000
        rpcTransport.mock("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', false], BlockJson, json)
        rpcTransport.mock("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', true], BlockJson, json)

        when:
        def act = defaultRpcClient.eth().getBlock(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), false)

        then:
        act.get() == json

        when:
        act = defaultRpcClient.eth().getBlock(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), true)

        then:
        act.get() == json
    }

    def "Get tx by hash"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        rpcTransport.mock("eth_getTransactionByHash",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionJson, json)

        when:
        def act = defaultRpcClient.eth().getTransaction(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))

        then:
        act.get() == json
    }

    def "Get tx by block hash and index"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        rpcTransport.mock("eth_getTransactionByBlockHashAndIndex",
                ["0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946", '0x0'], TransactionJson, json)

        when:
        def act = defaultRpcClient.eth().getTransaction(BlockHash.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'), 0)

        then:

        0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946
        act.get() == json
    }

    def "Get tx by block number and index"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        rpcTransport.mock("eth_getTransactionByBlockNumberAndIndex",
                ["0x1ea0c0", '0x0'], TransactionJson, json)

        when:
        def act = defaultRpcClient.eth().getTransaction(2007232, 0)

        then:
        act.get() == json
    }

    def "Get tx receipt"() {
        def json = new TransactionReceiptJson()
        json.transactionHash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        rpcTransport.mock("eth_getTransactionReceipt",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionReceiptJson, json)

        when:
        def act = defaultRpcClient.eth().getTransactionReceipt(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))

        then:
        act.get() == json
    }

    def "Get tx count"() {
        rpcTransport.mock("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String, "0x234")
        rpcTransport.mock("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51912', 'latest'], String, "0x1")
        rpcTransport.mock("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1f47d0'], String, "0x9")

        when:
        def act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()

        then:
        act == 564L

        when:
        act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51912'), BlockTag.LATEST).get()

        then:
        act == 1L

        when:
        act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2050000).get()

        then:
        act == 9L
    }

    def "Get block tx count"() {
        rpcTransport.mock("eth_getBlockTransactionCountByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'], String, "0xa")
        rpcTransport.mock("eth_getBlockTransactionCountByNumber", ['0x1f47d0'], String, "0x1")

        when:
        def act = defaultRpcClient.eth().getBlockTransactionCount(
                BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339')
        ).get()

        then:
        act == 10L

        when:
        act = defaultRpcClient.eth().getBlockTransactionCount(
                2050000
        ).get()

        then:
        act == 1L
    }

    def "Get block uncles count"() {
        rpcTransport.mock("eth_getUncleCountByBlockHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'], String, "0xa")
        rpcTransport.mock("eth_getUncleCountByBlockNumber", ['0x1f47d0'], String, "0x1")

        when:
        def act = defaultRpcClient.eth().getUncleCount(
                BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339')
        ).get()

        then:
        act == 10L

        when:
        act = defaultRpcClient.eth().getUncleCount(
                2050000
        ).get()

        then:
        act == 1L
    }

    def "Get code"() {
        rpcTransport.mock("eth_getCode",
                ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'],
                String,"0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056")
        rpcTransport.mock("eth_getCode",
                ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x2'],
                String,"0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056")

        when:
        def act = defaultRpcClient.eth().getCode(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST
        ).get()

        then:
        act.toHex() == "0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056"

        when:
        act = defaultRpcClient.eth().getCode(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2L
        ).get()

        then:
        act.toHex() == "0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056"
    }

    def "Get uncle"() {
        def json = new BlockJson()
        json.number = 2050000
        rpcTransport.mock("eth_getUncleByBlockHashAndIndex",
                ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', '0x0'],
                BlockJson, json)

        rpcTransport.mock("eth_getUncleByBlockNumberAndIndex",
                ['0x1f47d0', '0x0'],
                BlockJson, json)

        when:
        def act = defaultRpcClient.eth().getUncle(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), 0L)

        then:
        act.get() == json

        when:
        act = defaultRpcClient.eth().getUncle(2050000, 0L)

        then:
        act.get() == json
    }

    def "Get trace"() {
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        def json = [
                new TraceItemJson()
        ]
        rpcTransport.mock("trace_transaction",
                ['0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc'],
                TraceList, json)

        when:
        def act = defaultRpcClient.trace().getTransaction(txid)

        then:
        act.get() == json
    }

    def "Get work"() {
        def data = [    '0x7aecf7e21cd03501010454105ccd4b688939684505a01457cef338a33924ad02',
                        '0x002440e15267eebdf06fa7fe5aee5ccff445967925a90ecce6429aef7f8feb1f',
                        '0x000000000029891796c0001e696bca79de31c4640e112f147dc80e77263ffa1a']
        rpcTransport.mock("eth_getWork", [], HexData[], data as String[])
        when:
        def act = defaultRpcClient.eth().getWork()

        then:
        act.get().size() == data.size()
        act.get().collect { it.toHex() } as Set == data as Set
    }

    def "Submit Hashrate"() {
        def hashRate = Hex32.from("0x0000000000000000000000000000000000000000000000000000000000500000");
        def id = Hex32.from("0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c");
        rpcTransport.mock("eth_submitHashrate", [hashRate.toHex(), id.toHex()], Boolean, true)

        when:
        def act = defaultRpcClient.eth().submitHashrate(hashRate, id);

        then:
        act.get() == true
    }

    def "Submit Work"() {
        def nonce = Nonce.from("0x0000000000000001");
        def powHash = Hex32.from("0x0234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
        def digest = Hex32.from("0x01fe5700000000000000000000000000d1fe5700000000000000000000000000");
        rpcTransport.mock("eth_submitWork", [nonce.toHex(), powHash.toHex(), digest.toHex()], Boolean, true)

        when:
        def act = defaultRpcClient.eth().submitWork(nonce, powHash, digest);

        then:
        act.get() == true
    }

    def "Coinbase"() {
        def data = '0x7aecf7e21cd03501010454105ccd4b6889396845'
        rpcTransport.mock("eth_coinbase", [], Address, data)

        when:
        def act = defaultRpcClient.eth().getCoinbase()

        then:
        act.get().toHex() == data
    }

    def "Get Hashrate"() {
        def data = 947330l
        rpcTransport.mock("eth_hashrate", [], String, "0x1f47d0")

        when:
        def act = defaultRpcClient.eth().getHashrate()

        then:
        act.get() == 2050000
    }

    def "Is Mining"() {
        rpcTransport.mock("eth_mining", [], Boolean, true)

        when:
        def act = defaultRpcClient.eth().isMining();

        then:
        act.get() == true
    }

    def "Gas price"() {
        def data = 20000000000L
        rpcTransport.mock("eth_gasPrice", [], String, "0x4A817C800")

        when:
        def act = defaultRpcClient.eth().getGasPrice()

        then:
        act.get().amount == data
    }

    def "Accounts"() {
        def data = [    '0xf45c301e123a068badac079d0cff1a9e4ad51911',
                        '0x1e45c30168ba23a0dac51911079d0fcff1a9e4ad',
                        '0xf45c301e123a068badac079d0cff1a9e4ad51911',
                        '0xf45c301e123a068badac079d0cff1a9e4ad51911']
        rpcTransport.mock("eth_accounts", [], Address[], data as String[])

        when:
        def act = defaultRpcClient.eth().getAccounts()

        then:
        act.get().size() == data.size()
        act.get().collect { it.toHex() } as Set == data as Set
    }

    def "Call"() {
        def call = new TransactionCallJson(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                HexData.from('0x18160ddd')
        )
        rpcTransport.mock("eth_call", [call, 'latest'], String, '0x000000000000000000000000000000000000000000000000000039bc22c57200')

        when:
        def act = defaultRpcClient.eth().call(call, BlockTag.LATEST)

        then:
        act.get().toHex() == '0x000000000000000000000000000000000000000000000000000039bc22c57200'
    }

    def "Send Transaction"() {
        def tx = new TransactionCallJson(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                Address.from('0x1e45c30168ba23a0dac51911079d0fcff1a9e4ad'),
                Wei.ofEthers(12.345)
        )
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        rpcTransport.mock("eth_sendTransaction", [tx], String, '0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')

        when:
        def act = defaultRpcClient.eth().sendTransaction(tx)

        then:
        act.get() == txid
    }

    def "Send Raw Transaction"() {
        def tx = HexData.from('0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675')
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        rpcTransport.mock("eth_sendRawTransaction", ['0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675'], String, '0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')

        when:
        def act = defaultRpcClient.eth().sendTransaction(tx)

        then:
        act.get() == txid
    }

    def "Sign"() {
        def addr = Address.from('0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab')
        def data = HexData.from('0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470')
        rpcTransport.mock("eth_sign", ['0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab', '0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470'], String, '0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001')

        when:
        def act = defaultRpcClient.eth().sign(addr, data)

        then:
        act.get().toHex() == '0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001'
    }
}
