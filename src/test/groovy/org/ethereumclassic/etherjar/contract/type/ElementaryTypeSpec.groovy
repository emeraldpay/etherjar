package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

import java.util.function.Function

class ElementaryTypeSpec extends Specification {

    static class ElementaryTypeImpl<T> implements ElementaryType<T> {

        @Override
        Hex32 singleEncode(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T singleDecode(Hex32 hex32) {
            throw new UnsupportedOperationException()
        }

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }
    }

    final static ElementaryType<?> DEFAULT_TYPE = [] as ElementaryTypeImpl

    def "should create a default instance"() {
        expect:
        !DEFAULT_TYPE.dynamic
        DEFAULT_TYPE.encodedSize == Hex32.SIZE_BYTES
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            Boolean visit(ElementaryType type) { true }
        }

        expect:
        DEFAULT_TYPE.visit visitor
    }

    def "should encode an object into a single array"() {
        def hex = Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000123')

        def m = Mock(Function) {
            1 * apply(123) >> hex
            0 * _
        }

        def t = [ singleEncode: { m.apply it } ] as ElementaryType

        when:
        def arr = t.encode 123

        then:
        arr.length == 1
        arr[0] == hex
    }

    def "should decode a single array into an object"() {
        def hex = Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000123')

        def m = Mock(Function) {
            1 * apply(hex) >> 123
            0 * _
        }

        def t = [ singleDecode: { m.apply it } ] as ElementaryType

        when:
        def obj = t.decode([hex] as Hex32[])

        then:
        obj == 123
    }

    def "should catch not single data to decode"() {
        when:
        DEFAULT_TYPE.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | data
        _ | new Hex32[0]
        _ | new Hex32[2]
    }
}
