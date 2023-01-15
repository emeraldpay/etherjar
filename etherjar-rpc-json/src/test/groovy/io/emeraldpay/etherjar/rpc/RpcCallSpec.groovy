package io.emeraldpay.etherjar.rpc

import spock.lang.Specification

class RpcCallSpec extends Specification {

    def "toString implemented"() {
        when:
        def call = RpcCall.create("eth_test", ["hello", 123])
        def act = call.toString()
        then:
        act == "eth_test(hello, 123)"
    }

}
