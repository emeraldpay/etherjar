package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class FixedTypeTest extends Specification {

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
        _ | 'fexid<8>x<40>'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        FixedType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'fixed<-1>x<-1>'
        _ | 'fixed<-1>x<8>'
        _ | 'fixed<1>x<1>'
        _ | 'fixed<0>x<128>'
        _ | 'fixed<256>x<8>'
    }

    def "should create a correct default instance"() {
        expect:
        FixedType.DEFAULT.MBits == 128
        FixedType.DEFAULT.NBits == 128
        FixedType.DEFAULT.bits == 256
        FixedType.DEFAULT.signed
    }

    def "should return max value"() {
        when:
        FixedType obj = new FixedType(M, N)

        then:
        obj.maxValue == new BigDecimal(str)

        where:
        M   | N   | str
        8   | 8   | '128'
        64  | 64  | '9223372036854775808'
        128 | 128 | '170141183460469231731687303715884105728'
        40  | 8   | '549755813888'
        8   | 40  | '128'
    }

    def "should return min value"() {
        when:
        FixedType obj = new FixedType(M, N)

        then:
        obj.minValue == new BigDecimal(str)

        where:
        M   | N   | str
        8   | 8   | '-128'
        64  | 64  | '-9.223372036854775808e+18'
        128 | 128 | '-1.70141183460469231731687303715884105728e+38'
        40  | 8   | '-549755813888'
        8   | 40  | '-128'
    }

    def "should create an instance with specified number of bits"() {
        def type = [40, 8] as FixedType

        expect:
        type.MBits == 40
        type.NBits == 8
        type.bits == 48
        type.signed
    }

    def "should parse string representation"() {
        when:
        def opt = FixedType.from input

        then:
        opt.present
        opt.get().canonicalName == output

        where:
        input               | output
        'fixed'             | 'fixed<128>x<128>'
        'fixed<8>x<8>'      | 'fixed<8>x<8>'
        'fixed<64>x<64>'    | 'fixed<64>x<64>'
        'fixed<64>x<8>'     | 'fixed<64>x<8>'
        'fixed<40>x<120>'   | 'fixed<40>x<120>'
        'fixed<128>x<128>'  | 'fixed<128>x<128>'
    }
}
