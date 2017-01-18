package org.ethereumclassic.etherjar.model

import spock.lang.Specification

/**
 *
 * @author Igor Artamonov
 */
class ChainIdSpec extends Specification {

    def "Works for classic"() {
        when:
        def act = new ChainId(61)
        then:
        act.value == 61
        act == ChainId.MAINNET
        when:
        act = new ChainId(62)
        then:
        act.value == 62
        act == ChainId.TESTNET
    }

    def "Works for forked"() {
        when:
        def act = new ChainId(1)
        then:
        act.value == 1
        act == ChainId.EFNET
        when:
        act = new ChainId(3)
        then:
        act.value == 3
        act == ChainId.ROPSTEN
    }

    def "Accept byte numbers"() {
        expect:
        ChainId.isValid(x)
        where:
        x << (0..255)
    }

    def "Decline non-byte numbers"() {
        expect:
        !ChainId.isValid(x)
        where:
        x << [-1, -100, -250, 256, 1024, 6161, 6819571, Integer.MAX_VALUE, Integer.MIN_VALUE]
    }
}
