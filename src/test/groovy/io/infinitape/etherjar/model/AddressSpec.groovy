package io.infinitape.etherjar.model

import spock.lang.Specification

class AddressSpec extends Specification {

    def "should parse address"() {
        expect:
        Address.from(addr).toString() == addr
        Address.from(addr).bytes == bytes

        where:
        addr                                            | bytes
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'    | [0, 0, 0, 0, 0, 1, 91, 35, -57, -30, 11, 14, -91, -21, -40, 76, 57, -36, -66, 96] as byte[]
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'    | [-1, -1, -1, -1, -13, -104, 79, 86, -101, 76, 127, -11, 20, 52, -103, -39, 74, -66, 47,-14] as byte[]
        '0x0000000000000000000000000000000000000000'    | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
        '0xffffffffffffffffffffffffffffffffffffffff'    | [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1] as byte[]
    }

    def "should validate address with checksum"() {
        expect:
        Address.isValidAddress '0x52908400098527886E0F7030069857D2E4169EE7'
        Address.isValidAddress '0xde709f2102306220921060314715629080e2fb77'
        Address.isValidAddress '0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed'
        Address.isValidAddress '0x5A4EAB120fB44eb6684E5e32785702FF45ea344D'

        and: "2nd 'A' changed to 'a'"
        !Address.isValidAddress('0x5a4EAB120fB44eb6684E5e32785702FF45ea344D')
    }

    def "should ignore invalid addresses"() {
        when:
        Address.from '0xK2908400098527886E0F7030069857D2E4169EE7'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0'
        then:
        thrown IllegalArgumentException

        when:
        Address.from null as String
        then:
        thrown IllegalArgumentException

        when:
        Address.from(null as byte[])
        then:
        thrown IllegalArgumentException

        when:
        Address.from ''
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0xfake'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0000000000015b23c7e20b0ea5ebd84c39dcbe'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0000000000015b23c7e20b0ea5ebd84c39dcbe6070'
        then:
        thrown(IllegalArgumentException)
    }
}
