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

import spock.lang.Specification

class UIntTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = UIntType.from input

        then:
        opt.present
        opt.get() in UIntType
        opt.get().canonicalName == output

        where:
        input       | output
        'uint'      | 'uint256'
        'uint8'     | 'uint8'
        'uint40'    | 'uint40'
        'uint64'    | 'uint64'
        'uint128'   | 'uint128'
        'uint256'   | 'uint256'
    }

    def "should detect null string representation"() {
        when:
        UIntType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = UIntType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = UIntType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'int16'
        _ | 'xuint16'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        UIntType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'uinty'
        _ | 'uint0'
        _ | 'uint257'
        _ | 'uint1024'
        _ | 'uint16x'
    }

    def "should create a correct default instance"() {
        expect:
        UIntType.DEFAULT.bits == 256
        !UIntType.DEFAULT.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40] as UIntType

        expect:
        type.bits == 40
        !type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as UIntType

        expect:
        type.minValue == 0G

        where:
        bits << [8, 40, 64, 128, 256]
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as UIntType

        expect:
        type.maxValue == val

        where:
        bits    | val
        8       | 0x100G
        40      | 0x10000000000G
        64      | 0x10000000000000000G
        128     | 0x100000000000000000000000000000000G
        256     | 0x10000000000000000000000000000000000000000000000000000000000000000G
    }

    def "should return a canonical string representation"() {
        def type  = [size] as UIntType

        expect:
        type.canonicalName == str

        where:
        size    | str
        8       | 'uint8'
        40      | 'uint40'
        64      | 'uint64'
        128     | 'uint128'
        256     | 'uint256'
    }
}
