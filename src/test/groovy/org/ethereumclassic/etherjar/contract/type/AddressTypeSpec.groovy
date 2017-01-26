package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class AddressTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = AddressType.from 'address'

        then:
        opt.present
        opt.get() in AddressType
    }

    def "should detect null string representation"() {
        when:
        AddressType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = AddressType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = AddressType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint140'
        _ | 'byte'
    }

    def "should return a canonical string representation" () {
        expect:
        AddressType.DEFAULT.canonicalName == 'address'
    }

    def "should encode & decode bytes"() {
        def obj = Address.from addr

        when:
        def data = AddressType.DEFAULT.encodeStatic obj
        def res = AddressType.DEFAULT.decodeStatic data

        then:
        data.toHex() == hex
        res == obj

        where:
        addr                                            | hex
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'    | '0x0000000000000000000000000000000000015b23c7e20b0ea5ebd84c39dcbe60'
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'    | '0x000000000000000000000000fffffffff3984f569b4c7ff5143499d94abe2ff2'
        '0x0000000000000000000000000000000000000000'    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        '0xffffffffffffffffffffffffffffffffffffffff'    | '0x000000000000000000000000ffffffffffffffffffffffffffffffffffffffff'
    }

    def "should catch wrong data to decode"() {
        def data = Hex32.from hex

        when:
        AddressType.DEFAULT.decodeStatic data

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | '0x0000000000000000000000010000000000000000000000000000000000000000'
        _ | '0x000000000000000000000001ffffffffffffffffffffffffffffffffffffffff'
        _ | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first               | second
        AddressType.DEFAULT | [] as AddressType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first               | second
        AddressType.DEFAULT | AddressType.DEFAULT
        AddressType.DEFAULT | [] as AddressType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first               | second
        AddressType.DEFAULT | null
        AddressType.DEFAULT | UIntType.DEFAULT
        AddressType.DEFAULT | DynamicBytesType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        AddressType.DEFAULT as String == 'address'
    }
}
