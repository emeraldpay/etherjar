package org.ethereumclassic.etherjar.model

import spock.lang.Specification

/**
 *
 * @since
 * @author Igor Artamonov
 */
class WeiSpec extends Specification {

    def "Convert wei to Ether"() {
        expect:
        Wei.from(hex).toEther() == ether
        where:
        hex                     | ether
        '0x0'                   |  0.0
        '0x1692343a32d9000'     |  0.101651
        '0x11527914c23af80'     |  0.078012
        '0x3f794375d8dc4c00'    |  4.573761
        '0xa9964ef1b825f600'    | 12.220041
        '0x1b1ae4d6e2ef500000'  | 500
        '0x32d26ce13c9584e4800' | 14999.999126
        '0x54b40aedd840a8e4800' | 24999.999126
    }

    def "Process large number of wei"() {
        setup:
        String hex = '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        when:
        def wei = Wei.from(hex)
        then:
        wei.value.toString() == '115792089237316195423570985008687907853269984665640564039457584007913129639935'
        wei.value.toString(16) == 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        wei.toEther() == new BigDecimal('115792089237316195423570985008687907853269984665640564039457.584008')
        wei.getBytes().length == 33
        wei.getBytes().toList().tail().every { b -> b == (byte)-1 } //tail because first element is 0
        wei.toString() == '115792089237316195423570985008687907853269984665640564039457.5840 ether'
    }

    def "Process small number of wei"() {
        when:
        def wei = Wei.from('0x0b3266')
        then:
        wei.getValue().toLong() == 733798L
        wei.toEther() == 0.0
        wei.toString() == '0.0000 ether'
    }
}
