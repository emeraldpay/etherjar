package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class StringTypeSpec extends Specification {

    final static DEFAULT_TYPE = [] as StringType

    def "should parse string representation"() {
        when:
        def opt = StringType.from 'string'

        then:
        opt.present
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
        DEFAULT_TYPE.canonicalName == 'string'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first           | second
        DEFAULT_TYPE    | [] as StringType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first           | second
        DEFAULT_TYPE    | DEFAULT_TYPE
        DEFAULT_TYPE    | [] as StringType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first           | second
        DEFAULT_TYPE    | null
        DEFAULT_TYPE    | 'ABC'
        DEFAULT_TYPE    | new UIntType()
    }

    def "should be converted to a string representation"() {
        expect:
        DEFAULT_TYPE as String == 'string'
    }
}
