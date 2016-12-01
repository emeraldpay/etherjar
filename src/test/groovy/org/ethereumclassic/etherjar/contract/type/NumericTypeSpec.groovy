package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

class NumericTypeSpec extends Specification {

    static class NumericTypeImpl extends NumericType {

        protected NumericTypeImpl() {
            super()
        }

        protected NumericTypeImpl(int bits) {
            super(bits)
        }

        protected NumericTypeImpl(int bits, boolean signed) {
            super(bits, signed)
        }

        @Override
        BigInteger getMinValue() {
            throw new UnsupportedOperationException()
        }

        @Override
        BigInteger getMaxValue() {
            throw new UnsupportedOperationException()
        }

        @Override
        Optional<? extends Type<BigInteger>> parse(String str) {
            throw new UnsupportedOperationException()
        }

        @Override
        String getName() {
            throw new UnsupportedOperationException()
        }
    }

    final static NumericType DEFAULT_TYPE = [] as NumericTypeImpl

    def "should create a default instance"() {
        expect:
        DEFAULT_TYPE.bytes == Hex32.SIZE_BYTES
        DEFAULT_TYPE.bits == Hex32.SIZE_BYTES << 3
        !DEFAULT_TYPE.signed
    }

    def "should create an instance with specified number of bits"() {
        def obj = [16] as NumericTypeImpl

        expect:
        obj.bytes == 2
        obj.bits == 16
        !obj.signed
    }

    def "should create an unsigned instance with specified number of bits"() {
        def obj = [24, true] as NumericTypeImpl

        expect:
        obj.bytes == 3
        obj.bits == 24
        obj.signed
    }

    def "should prevent from incorrect number of bits"() {
        when:
        new NumericTypeImpl(bits)

        then:
        thrown IllegalArgumentException

        where:
        _ | bits
        _ | -1
        _ | 0
        _ | 1
        _ | 2
        _ | 3
        _ | 9
        _ | 31
        _ | 129
        _ | 257
    }

    def "should check value validity"() {
        def obj = [
                getMinValue: BigInteger.ZERO,
                getMaxValue: BigInteger.TEN,
        ] as NumericTypeImpl

        expect:
        obj.isValueValid value

        where:
        _ | value
        _ | BigInteger.ZERO
        _ | BigInteger.ONE
        _ | BigInteger.TEN.subtract(BigInteger.ONE)
    }

    def "should check value invalidity"() {
        def obj = [
                getMinValue: BigInteger.ZERO,
                getMaxValue: BigInteger.ONE,
        ] as NumericTypeImpl

        expect:
        !obj.isValueValid(value)

        where:
        _ | value
        _ | BigInteger.ONE
        _ | BigInteger.ONE.add(BigInteger.TEN)
        _ | BigInteger.ZERO.subtract(BigInteger.ONE)
    }

    def "should accept visitor"() {
        def visitor = Mock(Type.Visitor)

        when:
        DEFAULT_TYPE.visit visitor

        then:
        1 * visitor.visit(DEFAULT_TYPE as NumericType)
        0 * _
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        DEFAULT_TYPE                    | [] as NumericTypeImpl
        DEFAULT_TYPE                    | [256] as NumericTypeImpl
        [64, true] as NumericTypeImpl   | [64, true] as NumericTypeImpl
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                           | second
        DEFAULT_TYPE                    | DEFAULT_TYPE
        DEFAULT_TYPE                    | [] as NumericTypeImpl
        DEFAULT_TYPE                    | [256] as NumericTypeImpl
        [64, true] as NumericTypeImpl   | [64, true] as NumericTypeImpl
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first           | second
        DEFAULT_TYPE    | null
        DEFAULT_TYPE    | [64, true] as NumericTypeImpl
        DEFAULT_TYPE    | new UIntType()
    }

    def "should encode & decode numeric values"() {
        def obj = new NumericTypeImpl(bits, sign) {

            @Override
            BigInteger getMinValue() {
                return isSigned() ?
                        new BigInteger('-8' + '0' * 63, 16) : BigInteger.ZERO
            }

            @Override
            BigInteger getMaxValue() {
                return isSigned() ?
                        new BigInteger('+8' + '0' * 63, 16) : new BigInteger('+1' + '0' * 64, 16)
            }
        }

        def val = new BigInteger(str, 16)

        when:
        def data = obj.encode val
        def res = obj.decode data

        then:
        data.length == 1
        data[0].toHex() == hex
        res == val

        where:
        bits    | sign  | str       | hex
        8       | false | '+0'      | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | false | '+1'      | '0x0000000000000000000000000000000000000000000000000000000000000001'
        8       | false | '+10'     | '0x0000000000000000000000000000000000000000000000000000000000000010'
        8       | false | '+ff'     | '0x00000000000000000000000000000000000000000000000000000000000000ff'
        8       | true  | '-0'      | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | true  | '+0'      | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | true  | '-1'      | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        8       | true  | '+1'      | '0x0000000000000000000000000000000000000000000000000000000000000001'
        8       | true  | '-11'     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffef'
        8       | true  | '+12'     | '0x0000000000000000000000000000000000000000000000000000000000000012'
        8       | true  | '-80'     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80'
        8       | true  | '+7f'     | '0x000000000000000000000000000000000000000000000000000000000000007f'

        16      | false | '+1234'   | '0x0000000000000000000000000000000000000000000000000000000000001234'
        16      | false | '+ffff'   | '0x000000000000000000000000000000000000000000000000000000000000ffff'
        16      | true  | '-1234'   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffedcc'
        16      | true  | '+4321'   | '0x0000000000000000000000000000000000000000000000000000000000004321'
        16      | true  | '-8000'   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000'
        16      | true  | '+7fff'   | '0x0000000000000000000000000000000000000000000000000000000000007fff'

        40      | false | '+1122334455' | '0x0000000000000000000000000000000000000000000000000000001122334455'
        40      | false | '+ffffffffff' | '0x000000000000000000000000000000000000000000000000000000ffffffffff'
        40      | true  | '-1122334455' | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffeeddccbbab'
        40      | true  | '+5544332211' | '0x0000000000000000000000000000000000000000000000000000005544332211'
        40      | true  | '-8000000000' | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffff8000000000'
        40      | true  | '+7fffffffff' | '0x0000000000000000000000000000000000000000000000000000007fffffffff'

        64      | false | '+1122334455667788'   | '0x0000000000000000000000000000000000000000000000001122334455667788'
        64      | false | '+ffffffffffffffff'   | '0x000000000000000000000000000000000000000000000000ffffffffffffffff'
        64      | true  | '-1122334455667788'   | '0xffffffffffffffffffffffffffffffffffffffffffffffffeeddccbbaa998878'
        64      | true  | '+1122334455667788'   | '0x0000000000000000000000000000000000000000000000001122334455667788'
        64      | true  | '-8000000000000000'   | '0xffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000'
        64      | true  | '+7fffffffffffffff'   | '0x0000000000000000000000000000000000000000000000007fffffffffffffff'

        120     | false | '+112233445566778899aabbccddeeff' | '0x0000000000000000000000000000000000112233445566778899aabbccddeeff'
        120     | false | '+ffffffffffffffffffffffffffffff' | '0x0000000000000000000000000000000000ffffffffffffffffffffffffffffff'
        120     | true  | '-112233445566778899aabbccddeeff' | '0xffffffffffffffffffffffffffffffffffeeddccbbaa99887766554433221101'
        120     | true  | '+112233445566778899aabbccddeeff' | '0x0000000000000000000000000000000000112233445566778899aabbccddeeff'
        120     | true  | '-800000000000000000000000000000' | '0xffffffffffffffffffffffffffffffffff800000000000000000000000000000'
        120     | true  | '+7fffffffffffffffffffffffffffff' | '0x00000000000000000000000000000000007fffffffffffffffffffffffffffff'

        256     | false | '+112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'   | '0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'
        256     | false | '+ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        256     | true  | '-112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'   | '0xeeddccbbaa99887766554433221100eeddccbbaa99887766554433221100eede'
        256     | true  | '+112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'   | '0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'
        256     | true  | '-8000000000000000000000000000000000000000000000000000000000000000'   | '0x8000000000000000000000000000000000000000000000000000000000000000'
        256     | true  | '+7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'   | '0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
    }

    def "should catch out of range before encoding"() {
        def obj = [
                getMinValue: BigInteger.ZERO,
                getMaxValue: BigInteger.TEN
        ] as NumericTypeImpl

        when:
        obj.encode value

        then:
        thrown IllegalArgumentException

        where:
        _ | value
        _ | BigInteger.TEN
        _ | BigInteger.ZERO.subtract(BigInteger.ONE)
    }

    def "should catch out of range after decoding"() {
        def obj = new NumericTypeImpl(bits, sign) {

            @Override
            BigInteger getMinValue() {
                return BigInteger.ZERO
            }

            @Override
            BigInteger getMaxValue() {
                return BigInteger.valueOf(256)
            }
        }

        when:
        obj.decode([Hex32.from(hex)] as Hex32[])

        then:
        thrown IllegalArgumentException

        where:
        bits    |sign   | hex
        8       | true  | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        16      | false | '0x000000000000000000000000000000000000000000000000000000000000ffff'
    }

    def "should catch not single data"() {
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
