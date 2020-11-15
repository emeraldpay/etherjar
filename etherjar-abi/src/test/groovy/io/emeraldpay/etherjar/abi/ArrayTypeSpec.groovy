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

import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

class ArrayTypeSpec extends Specification {

    @Shared DEFAULT = [UIntType.DEFAULT, 12] as ArrayType<BigInteger>

    def "should parse string representation"() {
        def parser = Mock Function

        when:
        ArrayType.from({ -> [parser] }, input)

        then:
        1 * parser.apply(inter) >> Optional.of(UIntType.DEFAULT)
        0 * parser.apply(_)

        where:
        input           | inter
        'abc[123]'      | 'abc'
        '_[][123]'      | '_[]'
        '_[1][2][3]'    | '_[1][2]'
    }

    def "should detect null string representation"() {
        when:
        ArrayType.from({ -> [] }, null)

        then:
        thrown NullPointerException
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = ArrayType.from({ -> [] }, input)

        then:
        !opt.present

        where:
        _ | input
        _ | ''
        _ | 'int16'
        _ | 'int16['
        _ | 'int16[]'
        _ | '_[1][]'
        _ | '_[]'
        _ | '[[]'
        _ | '[]'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        ArrayType.from({ -> [] }, input)

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | '[123]'
        _ | 'int16]'
        _ | 'int16[0]'
        _ | 'int16[-1]'
        _ | 'int16[abc]'
        _ | 'int16[][0]'
        _ | 'int16[][-3]'
    }

    def "should detect unknown array wrapped type"() {
        when:
        ArrayType.from({ -> [] }, '_[1]')

        then:
        thrown IllegalArgumentException
    }

    def "should detect dynamic array wrapped types"() {
        def parser = { Optional.of({ true } as Type) } as Function

        when:
        ArrayType.from({ -> [parser] }, '_[1]')

        then:
        thrown IllegalArgumentException
    }

    def "should create a correct default instance"() {
        expect:
        DEFAULT.wrappedType == UIntType.DEFAULT
        DEFAULT.length == 12
        DEFAULT.static
    }

    def "should detect a negative array length"() {
        when:
        new ArrayType<>(UIntType.DEFAULT, len)

        then:
        thrown IllegalArgumentException

        where:
        _ | len
        _ | -1
        _ | -12
    }

    def "should return a canonical string representation"() {
        expect:
        type.canonicalName == str

        where:
        type                                                                | str
        DEFAULT                                                             | 'uint256[12]'
        [DEFAULT, 21] as ArrayType<BigInteger>                              | 'uint256[12][21]'
        [[DEFAULT, 1] as ArrayType<BigInteger>, 2] as ArrayType<BigInteger> | 'uint256[12][1][2]'
    }

    def "should encode & decode array values"() {
        def parser = { Optional.of(BoolType.DEFAULT) } as Function

        def obj = ArrayType.from({ -> [parser] }, str).get()

        when:
        def data = obj.encode(arr as Object[])
        def res = obj.decode data

        then:
        data == hex
        Arrays.equals(res, arr as Object[])

        where:
        str     | arr                                               | hex
        '_[1]'  | [BoolType.TRUE]                                   | BoolType.DEFAULT.encode(BoolType.TRUE)
        '_[3]'  | [BoolType.TRUE, BoolType.FALSE, BoolType.FALSE]   | HexData.combine(BoolType.DEFAULT.encode(BoolType.TRUE), BoolType.DEFAULT.encode(BoolType.FALSE), BoolType.DEFAULT.encode(BoolType.FALSE))
    }

    def "should catch wrong array length to encode"() {
        when:
        DEFAULT.encode(new BigInteger[len])

        then:
        thrown IllegalArgumentException

        where:
        _ | len
        _ | 1
        _ | 8
        _ | 21
    }

    def "should catch wrong data to decode"() {
        def parser = { Optional.of(BoolType.DEFAULT) } as Function

        def obj = ArrayType.from({ -> [parser] }, str).get()

        when:
        obj.decode hex

        then:
        thrown IllegalArgumentException

        where:
        str     | hex
        '_[3]'   | Type.encodeLength(3)
        '_[1]'   | Type.encodeLength(1).concat(BoolType.DEFAULT.encode(BoolType.FALSE), BoolType.DEFAULT.encode(BoolType.FALSE))
    }

    def "should catch empty data to decode"() {
        when:
        DEFAULT.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first   | second
        DEFAULT | [UIntType.DEFAULT, 12] as ArrayType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first   | second
        DEFAULT | DEFAULT
        DEFAULT | [UIntType.DEFAULT, 12] as ArrayType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first   | second
        DEFAULT | null
        DEFAULT | [UIntType.DEFAULT, 8] as ArrayType
        DEFAULT | [IntType.DEFAULT, 12] as ArrayType
        DEFAULT | UIntType.DEFAULT
        DEFAULT | 'ABC'
    }

    def "should be converted to a string representation"() {
        expect:
        DEFAULT as String == 'uint256[12]'
    }
}
