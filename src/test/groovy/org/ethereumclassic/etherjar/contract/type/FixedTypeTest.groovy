package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class FixedTypeTest extends Specification {

    def "should parse string representation"() {
        when:
        def opt = FixedType.from input

        then:
        opt.present
        opt.get().canonicalName == output

        where:
        input               | output
        'fixed'             | 'fixed128x128'
        'fixed8x8'      | 'fixed8x8'
        'fixed64x64'    | 'fixed64x64'
        'fixed64x8'     | 'fixed64x8'
        'fixed40x120'   | 'fixed40x120'
        'fixed128x128'  | 'fixed128x128'
    }

    def "should detect null string representation"() {
        when:
        FixedType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = FixedType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = FixedType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'fxed'
        _ | 'fexid8x40'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        FixedType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'fixed-1x-1'
        _ | 'fixed-1x8'
        _ | 'fixed8x1'
        _ | 'fixed0x128'
        _ | 'fixed256x8'
    }

    def "should create a correct default instance"() {
        expect:
        FixedType.DEFAULT.MBits == 128
        FixedType.DEFAULT.NBits == 128
        FixedType.DEFAULT.bits == 256
        FixedType.DEFAULT.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40, 8] as FixedType

        expect:
        type.MBits == 40
        type.NBits == 8
        type.bits == 48
        type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as FixedType

        expect:
        type.minValue == val as BigDecimal

        where:
        bits    | val
        8       | -0x80G
        40      | -0x8000000000G
        64      | -0x8000000000000000G
        128     | -0x80000000000000000000000000000000G
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as FixedType

        expect:
        type.maxValue == val as BigDecimal

        where:
        bits    | val
        8       | 0x80G
        40      | 0x8000000000G
        64      | 0x8000000000000000G
        128     | 0x80000000000000000000000000000000G
    }
}
