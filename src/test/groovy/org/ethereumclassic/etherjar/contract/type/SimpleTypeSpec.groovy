package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class SimpleTypeSpec extends Specification {

    static class SimpleTypeImpl<T> implements SimpleType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        Hex32 encodeStatic(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decodeStatic(Hex32 hex32) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as SimpleTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT.static
        DEFAULT.fixedSize == Hex32.SIZE_BYTES
    }

    def "should encode an object into hex data"() {
        def hex = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000123'

        def t = { hex } as SimpleType

        when:
        def x = t.encode 123

        then:
        x == hex
    }

    def "should decode hex data into an object"() {
        def hex = Hex32.from '0x0000000000000000000000000000000880000000000000000000000000000123'

        def t = { 123 } as SimpleType

        when:
        def obj = t.decode hex

        then:
        obj == 123
    }

    def "should catch empty or too long data to decode"() {
        when:
        DEFAULT.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | data
        _ | HexData.EMPTY
        _ | HexData.from('0x' + '12' * 48)
        _ | HexData.from('0x' + '00' * 64)
    }
}
