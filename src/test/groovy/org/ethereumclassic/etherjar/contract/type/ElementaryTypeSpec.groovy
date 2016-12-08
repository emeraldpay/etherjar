package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

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
