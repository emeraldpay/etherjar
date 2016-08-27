package org.ethereumclassic.etherjar.model

import spock.lang.Specification

/**
 *
 * @since
 * @author Igor Artamonov
 */
class AddressSpec extends Specification {

    def "Parse address"() {
        expect:
        Address.from(addr).toString() == addr
        Address.from(addr).bytes == bytes
        where:
        addr                                          | bytes
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'  | [0, 0, 0, 0, 0, 1, 91, 35, -57, -30, 11, 14, -91, -21, -40, 76, 57, -36, -66, 96] as byte[]
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'  | [-1, -1, -1, -1, -13, -104, 79, 86, -101, 76, 127, -11, 20, 52, -103, -39, 74, -66, 47,-14] as byte[]
        '0x0000000000000000000000000000000000000000'  | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
        '0xffffffffffffffffffffffffffffffffffffffff'  | [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1] as byte[]
    }

    def "Ignore Invalid addr"() {
        when:
        Address.from('0x0')
        then:
        thrown(IllegalArgumentException)

        when:
        Address.from(null as String)
        then:
        thrown(IllegalArgumentException)

        when:
        Address.from(null as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        Address.from('')
        then:
        thrown(IllegalArgumentException)

        when:
        Address.from('0xfake')
        then:
        thrown(IllegalArgumentException)

        when:
        Address.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe')
        then:
        thrown(IllegalArgumentException)
        when:
        Address.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe6070')
        then:
        thrown(IllegalArgumentException)
    }
}
