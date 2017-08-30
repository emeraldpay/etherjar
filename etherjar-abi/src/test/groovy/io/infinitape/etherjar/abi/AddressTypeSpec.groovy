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

package io.infinitape.etherjar.abi

import io.infinitape.etherjar.domain.Address
import spock.lang.Specification

class AddressTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = AddressType.from 'address'

        then:
        opt.present
        opt.get() in AddressType
    }

    def "should detect null string representation"() {
        when:
        AddressType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = AddressType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = AddressType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint140'
        _ | 'byte'
    }

    def "should return a canonical string representation"() {
        expect:
        AddressType.DEFAULT.canonicalName == 'address'
    }

    def "should encode & decode bytes"() {
        def obj = Address.from str

        when:
        def data = AddressType.DEFAULT.encodeSimple obj
        def res = AddressType.DEFAULT.decodeSimple data

        then:
        data.toHex() == hex
        res == obj

        where:
        str                                             | hex
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'    | '0x0000000000000000000000000000000000015b23c7e20b0ea5ebd84c39dcbe60'
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'    | '0x000000000000000000000000fffffffff3984f569b4c7ff5143499d94abe2ff2'
        '0x0000000000000000000000000000000000000000'    | '0x0000000000000000000000000000000000000000000000000000000000000000'
        '0xffffffffffffffffffffffffffffffffffffffff'    | '0x000000000000000000000000ffffffffffffffffffffffffffffffffffffffff'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first               | second
        AddressType.DEFAULT | [] as AddressType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first               | second
        AddressType.DEFAULT | AddressType.DEFAULT
        AddressType.DEFAULT | [] as AddressType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first               | second
        AddressType.DEFAULT | null
        AddressType.DEFAULT | UIntType.DEFAULT
        AddressType.DEFAULT | DynamicBytesType.DEFAULT
    }

    def "should be converted to a string representation"() {
        expect:
        AddressType.DEFAULT as String == 'address'
    }
}
