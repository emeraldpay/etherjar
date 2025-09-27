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

package io.emeraldpay.etherjar.domain

import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class MethodIdSpec extends Specification {

    def "should be created from ABI"() {
        expect:
        id == MethodId.fromSignature(name, types as String[]).toHex()

        where:
        id           | name     | types
        '0xcdcd77c0' | 'baz'    | ['uint32', 'bool']
        '0xab55044d' | 'bar'    | ['fixed128x128[2]']
        '0x8be65246' | 'f'      | ['uint256', 'uint32[]', 'bytes10', 'bytes']
    }

    def "should catch null contract method name"() {
        when:
        MethodId.fromSignature null

        then:
        thrown NullPointerException
    }

    def "should catch null contract method types"() {
        when:
        MethodId.fromSignature(null, null as Collection<String>)

        then:
        thrown NullPointerException
    }

    def "check from invalid bytes"() {
        when:
        MethodId.from(invalid_bytes as byte[])

        then:
        thrown(IllegalArgumentException)

        where:
        _ | invalid_bytes
        _ | []
        _ | [0x00]
        _ | [0x00, 0x01, 0x00, 0x00, 0xFF]
    }

    def "check from null"() {
        when:
        MethodId.from(null as byte[])

        then:
        thrown(NullPointerException)

        when:
        MethodId.from(null as String)

        then:
        thrown(NullPointerException)
    }

    def "check from invalid string"() {
        when:
        MethodId.from(invalid_string as String)

        then:
        thrown(IllegalArgumentException)

        where:
        _ | invalid_string
        _ | '123'
        _ | '-850932852093457982375'
    }

    def "check from valid bytes"() {
        when:
        MethodId id = MethodId.from(valid_bytes as byte[])

        then:
        id.getBytes() == valid_bytes as byte[]

        where:
        _ | valid_bytes
        _ | [(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00]
        _ | [(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x0F]
        _ | [(byte)0xFF, (byte)0xaa, (byte)0xcc, (byte)0x1c]
        _ | [(byte)0x13, (byte)0x3b, (byte)0xf2, (byte)0x2F]
    }

    def "check from valid string"() {
        when:
        MethodId id = MethodId.from(valid_string as String)

        then:
        id.toString().equalsIgnoreCase(valid_string)

        where:
        _ | valid_string
        _ | '0x11223344'
        _ | '0x00000000'
        _ | '0xabcdefff'
        _ | '0x12345678'
    }

    def "extract from input - none"() {
        when:
        MethodId.fromInput(HexData.from('0x'))
        then:
        thrown(IllegalArgumentException)
    }

    def "extract from input - null"() {
        when:
        MethodId.fromInput(null)
        then:
        thrown(NullPointerException)
    }

    def "extract from input - just method"() {
        when:
        def id = MethodId.fromInput(HexData.from('0xab55044d'))
        then:
        id.toHex() == '0xab55044d'
        when:
        id = MethodId.fromInput(HexData.from('0x00000001'))
        then:
        id.toHex() == '0x00000001'
    }

    def "extract from input - with parameters"() {
        when:
        def id = MethodId.fromInput(HexData.from('0xa9059cbb000000000000000000000000ef2de474d07c9db4eb6b8ffeb64f5cbc725c75180000000000000000000000000000000000000000000000000000000000000cba'))
        then:
        id.toHex() == '0xa9059cbb'
    }
}
