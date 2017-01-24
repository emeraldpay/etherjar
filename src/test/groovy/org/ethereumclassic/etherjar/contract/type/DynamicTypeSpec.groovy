package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class DynamicTypeSpec extends Specification {

    static class DynamicTypeImpl<T> implements DynamicType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        HexData encode(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decode(HexData data) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as DynamicTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT.dynamic
        DEFAULT.fixedSize == Hex32.SIZE_BYTES
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T> Boolean visit(DynamicType<T> type) { true }
        }

        expect:
        DEFAULT.visit visitor
    }
}
