package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class UFixedTypeTest extends Specification {

    def "should detect null string representation"() {
        when:
        UFixedType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = UFixedType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = UFixedType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'ufxed'
        _ | 'ufexid<8>x<40>'
        _ | 'uint16'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        UFixedType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'ufixed<-1>x<-1>'
        _ | 'ufixed<-1>x<8>'
        _ | 'ufixed<1>x<1>'
        _ | 'ufixed<0>x<128>'
        _ | 'ufixed<256>x<8>'
    }

    def "should create a correct default instance"() {
        expect:
        UFixedType.DEFAULT_TYPE.MBits == 128
        UFixedType.DEFAULT_TYPE.NBits == 128
        UFixedType.DEFAULT_TYPE.bits == 256
        !UFixedType.DEFAULT_TYPE.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40, 8] as UFixedType

        expect:
        type.MBits == 40
        type.NBits == 8
        type.bits == 48
        !type.signed
    }

    def "should parse string representation"() {
        when:
        def opt = UFixedType.from input

        then:
        opt.present
        opt.get().canonicalName == output

        where:
        input               | output
        'ufixed'            | 'ufixed<128>x<128>'
        'ufixed<8>x<8>'     | 'ufixed<8>x<8>'
        'ufixed<64>x<64>'   | 'ufixed<64>x<64>'
        'ufixed<64>x<8>'    | 'ufixed<64>x<8>'
        'ufixed<40>x<120>'  | 'ufixed<40>x<120>'
        'ufixed<128>x<128>' | 'ufixed<128>x<128>'
    }
}
