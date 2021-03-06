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

import spock.lang.Specification

class BytesTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = BytesType.from input

        then:
        opt.present
        opt.get() in BytesType
        opt.get().canonicalName == output

        where:
        input       | output
        'byte'      | 'bytes1'
        'bytes1'    | 'bytes1'
        'bytes7'    | 'bytes7'
        'bytes8'    | 'bytes8'
        'bytes32'   | 'bytes32'
    }

    def "should detect null string representation"() {
        when:
        BytesType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = BytesType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = BytesType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'bytes'
        _ | 'uint40'
        _ | 'int256'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        BytesType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'bytes_'
        _ | 'bytes0'
        _ | 'bytes37'
        _ | 'bytes128'
        _ | 'bytes3x'
    }

    def "should return a canonical string representation"() {
        expect:
        BytesType.DEFAULT.length == 32
        BytesType.DEFAULT.canonicalName == 'bytes32'
        BytesType.DEFAULT_ONE_BYTE.canonicalName == 'bytes1'
    }

    def "should encode & decode bytes"() {
        def obj = [bytes.size()] as BytesType
        def arr = bytes as byte[]

        when:
        def data = obj.encodeSimple arr
        def res = obj.decodeSimple data

        then:
        data.toHex() == hex
        Arrays.equals res, arr

        where:
        bytes                       | hex
        [0x37]                      | '0x3700000000000000000000000000000000000000000000000000000000000000'
        [0x64, 0x61, 0x76, 0x65]    | '0x6461766500000000000000000000000000000000000000000000000000000000'
        [0x01] * 24                 | '0x0101010101010101010101010101010101010101010101010000000000000000'
        [0x12] * 32                 | '0x1212121212121212121212121212121212121212121212121212121212121212'
    }

    def "should catch wrong bytes length to encode"() {
        when:
        BytesType.DEFAULT.encode(new byte[len])

        then:
        thrown IllegalArgumentException

        where:
        _ | len
        _ | 1
        _ | 8
        _ | 21
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first               | second
        BytesType.DEFAULT   | [] as BytesType
        BytesType.DEFAULT   | [32] as BytesType
        [24] as BytesType   | [24] as BytesType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first               | second
        BytesType.DEFAULT   | BytesType.DEFAULT
        BytesType.DEFAULT   | [] as BytesType
        BytesType.DEFAULT   | [32] as BytesType
        [24] as BytesType   | [24] as BytesType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first               | second
        BytesType.DEFAULT   | null
        BytesType.DEFAULT   | UIntType.DEFAULT
        BytesType.DEFAULT   | DynamicBytesType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        BytesType.DEFAULT as String == 'bytes32'
    }
}
