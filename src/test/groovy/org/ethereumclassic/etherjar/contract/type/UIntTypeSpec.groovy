package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class UIntTypeSpec extends Specification {

    final static DEFAULT_TYPE = [] as UIntType

    def "should create a default instance"() {
        expect:
        DEFAULT_TYPE.bytes == Hex32.SIZE_BYTES
        !DEFAULT_TYPE.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40] as UIntType

        expect:
        type.bytes == 5
        !type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as UIntType

        expect:
        type.minValue == BigInteger.ZERO

        where:
        bits << [8, 40, 64, 128, 256]
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as UIntType

        expect:
        type.maxValue == new BigInteger(str, 16)

        where:
        bits    | str
        8       | '+100'
        40      | '+10000000000'
        64      | '+10000000000000000'
        128     | '+100000000000000000000000000000000'
        256     | '+10000000000000000000000000000000000000000000000000000000000000000'
    }

    def "should accept visitor"() {
        def visitor = Mock(Type.Visitor)

        when:
        DEFAULT_TYPE.visit visitor

        then:
        1 * visitor.visit(DEFAULT_TYPE as UIntType)
        0 * _
    }

    def "should parse string representation"() {
        when:
        def opt = DEFAULT_TYPE.parse input

        then:
        opt.present
        opt.get().name == output

        where:
        input       | output
        'uint'      | 'uint256'
        'uint8'     | 'uint8'
        'uint40'    | 'uint40'
        'uint64'    | 'uint64'
        'uint128'   | 'uint128'
        'uint256'   | 'uint256'
    }

    def "should detect null string representation"() {
        when:
        DEFAULT_TYPE.parse null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = DEFAULT_TYPE.parse ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = DEFAULT_TYPE.parse input

        then:
        !opt.present

        where:
        _ | input
        _ | 'int16'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        DEFAULT_TYPE.parse input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'uint257'
        _ | 'uint1024'
    }

    def "should return a canonical string representation" () {
        def type  = [size] as UIntType

        expect:
        type.name == str

        where:
        size    | str
        8       | 'uint8'
        40      | 'uint40'
        64      | 'uint64'
        128     | 'uint128'
        256     | 'uint256'
    }

    def "should be converted to a string representation"() {
        def type = [64] as UIntType

        when:
        def str = type as String

        then:
        str ==~ /UIntType\{.+}/
        str.contains "bytes=8"
    }
}
