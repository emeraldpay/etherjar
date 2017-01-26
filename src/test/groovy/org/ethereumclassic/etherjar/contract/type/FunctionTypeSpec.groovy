package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Specification

class FunctionTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = FunctionType.from 'function'

        then:
        opt.present
        opt.get() in FunctionType
    }

    def "should detect null string representation"() {
        when:
        FunctionType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = FunctionType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = FunctionType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint140'
        _ | 'byte'
    }

    def "should return a canonical string representation" () {
        expect:
        FunctionType.DEFAULT.canonicalName == 'function'
    }

    def "should encode & decode bytes"() {
        def obj = new AbstractMap.SimpleEntry<Address, MethodId>(Address.from(addr), MethodId.from(id))

        when:
        def data = FunctionType.DEFAULT.encodeStatic obj
        def res = FunctionType.DEFAULT.decodeStatic data

        then:
        data.toHex() == hex
        res == obj

        where:
        addr                                            | id            | hex
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'    | '0x11223344'  | '0x00000000000000000000000000015b23c7e20b0ea5ebd84c39dcbe6011223344'
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'    | '0xabcdefff'  | '0x0000000000000000fffffffff3984f569b4c7ff5143499d94abe2ff2abcdefff'
        '0x0000000000000000000000000000000000000000'    | '0x00000000'  | '0x0000000000000000000000000000000000000000000000000000000000000000'
        '0xffffffffffffffffffffffffffffffffffffffff'    | '0xffffffff'  | '0x0000000000000000ffffffffffffffffffffffffffffffffffffffffffffffff'
    }

    def "should catch wrong data to decode"() {
        def data = Hex32.from hex

        when:
        FunctionType.DEFAULT.decodeStatic data

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | '0x0000000000000001000000000000000000000000000000000000000000000000'
        _ | '0x0000000000000001ffffffffffffffffffffffffffffffffffffffffffffffff'
        _ | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                   | second
        FunctionType.DEFAULT    | [] as FunctionType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                   | second
        FunctionType.DEFAULT    | FunctionType.DEFAULT
        FunctionType.DEFAULT    | [] as FunctionType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first                   | second
        FunctionType.DEFAULT    | null
        FunctionType.DEFAULT    | UIntType.DEFAULT
        FunctionType.DEFAULT    | DynamicBytesType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        FunctionType.DEFAULT as String == 'function'
    }
}
