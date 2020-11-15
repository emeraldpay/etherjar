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

class IntTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = IntType.from input

        then:
        opt.present
        opt.get() in IntType
        opt.get().canonicalName == output

        where:
        input       | output
        'int'       | 'int256'
        'int8'      | 'int8'
        'int40'     | 'int40'
        'int64'     | 'int64'
        'int128'    | 'int128'
        'int256'    | 'int256'
    }

    def "should detect null string representation"() {
        when:
        IntType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = IntType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = IntType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint40'
        _ | 'xint140'
        _ | 'bool'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        IntType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'intx'
        _ | 'int0'
        _ | 'int257'
        _ | 'int1024'
        _ | 'int140x'
    }

    def "should create a correct default instance"() {
        expect:
        IntType.DEFAULT.bits == 256
        IntType.DEFAULT.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40] as IntType

        expect:
        type.bits == 40
        type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as IntType

        expect:
        type.minValue == val

        where:
        bits    | val
        8       | -0x80G
        40      | -0x8000000000G
        64      | -0x8000000000000000G
        128     | -0x80000000000000000000000000000000G
        256     | -0x8000000000000000000000000000000000000000000000000000000000000000G
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as IntType

        expect:
        type.maxValue == val

        where:
        bits    | val
        8       | 0x80G
        40      | 0x8000000000G
        64      | 0x8000000000000000G
        128     | 0x80000000000000000000000000000000G
        256     | 0x8000000000000000000000000000000000000000000000000000000000000000G
    }

    def "should return a canonical string representation"() {
        def type  = [size] as IntType

        expect:
        type.canonicalName == str

        where:
        size    | str
        8       | 'int8'
        40      | 'int40'
        64      | 'int64'
        128     | 'int128'
        256     | 'int256'
    }
}
