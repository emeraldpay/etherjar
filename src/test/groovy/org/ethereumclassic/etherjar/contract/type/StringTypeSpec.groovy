package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class StringTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = StringType.from 'string'

        then:
        opt.present
        opt.get() in StringType
    }

    def "should detect null string representation"() {
        when:
        StringType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = StringType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = StringType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'bytes'
        _ | 'int256[]'
    }

    def "should return a canonical string representation" () {
        expect:
        StringType.DEFAULT.canonicalName == 'string'
    }

    def "should encode & decode array of bytes"() {
        when:
        def data = StringType.DEFAULT.encode str
        def res = StringType.DEFAULT.decode data

        then:
        data == hex
        res == str

        where:
        str     | hex
        ""      | Type.encodeLength(0)
        "~"     | Type.encodeLength(1).concat(Hex32.from('0x7e00000000000000000000000000000000000000000000000000000000000000'))
        "s"     | Type.encodeLength(1).concat(Hex32.from('0x7300000000000000000000000000000000000000000000000000000000000000'))
        "AK"    | Type.encodeLength(2).concat(Hex32.from('0x414b000000000000000000000000000000000000000000000000000000000000'))
        "ABC"   | Type.encodeLength(3).concat(Hex32.from('0x4142430000000000000000000000000000000000000000000000000000000000'))
        "௵" | Type.encodeLength(3).concat(Hex32.from('0xe0afb50000000000000000000000000000000000000000000000000000000000'))
        "𦈘"    | Type.encodeLength(4).concat(Hex32.from('0xf0a6889800000000000000000000000000000000000000000000000000000000'))
    }

    def "should catch incorrect utf-8 encoded data"() {
        when:
        StringType.DEFAULT.decode hex

        then:
        thrown RuntimeException

        where:
        _ | hex
        _ | Type.encodeLength(2).concat(Hex32.from('0xc100000000000000000000000000000000000000000000000000000000000000'))
        _ | Type.encodeLength(3).concat(Hex32.from('0xe081000000000000000000000000000000000000000000000000000000000000'))
        _ | Type.encodeLength(4).concat(Hex32.from('0xf080810000000000000000000000000000000000000000000000000000000000'))
    }

    def "should catch wrong data to decode"() {
        when:
        StringType.DEFAULT.decode hex

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | Type.encodeLength(4)
        _ | Type.encodeLength(17).concat(Hex32.EMPTY, Hex32.EMPTY)
        _ | Type.encodeLength(32).concat(Hex32.EMPTY, Hex32.EMPTY)
        _ | Type.encodeLength(34).concat(Hex32.EMPTY)
        _ | Type.encodeLength(0).concat(Hex32.EMPTY)
    }

    def "should catch empty data to decode"() {
        when:
        StringType.DEFAULT.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first               | second
        StringType.DEFAULT  | [] as StringType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first               | second
        StringType.DEFAULT  | StringType.DEFAULT
        StringType.DEFAULT  | [] as StringType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first               | second
        StringType.DEFAULT  | null
        StringType.DEFAULT  | 'ABC'
        StringType.DEFAULT  | new UIntType()
    }

    def "should be converted to a string representation"() {
        expect:
        StringType.DEFAULT as String == 'string'
    }
}
