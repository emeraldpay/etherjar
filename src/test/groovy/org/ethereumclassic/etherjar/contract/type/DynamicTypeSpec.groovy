package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class DynamicTypeSpec extends Specification {

    static class DynamicTypeImpl<T> implements DynamicType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        List<Hex32> encode(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decode(Collection<? extends Hex32> data) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT_TYPE = [] as DynamicTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT_TYPE.dynamic
        DEFAULT_TYPE.fixedSize == Hex32.SIZE_BYTES
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T> Boolean visit(DynamicType<T> type) { true }
        }

        expect:
        DEFAULT_TYPE.visit visitor
    }
}
