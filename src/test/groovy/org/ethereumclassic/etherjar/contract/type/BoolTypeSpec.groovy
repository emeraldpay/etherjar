package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class BoolTypeSpec extends Specification {

    final static DEFAULT_TYPE = [] as BoolType

    def "should parse string representation"() {
        when:
        def opt = BoolType.from 'bool'

        then:
        opt.present
    }

    def "should detect null string representation"() {
        when:
        BoolType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = BoolType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = BoolType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint40'
        _ | 'int256'
    }

    def "should create a default instance"() {
        expect:
        DEFAULT_TYPE.bytes == 1
        !DEFAULT_TYPE.signed
    }

    def "should return a minimal value (inclusive)"() {
        expect:
        DEFAULT_TYPE.minValue == BigInteger.ZERO
    }

    def "should return a maximal value (exclusive)"() {
        expect:
        DEFAULT_TYPE.maxValue == BigInteger.valueOf(2)
    }

    def "should accept visitor"() {
        def visitor = Mock(Type.Visitor)

        when:
        DEFAULT_TYPE.visit visitor

        then:
        1 * visitor.visit(DEFAULT_TYPE as BoolType)
        0 * _
    }

    def "should return a canonical string representation" () {
        expect:
        DEFAULT_TYPE.canonicalName == 'bool'
    }

    def "should be converted to a string representation"() {
        when:
        def str = DEFAULT_TYPE as String

        then:
        str == 'BoolType{}'
    }
}