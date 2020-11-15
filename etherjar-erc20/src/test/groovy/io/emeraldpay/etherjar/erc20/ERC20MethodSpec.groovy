package io.emeraldpay.etherjar.erc20

import spock.lang.Specification

class ERC20MethodSpec extends Specification {

    def "totalSupply id"() {
        when:
        def act = ERC20Method.TOTAL_SUPPLY.methodId
        then:
        act.toHex() == "0x18160ddd"
    }

    def "balanceOf id"() {
        when:
        def act = ERC20Method.BALANCE_OF.methodId
        then:
        act.toHex() == "0x70a08231"
    }

    def "allowance id"() {
        when:
        def act = ERC20Method.ALLOWANCE.methodId
        then:
        act.toHex() == "0xdd62ed3e"
    }

    def "transfer id"() {
        when:
        def act = ERC20Method.TRANSFER.methodId
        then:
        act.toHex() == "0xa9059cbb"
    }

    def "approve id"() {
        when:
        def act = ERC20Method.APPROVE.methodId
        then:
        act.toHex() == "0x095ea7b3"
    }

    def "transferFrom id"() {
        when:
        def act = ERC20Method.TRANSFER_FROM.methodId
        then:
        act.toHex() == "0x23b872dd"
    }
}
