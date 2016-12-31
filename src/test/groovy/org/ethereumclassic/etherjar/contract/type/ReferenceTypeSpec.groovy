package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Specification

class ReferenceTypeSpec extends Specification {

    static class ReferenceTypeImpl<T> implements ReferenceType<T, T> {

        @Override
        Type getWrappedType() {
            throw new UnsupportedOperationException()
        }

        @Override
        OptionalInt getLength() {
            throw new UnsupportedOperationException()
        }

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

    final static DEFAULT_TYPE = [] as ReferenceTypeImpl

    def "should recognize static instance"() {
        def t = [
                getWrappedType: { [
                        isDynamic: { false },
                        getFixedSize: { Hex32.SIZE_BYTES },
                ] as Type },
                getLength: { OptionalInt.of 123 },
        ] as ReferenceType

        expect:
        t.static
        t.fixedSize == 123 * Hex32.SIZE_BYTES
    }

    def "should recognize dynamic instance with fixed length "() {
        def t = [
                getWrappedType: { [
                        isDynamic: { true },
                        getFixedSize: { Hex32.SIZE_BYTES },
                ] as Type },
                getLength: { OptionalInt.of 321 },
        ] as ReferenceType

        expect:
        t.dynamic
        t.fixedSize == 321 * Hex32.SIZE_BYTES
    }

    def "should recognize dynamic instance without fixed length "() {
        def t = [
                getWrappedType: { ({ true } as Type) },
                getLength: { OptionalInt.empty() },
        ] as ReferenceType

        expect:
        t.dynamic
        t.fixedSize == Hex32.SIZE_BYTES
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T, W> Boolean visit(ReferenceType<T, W> type) { true }
        }

        expect:
        DEFAULT_TYPE.visit visitor
    }
}
