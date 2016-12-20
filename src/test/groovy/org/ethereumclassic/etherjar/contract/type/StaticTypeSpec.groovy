package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

import java.util.function.Function

class StaticTypeSpec extends Specification {

    static class StaticTypeImpl<T> implements StaticType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        Hex32 encodeSingle(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decodeSingle(Hex32 hex32) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT_TYPE = [] as StaticTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT_TYPE.static
        DEFAULT_TYPE.fixedSize == Hex32.SIZE_BYTES
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T> Boolean visit(StaticType<T> type) { true }
        }

        expect:
        DEFAULT_TYPE.visit visitor
    }

    def "should encode an object into a singleton list"() {
        def hex = Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000123')

        def m = Stub(Function) { apply(123) >> hex }

        def t = [encodeSingle: { m.apply it }] as StaticType

        when:
        def arr = t.encode 123

        then:
        arr.size() == 1
        arr[0] == hex
    }

    def "should decode a singleton collection into an object"() {
        def hex = Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000123')

        def m = Stub(Function) { apply(hex) >> 123 }

        def t = [decodeSingle: { m.apply it }] as StaticType

        when:
        def obj = t.decode hex

        then:
        obj == 123
    }

    def "should catch empty or not single data to decode"() {
        when:
        DEFAULT_TYPE.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | data
        _ | []
        _ | [Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000000')] * 2
    }
}
