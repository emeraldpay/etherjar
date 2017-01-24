package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class BytesTypeSpec extends Specification {

    final static DEFAULT = [] as BytesType

    def "should parse string representation"() {
        when:
        def opt = BytesType.from 'bytes'

        then:
        opt.present
    }

    def "should detect null string representation"() {
        when:
        BytesType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = BytesType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = BytesType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint40'
        _ | 'int256'
    }

    def "should return a canonical string representation" () {
        expect:
        DEFAULT.canonicalName == 'bytes'
    }

    def "should encode & decode list of bytes"() {
        def obj = bytes as byte[]

        when:
        def data = DEFAULT.encode obj
        def res = DEFAULT.decode data

        then:
        data == hex
        Arrays.equals res, obj

        where:
        bytes                       | hex
        [0x37]                      | Type.encodeLength(1).concat(HexData.from('0x3700000000000000000000000000000000000000000000000000000000000000'))
        [0x64, 0x61, 0x76, 0x65]    | Type.encodeLength(4).concat(HexData.from('0x6461766500000000000000000000000000000000000000000000000000000000'))
        [0x12] * 123                | Type.encodeLength(123).concat(HexData.from('0x' + '12' * 123 + '00' * 5))
    }

    def "should catch empty array to encode"() {
        when:
        DEFAULT.encode([] as byte[])

        then:
        thrown IllegalArgumentException
    }

    def "should catch wrong data to decode"() {
        when:
        DEFAULT.decode hex

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | Type.encodeLength(4)
        _ | Type.encodeLength(34).concat(HexData.from('0x6461766500000000000000000000000000000000000000000000000000000000'))
    }

    def "should catch empty data to decode"() {
        when:
        DEFAULT.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first           | second
        DEFAULT    | [] as BytesType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first           | second
        DEFAULT    | DEFAULT
        DEFAULT    | [] as BytesType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first           | second
        DEFAULT    | null
        DEFAULT    | 'ABC'
        DEFAULT    | UIntType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        DEFAULT as String == 'bytes'
    }
}
