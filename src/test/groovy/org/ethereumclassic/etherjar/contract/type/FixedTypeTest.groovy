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
        FixedType.DEFAULT_TYPE.MBits == 128
        FixedType.DEFAULT_TYPE.NBits == 128
        FixedType.DEFAULT_TYPE.bits == 256
        FixedType.DEFAULT_TYPE.signed
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
