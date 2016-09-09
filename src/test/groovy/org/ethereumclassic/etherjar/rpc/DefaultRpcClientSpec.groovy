package org.ethereumclassic.etherjar.rpc

import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.BlockHash
import org.ethereumclassic.etherjar.model.HexData
import org.ethereumclassic.etherjar.model.TransactionId
import org.ethereumclassic.etherjar.rpc.json.BlockJson
import org.ethereumclassic.etherjar.rpc.json.BlockTag
import org.ethereumclassic.etherjar.rpc.json.TransactionJson
import org.ethereumclassic.etherjar.rpc.json.TransactionReceiptJson
import org.ethereumclassic.etherjar.rpc.transport.RpcTransport
import spock.lang.Specification

import static org.ethereumclassic.etherjar.rpc.ConcurrencyUtils.*

/**
 *
 * @author Igor Artamonov
 */
class DefaultRpcClientSpec extends Specification {

    DefaultRpcClient defaultRpcClient
    RpcTransport rpcTransport

    def setup() {
        rpcTransport = Mock(RpcTransport)
        defaultRpcClient = new DefaultRpcClient(rpcTransport)
    }

    def "Load current height"() {
        when:
        def act = defaultRpcClient.network().blockNumber().get()
        then:
        1 * rpcTransport.execute("eth_blockNumber", [], String) >> new CompletedFuture<>("0x1f47d0")
        act == 2050000
    }

    def "Get balance"() {
        when:
        def act = defaultRpcClient.network().getBalance(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()
        then:
        1 * rpcTransport.execute("eth_getBalance", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> new CompletedFuture<>("0x0234c8a3397aab58")
        act.toString() == "0.1590 ether"
    }

    def "Get block by number"() {
        setup:
        def json = new BlockJson()
        json.number = 2050000
        when:
        def act = defaultRpcClient.network().getBlock(2050000, false)
        then:
        1 * rpcTransport.execute("eth_getBlockByNumber", ['0x1f47d0', false], BlockJson) >> new CompletedFuture<>(json)
        act.get() == json

        when:
        act = defaultRpcClient.network().getBlock(2050000, true)
        then:
        1 * rpcTransport.execute("eth_getBlockByNumber", ['0x1f47d0', true], BlockJson) >> new CompletedFuture<>(json)
        act.get() == json
    }

    def "Get block by hash"() {
        setup:
        def json = new BlockJson()
        json.number = 2050000
        when:
        def act = defaultRpcClient.network().getBlock(HexData.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), false)
        then:
        1 * rpcTransport.execute("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', false], BlockJson) >> new CompletedFuture<>(json)
        act.get() == json

        when:
        act = defaultRpcClient.network().getBlock(HexData.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'), true)
        then:
        1 * rpcTransport.execute("eth_getBlockByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339', true], BlockJson) >> new CompletedFuture<>(json)
        act.get() == json
    }

    def "Get tx by hash"() {
        setup:
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        when:
        def act = defaultRpcClient.network().getTransaction(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))
        then:
        1 * rpcTransport.execute("eth_getTransactionByHash",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionJson) >> new CompletedFuture<>(json)
        act.get() == json
    }
    def "Get tx by block hash and index"() {
        setup:
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        when:
        def act = defaultRpcClient.network().getTransaction(HexData.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'), 0)
        then:
        1 * rpcTransport.execute("eth_getTransactionByBlockHashAndIndex",
                ["0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946", '0x0'], TransactionJson) >> new CompletedFuture<>(json)
        act.get() == json
    }
    def "Get tx by block number and index"() {
        setup:
        def json = new TransactionJson()
        json.hash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        when:
        def act = defaultRpcClient.network().getTransaction(2007232, 0)
        then:
        1 * rpcTransport.execute("eth_getTransactionByBlockNumberAndIndex",
                ["0x1ea0c0", '0x0'], TransactionJson) >> new CompletedFuture<>(json)
        act.get() == json
    }

    def "Get tx receipt"() {
        setup:
        def json = new TransactionReceiptJson()
        json.transactionHash = TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc")
        when:
        def act = defaultRpcClient.network().getTransactionReceipt(TransactionId.from("0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"))
        then:
        1 * rpcTransport.execute("eth_getTransactionReceipt",
                ["0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc"], TransactionReceiptJson) >> new CompletedFuture<>(json)
        act.get() == json
    }

    def "Get tx count"() {
        when:
        def act = defaultRpcClient.network().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()
        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> new CompletedFuture<>("0x0234")
        act == 564L

        when:
        act = defaultRpcClient.network().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), BlockTag.LATEST).get()
        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', 'latest'], String) >> new CompletedFuture<>("0x1")
        act == 1L

        when:
        act = defaultRpcClient.network().getTransactionCount(Address.from('0xf45c301e123a068badac079d0cff1a9e4ad51911'), 2050000).get()
        then:
        1 * rpcTransport.execute("eth_getTransactionCount", ['0xf45c301e123a068badac079d0cff1a9e4ad51911', '0x1f47d0'], String) >> new CompletedFuture<>("0x9")
        act == 9L
    }

    def "Get block tx count"() {
        when:
        def act = defaultRpcClient.network().getBlockTransactionCount(
                BlockHash.from('0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339')
        ).get()
        then:
        1 * rpcTransport.execute("eth_getBlockTransactionCountByHash", ['0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'], String) >> new CompletedFuture<>("0x0a")
        act == 10L

        when:
        act = defaultRpcClient.network().getBlockTransactionCount(
                2050000
        ).get()
        then:
        1 * rpcTransport.execute("eth_getBlockTransactionCountByNumber", ['0x1f47d0'], String) >> new CompletedFuture<>("0x1")
        act == 1L
    }
}
