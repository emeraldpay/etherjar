package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class ReferenceTypeSpec extends Specification {

    static class ReferenceTypeImpl<T> implements ReferenceType<T, T> {

        @Override
        Type getWrappedType() {
            throw new UnsupportedOperationException()
        }

        @Override
        OptionalLong getFixedLength() {
            throw new UnsupportedOperationException()
        }

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

    final static DEFAULT_TYPE = [] as ReferenceTypeImpl

    def "should recognize static instance"() {
        def t = [
                getWrappedType: { [
                        isDynamic: { false },
                        getFixedSize: { Hex32.SIZE_BYTES as long },
                ] as Type },
                getFixedLength: { OptionalLong.of 123 },
        ] as ReferenceType

        expect:
        t.static
        t.fixedSize == 123 * Hex32.SIZE_BYTES
    }

    def "should recognize dynamic instance with fixed length "() {
        def t = [
                getWrappedType: { [
                        isDynamic: { true },
                        getFixedSize: { Hex32.SIZE_BYTES as long },
                ] as Type },
                getFixedLength: { OptionalLong.of 321 },
        ] as ReferenceType

        expect:
        t.dynamic
        t.fixedSize == 321 * Hex32.SIZE_BYTES
    }

    def "should recognize dynamic instance without fixed length "() {
        def t = [
                getWrappedType: { [
                        isDynamic: { true },
                ] as Type },
                getFixedLength: { OptionalLong.empty() },
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
