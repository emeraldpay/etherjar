/*
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

package io.infinitape.etherjar.contract.type

import io.infinitape.etherjar.core.HexData
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

class DynamicArrayTypeSpec extends Specification {

    @Shared DEFAULT = [UIntType.DEFAULT] as DynamicArrayType<BigInteger>

    def "should parse string representation"() {
        def parser = Mock Function

        when:
        DynamicArrayType.from({ -> [parser] }, input)

        then:
        1 * parser.apply(inter) >> Optional.of(UIntType.DEFAULT)
        0 * parser.apply(_)

        where:
        input           | inter
        '_[]'           | '_'
        '_[123][]'      | '_[123]'
    }

    def "should detect null string representation"() {
        when:
        DynamicArrayType.from({ -> [] }, null)

        then:
        thrown NullPointerException
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = DynamicArrayType.from({ -> [] }, input)

        then:
        !opt.present

        where:
        _ | input
        _ | ''
        _ | 'int16'
        _ | 'int16['
        _ | 'int16[1]'
        _ | '_[1][2][3]'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        DynamicArrayType.from({ -> [] }, '[]')

        then:
        thrown IllegalArgumentException
    }

    def "should detect unknown array wrapped type"() {
        when:
        DynamicArrayType.from({ -> [] }, '_[]')

        then:
        thrown IllegalArgumentException
    }

    def "should detect dynamic array wrapped types"() {
        def parser = { Optional.of({ true } as Type) } as Function

        when:
        DynamicArrayType.from({ -> [parser] }, '_[]')

        then:
        thrown IllegalArgumentException
    }

    def "should create a correct default instance"() {
        expect:
        DEFAULT.wrappedType == UIntType.DEFAULT
        DEFAULT.dynamic
    }

    def "should return a canonical string representation"() {
        expect:
        type.canonicalName == str

        where:
        type                                                                                | str
        DEFAULT                                                                             | 'uint256[]'
        [BoolType.DEFAULT] as DynamicArrayType<BigInteger>                                  | 'bool[]'
        [[UIntType.DEFAULT, 12] as ArrayType<BigInteger>] as DynamicArrayType<BigInteger>   | 'uint256[12][]'
    }

    def "should encode & decode array values"() {
        def parser = { Optional.of(BoolType.DEFAULT) } as Function

        def obj = DynamicArrayType.from({ -> [parser] }, str).get()

        when:
        def data = obj.encode(arr as Object[])
        def res = obj.decode data

        then:
        data == hex
        Arrays.equals(res, arr as Object[])

        where:
        str     | arr                               | hex
        '_[]'   | []                                | Type.encodeLength(0)
        '_[]'   | [BoolType.FALSE, BoolType.TRUE]   | Type.encodeLength(2).concat(BoolType.DEFAULT.encode(BoolType.FALSE), BoolType.DEFAULT.encode(BoolType.TRUE))
    }

    def "should catch wrong data to decode"() {
        def parser = { Optional.of(BoolType.DEFAULT) } as Function

        def obj = DynamicArrayType.from({ -> [parser] }, str).get()

        when:
        obj.decode hex

        then:
        thrown IllegalArgumentException

        where:
        str     | hex
        '_[]'   | Type.encodeLength(0).concat(BoolType.DEFAULT.encode(BoolType.FALSE))
        '_[]'   | Type.encodeLength(2).concat(BoolType.DEFAULT.encode(BoolType.TRUE))
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
        DEFAULT | [UIntType.DEFAULT] as DynamicArrayType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first   | second
        DEFAULT | DEFAULT
        DEFAULT | [UIntType.DEFAULT] as DynamicArrayType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first   | second
        DEFAULT | null
        DEFAULT | [IntType.DEFAULT] as DynamicArrayType
        DEFAULT | UIntType.DEFAULT
        DEFAULT | 'ABC'
    }

    def "should be converted to a string representation"() {
        expect:
        DEFAULT as String == 'uint256[]'
    }
}
