package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class IntTypeSpec extends Specification {

    final static DEFAULT_TYPE = [] as IntType

    def "should parse string representation"() {
        when:
        def opt = IntType.from input

        then:
        opt.present
        opt.get().canonicalName == output

        where:
        input       | output
        'int'       | 'int256'
        'int8'      | 'int8'
        'int40'     | 'int40'
        'int64'     | 'int64'
        'int128'    | 'int128'
        'int256'    | 'int256'
    }

    def "should detect null string representation"() {
        when:
        IntType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = IntType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = IntType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint40'
        _ | 'xint140'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        IntType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'int257'
        _ | 'int1024'
        _ | 'int140x'
    }

    def "should detect negative bits before min value calculation"() {
        when:
        IntType.minValue(-1)

        then:
        thrown IllegalArgumentException
    }

    def "should detect negative bits before max value calculation"() {
        when:
        IntType.maxValue(-2)

        then:
        thrown IllegalArgumentException
    }

    def "should create a default instance"() {
        expect:
        DEFAULT_TYPE.bytes == Hex32.SIZE_BYTES
        !DEFAULT_TYPE.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40] as IntType

        expect:
        type.bytes == 5
        !type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as IntType

        expect:
        type.minValue == new BigInteger(str, 16)

        where:
        bits    | str
        8       | '-80'
        40      | '-8000000000'
        64      | '-8000000000000000'
        128     | '-80000000000000000000000000000000'
        256     | '-8000000000000000000000000000000000000000000000000000000000000000'
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as IntType

        expect:
        type.maxValue == new BigInteger(str, 16)

        where:
        bits    | str
        8       | '+80'
        40      | '+8000000000'
        64      | '+8000000000000000'
        128     | '+80000000000000000000000000000000'
        256     | '+8000000000000000000000000000000000000000000000000000000000000000'
    }

    def "should return a canonical string representation" () {
        def type  = [size] as IntType

        expect:
        type.canonicalName == str

        where:
        size    | str
        8       | 'int8'
        40      | 'int40'
        64      | 'int64'
        128     | 'int128'
        256     | 'int256'
    }

    def "should be converted to a string representation"() {
        def type = [64] as IntType

        when:
        def str = type as String

        then:
        str ==~ /IntType\{.+}/
        str.contains "bytes=8"
    }
}
