package io.infinitape.etherjar.contract.type

import io.infinitape.etherjar.model.Hex32
import io.infinitape.etherjar.model.HexData
import spock.lang.Specification

class DynamicBytesTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = DynamicBytesType.from 'bytes'

        then:
        opt.present
        opt.get() in DynamicType
    }

    def "should detect null string representation"() {
        when:
        DynamicBytesType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = DynamicBytesType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = DynamicBytesType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'byte'
        _ | 'bytes32'
        _ | 'uint40'
        _ | 'int256'
    }

    def "should return a canonical string representation"() {
        expect:
        DynamicBytesType.DEFAULT.canonicalName == 'bytes'
    }

    def "should encode & decode array of bytes"() {
        def obj = bytes as byte[]

        when:
        def data = DynamicBytesType.DEFAULT.encode obj
        def res = DynamicBytesType.DEFAULT.decode data

        then:
        data == hex
        Arrays.equals res, obj

        where:
        bytes                       | hex
        [0x37]                      | Type.encodeLength(1).concat(Hex32.from('0x3700000000000000000000000000000000000000000000000000000000000000'))
        [0x64, 0x61, 0x76, 0x65]    | Type.encodeLength(4).concat(Hex32.from('0x6461766500000000000000000000000000000000000000000000000000000000'))
        [0x12] * 123                | Type.encodeLength(123).concat(HexData.from('0x' + '12' * 123 + '00' * 5))
    }

    def "should catch wrong data to decode"() {
        when:
        DynamicBytesType.DEFAULT.decode hex

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | Type.encodeLength(1)
        _ | Type.encodeLength(32).concat(Hex32.EMPTY, Hex32.EMPTY)
        _ | Type.encodeLength(34).concat(Hex32.from('0x6461766500000000000000000000000000000000000000000000000000000000'))
        _ | Type.encodeLength(0).concat(Hex32.EMPTY)
    }

    def "should catch empty data to decode"() {
        when:
        DynamicBytesType.DEFAULT.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                       | second
        DynamicBytesType.DEFAULT    | [] as DynamicBytesType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                       | second
        DynamicBytesType.DEFAULT    | DynamicBytesType.DEFAULT
        DynamicBytesType.DEFAULT    | [] as DynamicBytesType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first                       | second
        DynamicBytesType.DEFAULT    | null
        DynamicBytesType.DEFAULT    | 'ABC'
        DynamicBytesType.DEFAULT    | UIntType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        DynamicBytesType.DEFAULT as String == 'bytes'
    }
}
