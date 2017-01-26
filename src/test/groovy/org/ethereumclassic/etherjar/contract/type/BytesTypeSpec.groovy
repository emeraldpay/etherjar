package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class BytesTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = BytesType.from input

        then:
        opt.present
        opt.get() in BytesType
        opt.get().canonicalName == output

        where:
        input       | output
        'byte'      | 'bytes1'
        'bytes1'    | 'bytes1'
        'bytes7'    | 'bytes7'
        'bytes8'    | 'bytes8'
        'bytes32'   | 'bytes32'
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
        _ | 'bytes'
        _ | 'uint40'
        _ | 'int256'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        BytesType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'bytes_'
        _ | 'bytes0'
        _ | 'bytes37'
        _ | 'bytes128'
        _ | 'bytes3x'
    }

    def "should return a canonical string representation" () {
        expect:
        BytesType.DEFAULT.canonicalName == 'bytes32'
        BytesType.DEFAULT_ONE_BYTE.canonicalName == 'bytes1'
    }

    def "should encode & decode bytes"() {
        def obj = [bytes.size()] as BytesType
        def arr = bytes as byte[]

        when:
        def data = obj.encodeStatic arr
        def res = obj.decodeStatic data

        then:
        data == hex
        Arrays.equals res, arr

        where:
        bytes                       | hex
        [0x37]                      | Hex32.from('0x3700000000000000000000000000000000000000000000000000000000000000')
        [0x64, 0x61, 0x76, 0x65]    | Hex32.from('0x6461766500000000000000000000000000000000000000000000000000000000')
        [0x01] * 24                 | Hex32.from('0x0101010101010101010101010101010101010101010101010000000000000000')
        [0x12] * 32                 | Hex32.from('0x1212121212121212121212121212121212121212121212121212121212121212')
    }

    def "should catch empty data to decode"() {
        when:
        BytesType.DEFAULT.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first               | second
        BytesType.DEFAULT   | [] as BytesType
        BytesType.DEFAULT   | [32] as BytesType
        [24] as BytesType   | [24] as BytesType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first               | second
        BytesType.DEFAULT   | BytesType.DEFAULT
        BytesType.DEFAULT   | [] as BytesType
        BytesType.DEFAULT   | [32] as BytesType
        [24] as BytesType   | [24] as BytesType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first               | second
        BytesType.DEFAULT   | null
        BytesType.DEFAULT   | UIntType.DEFAULT
        BytesType.DEFAULT   | DynamicBytesType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        BytesType.DEFAULT as String == 'bytes32'
    }
}
