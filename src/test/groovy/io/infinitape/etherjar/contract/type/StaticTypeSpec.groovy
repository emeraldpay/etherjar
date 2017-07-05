package io.infinitape.etherjar.contract.type

import io.infinitape.etherjar.model.HexData
import spock.lang.Specification

class StaticTypeSpec extends Specification {

    static class StaticTypeImpl<T> implements StaticType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        int getFixedSize() {
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

    final static DEFAULT = [] as StaticTypeImpl<Void>

    def "should create a correct default instance"() {
        expect:
        DEFAULT.static
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T> Boolean visit(StaticType<T> type) { true }
        }

        expect:
        DEFAULT.visit visitor
    }
}
