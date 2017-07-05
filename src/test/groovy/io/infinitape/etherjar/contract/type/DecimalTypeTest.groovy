package io.infinitape.etherjar.contract.type

import spock.lang.Specification

class DecimalTypeTest extends Specification {

    static class DecimalTypeImpl extends DecimalType {

        DecimalTypeImpl() {
            super(128, 128)
        }

        DecimalTypeImpl(int bits) {
            super(bits, bits)
        }

        DecimalTypeImpl(int mBits, int nBits) {
            super(mBits, nBits)
        }

        @Override
        BigDecimal getMinValue() {
            throw new UnsupportedOperationException()
        }

        @Override
        BigDecimal getMaxValue() {
            throw new UnsupportedOperationException()
        }

        @Override
        NumericType getNumericType() {
            throw new UnsupportedOperationException()
        }

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as DecimalTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT.MBits == 128
        DEFAULT.NBits == 128
        DEFAULT.bits == 256
    }

    def "should create an unsigned instance with specified number of bits"() {
        def obj = new DecimalTypeImpl(8, 16) {

            @Override
            NumericType getNumericType() {
                UIntType.DEFAULT
            }
        }

        expect:
        obj.MBits == 8
        obj.NBits == 16
        obj.bits == 24
        !obj.signed
    }

    def "should create a signed instance with specified number of bits"() {
        def obj = new DecimalTypeImpl(24, 64) {

            @Override
            NumericType getNumericType() {
                IntType.DEFAULT
            }
        }

        expect:
        obj.MBits == 24
        obj.NBits == 64
        obj.bits == 88
        obj.signed
    }

    def "should prevent from incorrect number of side bits"() {
        when:
        new DecimalTypeImpl(bits, 8)

        then:
        thrown IllegalArgumentException

        when:
        new DecimalTypeImpl(8, bits)

        then:
        thrown IllegalArgumentException

        where:
        _ | bits
        _ | -8
        _ | -1
        _ | 0
        _ | 1
        _ | 7
        _ | 21
        _ | 123
        _ | 137
        _ | 256
    }

    def "should prevent from incorrect number of total bits"() {
        when:
        new DecimalTypeImpl(m, n)

        then:
        thrown IllegalArgumentException

        where:
        m   | n
        128 | 136
        144 | 120
        256 | 256
    }

    def "should check value validity"() {
        def obj = [
                getMinValue: 0.0G,
                getMaxValue: 10.0G,
        ] as DecimalTypeImpl

        expect:
        obj.isValueValid value

        where:
        _ | value
        _ | 0.0G
        _ | 1.2G
        _ | 9.3G
    }

    def "should check value invalidity"() {
        def obj = [
                getMinValue: 0.0G,
                getMaxValue: 1.0G,
        ] as DecimalTypeImpl

        expect:
        !obj.isValueValid(value)

        where:
        _ | value
        _ | -1.3G
        _ | 1.0G
        _ | 11.9G
    }

    def "should encode double values"() {
        def obj = [
                getMinValue: -0x80000000000000000000000000000000 as BigDecimal,
                getMaxValue: 0x80000000000000000000000000000000 as BigDecimal,
                getNumericType: IntType.DEFAULT,
        ] as DecimalTypeImpl

        when:
        def data = obj.encode val

        then:
        data.toHex() == hex

        where:
        val     | hex
        0.0D    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8.5D    | '0x0000000000000000000000000000000880000000000000000000000000000000'
        2.125D  | '0x0000000000000000000000000000000220000000000000000000000000000000'
    }

    def "should encode & decode "() {
        def type = sign ?
                new IntType(m + n) : new UIntType(m + n)

        def obj = new DecimalTypeImpl(m, n) {

            @Override
            boolean isValueValid(BigDecimal value) {
                true
            }

            @Override
            NumericType getNumericType() {
                type
            }
        }

        when:
        def data = obj.encodeSimple(val as BigDecimal)
        def res = obj.decodeSimple data

        then:
        data.toHex() == hex
        res == val as BigDecimal

        where:
        m   | n | sign  | val           | hex
        8   | 8 | true  | -0.5          | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80'
        8   | 8 | true  | 0.5           | '0x0000000000000000000000000000000000000000000000000000000000000080'
        8   | 8 | true  | -1.0          | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00'
        8   | 8 | true  | 1.0           | '0x0000000000000000000000000000000000000000000000000000000000000100'
        8   | 8 | true  | -1.5          | '0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe80'
        8   | 8 | true  | 1.5           | '0x0000000000000000000000000000000000000000000000000000000000000180'
        8   | 8 | true  | -123.625      | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8460'
        8   | 8 | true  | 123.625       | '0x0000000000000000000000000000000000000000000000000000000000007ba0'
        8   | 8 | true  | -0.00390625   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        8   | 8 | true  | 0.00390625    | '0x0000000000000000000000000000000000000000000000000000000000000001'
        8   | 8 | true  | -128          | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000'
        8   | 8 | true  | -127.99609375 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8001'
        8   | 8 | true  | -127.00390625 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80ff'
        8   | 8 | true  | 127.00390625  | '0x0000000000000000000000000000000000000000000000000000000000007f01'
        8   | 8 | true  | 127.99609375  | '0x0000000000000000000000000000000000000000000000000000000000007fff'
        8   | 8 | false | 128           | '0x0000000000000000000000000000000000000000000000000000000000008000'
        8   | 8 | false | 211.83203125  | '0x000000000000000000000000000000000000000000000000000000000000d3d5'
        8   | 8 | false | 255.99609375  | '0x000000000000000000000000000000000000000000000000000000000000ffff'

        16  | 16    | true  | -32767.5                  | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffff80008000'
        16  | 16    | true  | 32766.5                   | '0x000000000000000000000000000000000000000000000000000000007ffe8000'
        16  | 16    | true  | -32768                    | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000000'
        16  | 16    | true  | -32767.9999847412109375   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000001'
        16  | 16    | true  | -32767.0000152587890625   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000ffff'
        16  | 16    | true  | 32767.0000152587890625    | '0x000000000000000000000000000000000000000000000000000000007fff0001'
        16  | 16    | true  | 32767.9999847412109375    | '0x000000000000000000000000000000000000000000000000000000007fffffff'
        16  | 16    | false | 32768                     | '0x0000000000000000000000000000000000000000000000000000000080000000'
        16  | 16    | false | 65535.9999847412109375    | '0x00000000000000000000000000000000000000000000000000000000ffffffff'

        128 | 128   | true  | -0.5  | '0xffffffffffffffffffffffffffffffff80000000000000000000000000000000'
        128 | 128   | true  | 0.5   | '0x0000000000000000000000000000000080000000000000000000000000000000'
        128 | 128   | true  | -1.5  | '0xfffffffffffffffffffffffffffffffe80000000000000000000000000000000'
        128 | 128   | true  | 1.5   | '0x0000000000000000000000000000000180000000000000000000000000000000'
    }

    def "should round before encode"() {
        def obj = new DecimalTypeImpl(16) {

            @Override
            BigDecimal getMinValue() {
                0
            }

            @Override
            BigDecimal getMaxValue() {
                65536
            }

            @Override
            NumericType getNumericType() {
                new UIntType(32)
            }
        }

        when:
        def data = obj.encodeSimple(before as BigDecimal)
        def res = obj.decodeSimple data

        then:
        res == after

        where:
        before                  | after
        64.123456789            | 64.1234588623046875
        65535.9999847412109370  | 65535.9999847412109375
        65535.9999847412109377  | 65535.9999847412109375
        65535.9999999999999999  | 65535.9999847412109375
    }

    def "should catch out of range before encoding"() {
        def obj = [
                isValueValid: false,
        ] as DecimalTypeImpl

        when:
        obj.encodeSimple 0.0G

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                       | second
        DEFAULT                     | [] as DecimalTypeImpl
        DEFAULT                     | [128] as DecimalTypeImpl
        [64, 24] as DecimalTypeImpl | [64, 24] as DecimalTypeImpl
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                       | second
        DEFAULT                     | DEFAULT
        DEFAULT                     | [] as DecimalTypeImpl
        DEFAULT                     | [128] as DecimalTypeImpl
        [64, 24] as DecimalTypeImpl | [64, 24] as DecimalTypeImpl
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first      | second
        DEFAULT    | null
        DEFAULT    | [64, 24] as DecimalTypeImpl
        DEFAULT    | BoolType.DEFAULT
    }

    def "should be converted to a string representation"() {
        def obj = [
                getCanonicalName: 'impl'
        ] as DecimalTypeImpl

        expect:
        obj as String == 'impl'
    }
}
