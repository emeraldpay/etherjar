/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.emeraldpay.etherjar.abi

import io.emeraldpay.etherjar.hex.Hex32
import spock.lang.Specification

class NumericTypeSpec extends Specification {

    static class NumericTypeImpl extends NumericType {

        protected NumericTypeImpl() {
            super(256, false)
        }

        protected NumericTypeImpl(int bits) {
            super(bits, false)
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
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as NumericTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT.bits == 256
        !DEFAULT.signed
    }

    def "should return a power of two"() {
        def res = NumericType.powerOfTwo bits

        expect:
        res == pow

        where:
        bits    | pow
        0       | 1G
        1       | 2G
        2       | 4G
        3       | 8G
        8       | 256G
        21      | 2097152G
        128     | 340282366920938463463374607431768211456G
        253     | 14474011154664524427946373126085988481658748083205070504932198000989141204992G
        256     | 115792089237316195423570985008687907853269984665640564039457584007913129639936G
    }

    def "should detect negative bits before calculate a power of two"() {
        when:
        NumericType.powerOfTwo bits

        then:
        thrown IllegalArgumentException

        where:
        _ | bits
        _ | -1
        _ | -2
        _ | -8
    }

    def "should create an unsigned instance with specified number of bits"() {
        def obj = [16, false] as NumericTypeImpl

        expect:
        obj.bits == 16
        !obj.signed
    }

    def "should create a signed instance with specified number of bits"() {
        def obj = [24, true] as NumericTypeImpl

        expect:
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
                getMinValue: 0G,
                getMaxValue: 10G,
        ] as NumericTypeImpl

        expect:
        obj.isValueValid value

        where:
        _ | value
        _ | 0G
        _ | 1G
        _ | 9G
    }

    def "should check value invalidity"() {
        def obj = [
                getMinValue: 0G,
                getMaxValue: 1G,
        ] as NumericTypeImpl

        expect:
        !obj.isValueValid(value)

        where:
        _ | value
        _ | -1G
        _ | 1G
        _ | 11G
    }

    def "should encode long values"() {
        def obj = new NumericTypeImpl(128, true) {

            @Override
            boolean isValueValid(BigInteger value) {
                true
            }
        }

        when:
        def data = obj.encode val

        then:
        data.toHex() == hex

        where:
        val                 | hex
        0                   | '0x0000000000000000000000000000000000000000000000000000000000000000'
        -64                 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc0'
        64                  | '0x0000000000000000000000000000000000000000000000000000000000000040'
        Integer.MIN_VALUE   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffff80000000'
        Integer.MAX_VALUE   | '0x000000000000000000000000000000000000000000000000000000007fffffff'
        Long.MIN_VALUE      | '0xffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000'
        Long.MAX_VALUE      | '0x0000000000000000000000000000000000000000000000007fffffffffffffff'
    }

    def "should encode & decode numeric values"() {
        def obj = new NumericTypeImpl(bits, sign) {

            @Override
            boolean isValueValid(BigInteger value) {
                true
            }

            @Override
            BigInteger getMaxValue() {
                0x10000000000000000000000000000000000000000000000000000000000000000
            }
        }

        when:
        def data = obj.encodeSimple(val as BigInteger)
        def res = obj.decodeSimple data

        then:
        data.toHex() == hex
        res == val as BigInteger

        where:
        bits    | sign  | val       | hex
        8       | false | 0x00      | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | false | 0x01      | '0x0000000000000000000000000000000000000000000000000000000000000001'
        8       | false | 0x10      | '0x0000000000000000000000000000000000000000000000000000000000000010'
        8       | false | 0x64      | '0x0000000000000000000000000000000000000000000000000000000000000064'
        8       | false | 0xff      | '0x00000000000000000000000000000000000000000000000000000000000000ff'
        8       | true  | -0x00     | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | true  | 0x00      | '0x0000000000000000000000000000000000000000000000000000000000000000'
        8       | true  | -0x01     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        8       | true  | +0x01     | '0x0000000000000000000000000000000000000000000000000000000000000001'
        8       | true  | -0x11     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffef'
        8       | true  | 0x12      | '0x0000000000000000000000000000000000000000000000000000000000000012'
        8       | true  | -0x64     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        8       | true  | 0x64      | '0x0000000000000000000000000000000000000000000000000000000000000064'
        8       | true  | -0x80     | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80'
        8       | true  | 0x7f      | '0x000000000000000000000000000000000000000000000000000000000000007f'

        16      | false | 0x0000    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        16      | false | 0x0001    | '0x0000000000000000000000000000000000000000000000000000000000000001'
        16      | false | 0x0064    | '0x0000000000000000000000000000000000000000000000000000000000000064'
        16      | true  | -0x0000   | '0x0000000000000000000000000000000000000000000000000000000000000000'
        16      | true  | 0x0000    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        16      | true  | -0x0001   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        16      | true  | 0x0001    | '0x0000000000000000000000000000000000000000000000000000000000000001'
        16      | true  | -0x0064   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        16      | true  | 0x0064    | '0x0000000000000000000000000000000000000000000000000000000000000064'
        16      | false | 0x0647    | '0x0000000000000000000000000000000000000000000000000000000000000647'
        16      | false | 0x1234    | '0x0000000000000000000000000000000000000000000000000000000000001234'
        16      | false | 0xffff    | '0x000000000000000000000000000000000000000000000000000000000000ffff'
        16      | true  | -0x0647   | '0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9b9'
        16      | true  | 0x0647    | '0x0000000000000000000000000000000000000000000000000000000000000647'
        16      | true  | -0x1234   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffedcc'
        16      | true  | 0x4321    | '0x0000000000000000000000000000000000000000000000000000000000004321'
        16      | true  | -0x8000   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8000'
        16      | true  | 0x7fff    | '0x0000000000000000000000000000000000000000000000000000000000007fff'

        40      | false | 0x0000000000  | '0x0000000000000000000000000000000000000000000000000000000000000000'
        40      | false | 0x0000000001  | '0x0000000000000000000000000000000000000000000000000000000000000001'
        40      | false | 0x0000000064  | '0x0000000000000000000000000000000000000000000000000000000000000064'
        40      | true  | -0x0000000000 | '0x0000000000000000000000000000000000000000000000000000000000000000'
        40      | true  | 0x0000000000  | '0x0000000000000000000000000000000000000000000000000000000000000000'
        40      | true  | -0x0000000001 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        40      | true  | 0x0000000001  | '0x0000000000000000000000000000000000000000000000000000000000000001'
        40      | true  | -0x0000000064 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        40      | true  | 0x0000000064  | '0x0000000000000000000000000000000000000000000000000000000000000064'
        40      | false | 0x0000064123  | '0x0000000000000000000000000000000000000000000000000000000000064123'
        40      | false | 0x1122334455  | '0x0000000000000000000000000000000000000000000000000000001122334455'
        40      | false | 0xffffffffff  | '0x000000000000000000000000000000000000000000000000000000ffffffffff'
        40      | true  | -0x0000064123 | '0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9bedd'
        40      | true  | 0x0000064123  | '0x0000000000000000000000000000000000000000000000000000000000064123'
        40      | true  | -0x1122334455 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffeeddccbbab'
        40      | true  | 0x5544332211  | '0x0000000000000000000000000000000000000000000000000000005544332211'
        40      | true  | -0x8000000000 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffff8000000000'
        40      | true  | 0x7fffffffff  | '0x0000000000000000000000000000000000000000000000000000007fffffffff'

        64      | false | 0x0000000000000000    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        64      | false | 0x0000000000000001    | '0x0000000000000000000000000000000000000000000000000000000000000001'
        64      | false | 0x0000000000000064    | '0x0000000000000000000000000000000000000000000000000000000000000064'
        64      | true  | -0x0000000000000000   | '0x0000000000000000000000000000000000000000000000000000000000000000'
        64      | true  | 0x0000000000000000    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        64      | true  | -0x0000000000000001   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        64      | true  | 0x0000000000000001    | '0x0000000000000000000000000000000000000000000000000000000000000001'
        64      | true  | -0x0000000000000064   | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        64      | true  | 0x0000000000000064    | '0x0000000000000000000000000000000000000000000000000000000000000064'
        64      | false | 0x0000000641234567    | '0x0000000000000000000000000000000000000000000000000000000641234567'
        64      | false | 0x1122334455667788    | '0x0000000000000000000000000000000000000000000000001122334455667788'
        64      | false | 0xffffffffffffffff    | '0x000000000000000000000000000000000000000000000000ffffffffffffffff'
        64      | true  | -0x0000000641234567   | '0xfffffffffffffffffffffffffffffffffffffffffffffffffffffff9bedcba99'
        64      | true  | 0x0000000641234567    | '0x0000000000000000000000000000000000000000000000000000000641234567'
        64      | true  | -0x1122334455667788   | '0xffffffffffffffffffffffffffffffffffffffffffffffffeeddccbbaa998878'
        64      | true  | 0x1122334455667788    | '0x0000000000000000000000000000000000000000000000001122334455667788'
        64      | true  | -0x8000000000000000   | '0xffffffffffffffffffffffffffffffffffffffffffffffff8000000000000000'
        64      | true  | 0x7fffffffffffffff    | '0x0000000000000000000000000000000000000000000000007fffffffffffffff'

        120     | false | 0x000000000000000000000000000000  | '0x0000000000000000000000000000000000000000000000000000000000000000'
        120     | false | 0x000000000000000000000000000001  | '0x0000000000000000000000000000000000000000000000000000000000000001'
        120     | false | 0x000000000000000000000000000064  | '0x0000000000000000000000000000000000000000000000000000000000000064'
        120     | true  | -0x000000000000000000000000000000 | '0x0000000000000000000000000000000000000000000000000000000000000000'
        120     | true  | 0x000000000000000000000000000000  | '0x0000000000000000000000000000000000000000000000000000000000000000'
        120     | true  | -0x000000000000000000000000000001 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        120     | true  | 0x000000000000000000000000000001  | '0x0000000000000000000000000000000000000000000000000000000000000001'
        120     | true  | -0x000000000000000000000000000064 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        120     | true  | 0x000000000000000000000000000064  | '0x0000000000000000000000000000000000000000000000000000000000000064'
        120     | false | 0x000000000000000000001280000000  | '0x0000000000000000000000000000000000000000000000000000001280000000'
        120     | false | 0x112233445566778899aabbccddeeff  | '0x0000000000000000000000000000000000112233445566778899aabbccddeeff'
        120     | false | 0xffffffffffffffffffffffffffffff  | '0x0000000000000000000000000000000000ffffffffffffffffffffffffffffff'
        120     | true  | -0x000000000000000000001280000000 | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffed80000000'
        120     | true  | 0x000000000000000000001280000000  | '0x0000000000000000000000000000000000000000000000000000001280000000'
        120     | true  | -0x112233445566778899aabbccddeeff | '0xffffffffffffffffffffffffffffffffffeeddccbbaa99887766554433221101'
        120     | true  | 0x112233445566778899aabbccddeeff  | '0x0000000000000000000000000000000000112233445566778899aabbccddeeff'
        120     | true  | -0x800000000000000000000000000000 | '0xffffffffffffffffffffffffffffffffff800000000000000000000000000000'
        120     | true  | 0x7fffffffffffffffffffffffffffff  | '0x00000000000000000000000000000000007fffffffffffffffffffffffffffff'

        256     | false | 0x000000000000000000000000000000000000000000000000000000000000000     | '0x0000000000000000000000000000000000000000000000000000000000000000'
        256     | false | 0x000000000000000000000000000000000000000000000000000000000000001     | '0x0000000000000000000000000000000000000000000000000000000000000001'
        256     | false | 0x000000000000000000000000000000000000000000000000000000000000064     | '0x0000000000000000000000000000000000000000000000000000000000000064'
        256     | true  | -0x000000000000000000000000000000000000000000000000000000000000000    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        256     | true  | 0x000000000000000000000000000000000000000000000000000000000000000     | '0x0000000000000000000000000000000000000000000000000000000000000000'
        256     | true  | -0x000000000000000000000000000000000000000000000000000000000000001    | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        256     | true  | 0x000000000000000000000000000000000000000000000000000000000000001     | '0x0000000000000000000000000000000000000000000000000000000000000001'
        256     | true  | -0x000000000000000000000000000000000000000000000000000000000000064    | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c'
        256     | true  | 0x0000000000000000000000000000000000000000000000000000000000000064    | '0x0000000000000000000000000000000000000000000000000000000000000064'
        256     | false | 0x0000000000000000000000000000000000000000000000000000000006400000    | '0x0000000000000000000000000000000000000000000000000000000006400000'
        256     | false | 0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122    | '0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'
        256     | false | 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff    | '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        256     | true  | -0x0000000000000000000000000000000000000000000000000000000006400000   | '0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffff9c00000'
        256     | true  | 0x0000000000000000000000000000000000000000000000000000000006400000    | '0x0000000000000000000000000000000000000000000000000000000006400000'
        256     | true  | -0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122   | '0xeeddccbbaa99887766554433221100eeddccbbaa99887766554433221100eede'
        256     | true  | 0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122    | '0x112233445566778899aabbccddeeff112233445566778899aabbccddeeff1122'
        256     | true  | -0x8000000000000000000000000000000000000000000000000000000000000000   | '0x8000000000000000000000000000000000000000000000000000000000000000'
        256     | true  | 0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff    | '0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
    }

    def "should catch out of range before encoding"() {
        def obj = [
                isValueValid: false,
        ] as NumericTypeImpl

        when:
        obj.encodeSimple 0G

        then:
        thrown IllegalArgumentException
    }

    def "should catch out of range after decoding"() {
        def obj = [
                isValueValid: false,
        ] as NumericTypeImpl

        when:
        obj.decodeSimple(Hex32.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        DEFAULT                         | [] as NumericTypeImpl
        DEFAULT                         | [256] as NumericTypeImpl
        [64, true] as NumericTypeImpl   | [64, true] as NumericTypeImpl
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                           | second
        DEFAULT                         | DEFAULT
        DEFAULT                         | [] as NumericTypeImpl
        DEFAULT                         | [256] as NumericTypeImpl
        [64, true] as NumericTypeImpl   | [64, true] as NumericTypeImpl
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first       | second
        DEFAULT     | null
        DEFAULT     | [64] as NumericTypeImpl
        DEFAULT     | UIntType.DEFAULT
    }

    def "should be converted to a string representation"() {
        def obj = [
                getCanonicalName: 'impl'
        ] as NumericTypeImpl

        expect:
        obj as String == 'impl'
    }
}
