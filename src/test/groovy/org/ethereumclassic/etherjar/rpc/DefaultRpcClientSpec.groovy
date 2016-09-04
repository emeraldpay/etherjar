package org.ethereumclassic.etherjar.rpc

import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.HexData
import org.ethereumclassic.etherjar.rpc.json.BlockJson
import org.ethereumclassic.etherjar.rpc.json.BlockTag
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
}
