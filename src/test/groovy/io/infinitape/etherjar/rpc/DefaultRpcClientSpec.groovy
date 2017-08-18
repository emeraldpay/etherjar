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

import io.infinitape.etherjar.core.*
import io.infinitape.etherjar.rpc.json.*
import io.infinitape.etherjar.rpc.transport.RpcTransport
import spock.lang.Specification

import java.math.RoundingMode
import java.util.concurrent.CompletableFuture

class DefaultRpcClientSpec extends Specification {

    DefaultRpcClient defaultRpcClient

    RpcTransport rpcTransport

    def setup() {
        rpcTransport = Mock(RpcTransport)
        defaultRpcClient = new DefaultRpcClient(rpcTransport)
    }

    def "Load current height"() {
        when:
        def act = defaultRpcClient.eth().getBlockNumber().get()

        then:
        1 * rpcTransport.execute("eth_blockNumber", [], String) >> CompletableFuture.completedFuture("0x1f47d0")
        act == 2050000
    }

    def "Get balance"() {
        when:
        def act = defaultRpcClient.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()

        then:
        1 * rpcTransport.execute("eth_getBalance", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> CompletableFuture.completedFuture("0x0234c8a3397aab58")
        act.toEthers().setScale(3, RoundingMode.HALF_UP) == 0.159

        when:
        act = defaultRpcClient.eth().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2050000).get()

        then:
        1 * rpcTransport.execute("eth_getBalance", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1f47d0'], String) >> CompletableFuture.completedFuture("0x0234c8a3397aab58")
        act.toEthers().setScale(3, RoundingMode.HALF_UP) == 0.159
    }

    def "Get block by number"() {
        def json = new BlockJson()
        json.number = 2050000

        when:
        def act = defaultRpcClient.eth().getBlock(2050000, false)

        then:
        1 * rpcTransport.execute("eth_getBlockByNumber", ['0x1f47d0', false], BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json

        when:
        act = defaultRpcClient.eth().getBlock(2050000, true)

        then:
        1 * rpcTransport.execute("eth_getBlockByNumber", ['0x1f47d0', true], BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get block by hash"() {
        def json = new BlockJson()
        json.number = 2050000

        when:
        def act = defaultRpcClient.eth().getBlock(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), false)

        then:
        1 * rpcTransport.execute("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', false], BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json

        when:
        act = defaultRpcClient.eth().getBlock(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), true)

        then:
        1 * rpcTransport.execute("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', true], BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get tx by hash"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")

        when:
        def act = defaultRpcClient.eth().getTransaction(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))

        then:
        1 * rpcTransport.execute("eth_getTransactionByHash",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get tx by block hash and index"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")

        when:
        def act = defaultRpcClient.eth().getTransaction(BlockHash.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'), 0)

        then:
        1 * rpcTransport.execute("eth_getTransactionByBlockHashAndIndex",
                ["0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946", '0x00'], TransactionJson) >> CompletableFuture.completedFuture(json)

        0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946
        act.get() == json
    }

    def "Get tx by block number and index"() {
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")

        when:
        def act = defaultRpcClient.eth().getTransaction(2007232, 0)

        then:
        1 * rpcTransport.execute("eth_getTransactionByBlockNumberAndIndex",
                ["0x1ea0c0", '0x00'], TransactionJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get tx receipt"() {
        def json = new TransactionReceiptJson()
        json.transactionHash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")

        when:
        def act = defaultRpcClient.eth().getTransactionReceipt(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))

        then:
        1 * rpcTransport.execute("eth_getTransactionReceipt",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionReceiptJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get tx count"() {
        when:
        def act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()

        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> CompletableFuture.completedFuture("0x0234")
        act == 564L

        when:
        act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()

        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> CompletableFuture.completedFuture("0x1")
        act == 1L

        when:
        act = defaultRpcClient.eth().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2050000).get()

        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1f47d0'], String) >> CompletableFuture.completedFuture("0x9")
        act == 9L
    }

    def "Get block tx count"() {
        when:
        def act = defaultRpcClient.eth().getBlockTransactionCount(
                BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339')
        ).get()

        then:
        1 * rpcTransport.execute("eth_getBlockTransactionCountByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'], String) >> CompletableFuture.completedFuture("0x0a")
        act == 10L

        when:
        act = defaultRpcClient.eth().getBlockTransactionCount(
                2050000
        ).get()

        then:
        1 * rpcTransport.execute("eth_getBlockTransactionCountByNumber", ['0x1f47d0'], String) >> CompletableFuture.completedFuture("0x1")
        act == 1L
    }

    def "Get block uncles count"() {
        when:
        def act = defaultRpcClient.eth().getUncleCount(
                BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339')
        ).get()

        then:
        1 * rpcTransport.execute("eth_getUncleCountByBlockHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'], String) >> CompletableFuture.completedFuture("0x0a")
        act == 10L

        when:
        act = defaultRpcClient.eth().getUncleCount(
                2050000
        ).get()

        then:
        1 * rpcTransport.execute("eth_getUncleCountByBlockNumber", ['0x1f47d0'], String) >> CompletableFuture.completedFuture("0x1")
        act == 1L
    }

    def "Get code"() {
        when:
        def act = defaultRpcClient.eth().getCode(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST
        ).get()

        then:
        1 * rpcTransport.execute("eth_getCode",
                ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'],
                String) >> CompletableFuture.completedFuture("0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056")
        act.toHex() == "0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056"

        when:
        act = defaultRpcClient.eth().getCode(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2L
        ).get()

        then:
        1 * rpcTransport.execute("eth_getCode",
                ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x02'],
                String) >> CompletableFuture.completedFuture("0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056")
        act.toHex() == "0x600160008035811a818181146012578301005b601b6001356025565b8060005260206000f25b600060078202905091905056"
    }

    def "Get uncle"() {
        def json = new BlockJson()
        json.number = 2050000

        when:
        def act = defaultRpcClient.eth().getUncle(BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), 0L)

        then:
        1 * rpcTransport.execute("eth_getUncleByBlockHashAndIndex",
                ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', '0x00'],
                BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json

        when:
        act = defaultRpcClient.eth().getUncle(2050000, 0L)

        then:
        1 * rpcTransport.execute("eth_getUncleByBlockNumberAndIndex",
                ['0x1f47d0', '0x00'],
                BlockJson) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get trace"() {
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        def json = [
                new TraceItemJson()
        ]

        when:
        def act = defaultRpcClient.trace().getTransaction(txid)

        then:
        1 * rpcTransport.execute("trace_transaction",
                ['0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc'],
                TraceList) >> CompletableFuture.completedFuture(json)
        act.get() == json
    }

    def "Get work"() {
        def data = [    '0x7aecf7e21cd03501010454105ccd4b688939684505a01457cef338a33924ad02',
                        '0x002440e15267eebdf06fa7fe5aee5ccff445967925a90ecce6429aef7f8feb1f',
                        '0x000000000029891796c0001e696bca79de31c4640e112f147dc80e77263ffa1a']
        when:
        def act = defaultRpcClient.eth().getWork()

        then:
        1 * rpcTransport.execute("eth_getWork", [], HexData[]) >> CompletableFuture.completedFuture(data)
        act.get().size() == data.size()
        act.get() as Set == data as Set
    }

    def "Submit Hashrate"() {
        def hashRate = Hex32.from("0x0000000000000000000000000000000000000000000000000000000000500000");
        def id = Hex32.from("0x59daa26581d0acd1fce254fb7e85952f4c09d0915afd33d3886cd914bc7d283c");

        when:
        def act = defaultRpcClient.eth().submitHashrate(hashRate, id);

        then:
        1 * rpcTransport.execute("eth_submitHashrate", [hashRate.toHex(), id.toHex()], Boolean) >> CompletableFuture.completedFuture(true)
        act.get() == true
    }

    def "Submit Work"() {
        def nonce = Nonce.from("0x0000000000000001");
        def powHash = Hex32.from("0x0234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
        def digest = Hex32.from("0x01fe5700000000000000000000000000d1fe5700000000000000000000000000");

        when:
        def act = defaultRpcClient.eth().submitWork(nonce, powHash, digest);

        then:
        1 * rpcTransport.execute("eth_submitWork", [nonce.toHex(), powHash.toHex(), digest.toHex()], Boolean) >> CompletableFuture.completedFuture(true)
        act.get() == true
    }

    def "Coinbase"() {
        def data = '0x7aecf7e21cd03501010454105ccd4b688939684505a01457cef338a33924ad02'

        when:
        def act = defaultRpcClient.eth().getCoinbase()

        then:
        1 * rpcTransport.execute("eth_coinbase", [], Address) >> CompletableFuture.completedFuture(data)
        act.get() == data
    }

    def "Get Hashrate"() {
        def data = 947330l

        when:
        def act = defaultRpcClient.eth().getHashrate()

        then:
        1 * rpcTransport.execute("eth_hashrate", [], String) >> CompletableFuture.completedFuture("0x1f47d0")
        act.get() == 2050000
    }

    def "Is Mining"() {
        when:
        def act = defaultRpcClient.eth().isMining();

        then:
        1 * rpcTransport.execute("eth_mining", [], Boolean) >> CompletableFuture.completedFuture(true)
        act.get() == true
    }

    def "Gas price"() {
        def data = 20000000000L

        when:
        def act = defaultRpcClient.eth().getGasPrice()

        then:
        1 * rpcTransport.execute("eth_gasPrice", [], String) >> CompletableFuture.completedFuture("0x4A817C800")
        act.get() == data
    }

    def "Accounts"() {
        def data = [    Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                        Address.from('0x1e45c30168ba23a0dac51911079d0fcff1a9e4ad'),
                        Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                        Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911')]

        when:
        def act = defaultRpcClient.eth().getAccounts()

        then:
        1 * rpcTransport.execute("eth_accounts", [], Address[]) >> CompletableFuture.completedFuture(data)
        act.get().size() == data.size()
        act.get() as Set == data as Set
    }

    def "Get Compilers"() {
        def data = ["solidity", "lll", "serpent"]

        when:
        def act = defaultRpcClient.eth().getCompilers()

        then:
        1 * rpcTransport.execute("eth_getCompilers", [], String[]) >> CompletableFuture.completedFuture(data)
        act.get().size() == data.size()
        act.get() as Set == data as Set
    }

    def "Call"() {
        def call = new TransactionCallJson(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                HexData.from('0x18160ddd')
        )

        when:
        def act = defaultRpcClient.eth().call(call, BlockTag.LATEST)

        then:
        1 * rpcTransport.execute("eth_call", [call, 'latest'], String) >> CompletableFuture.completedFuture('0x000000000000000000000000000000000000000000000000000039bc22c57200')
        act.get().toHex() == '0x000000000000000000000000000000000000000000000000000039bc22c57200'
    }

    def "Send Transaction"() {
        def tx = new TransactionCallJson(
                Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'),
                Address.from('0x1e45c30168ba23a0dac51911079d0fcff1a9e4ad'),
                Wei.ofEthers(12.345)
        )
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')

        when:
        def act = defaultRpcClient.eth().sendTransaction(tx)

        then:
        1 * rpcTransport.execute("eth_sendTransaction", [tx], String) >> CompletableFuture.completedFuture('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        act.get() == txid
    }

    def "Send Raw Transaction"() {
        def tx = HexData.from('0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675')
        def txid = TransactionId.from('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')

        when:
        def act = defaultRpcClient.eth().sendTransaction(tx)

        then:
        1 * rpcTransport.execute("eth_sendRawTransaction", ['0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675'], String) >> CompletableFuture.completedFuture('0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc')
        act.get() == txid
    }

    def "Sign"() {
        def addr = Address.from('0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab')
        def data = HexData.from('0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470')

        when:
        def act = defaultRpcClient.eth().sign(addr, data)

        then:
        1 * rpcTransport.execute("eth_sign", ['0x8a3106a3e50576d4b6794a0e74d3bb5f8c9acaab', '0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470'], String) >> CompletableFuture.completedFuture('0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001')
        act.get().toHex() == '0xbd685c98ec39490f50d15c67ba2a8e9b5b1d6d7601fca80b295e7d717446bd8b7127ea4871e996cdc8cae7690408b4e800f60ddac49d2ad34180e68f1da0aaf001'
    }
}
