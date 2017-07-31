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

class UFixedTypeTest extends Specification {

    def "should parse string representation"() {
        when:
        def opt = UFixedType.from input

        then:
        opt.present
        opt.get().canonicalName == output

        where:
        input               | output
        'ufixed'            | 'ufixed128x128'
        'ufixed8x8'         | 'ufixed8x8'
        'ufixed64x64'       | 'ufixed64x64'
        'ufixed64x8'        | 'ufixed64x8'
        'ufixed40x120'      | 'ufixed40x120'
        'ufixed128x128'     | 'ufixed128x128'
    }

    def "should detect null string representation"() {
        when:
        UFixedType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = UFixedType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = UFixedType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'ufxed'
        _ | 'ufexid8x40'
        _ | 'uint16'
    }

    def "should detect wrong inputs in string representation"() {
        when:
        UFixedType.from input

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'ufixed-1x-1'
        _ | 'ufixed-1x8'
        _ | 'ufixed8x1'
        _ | 'ufixed0x128'
        _ | 'ufixed256x8'
    }

    def "should create a correct default instance"() {
        expect:
        UFixedType.DEFAULT.MBits == 128
        UFixedType.DEFAULT.NBits == 128
        UFixedType.DEFAULT.bits == 256
        !UFixedType.DEFAULT.signed
    }

    def "should create an instance with specified number of bits"() {
        def type = [40, 8] as UFixedType

        expect:
        type.MBits == 40
        type.NBits == 8
        type.bits == 48
        !type.signed
    }

    def "should return a minimal value (inclusive)"() {
        def type = [bits] as UFixedType

        expect:
        type.minValue == 0.0G

        where:
        bits << [8, 40, 64, 128]
    }

    def "should return a maximal value (exclusive)"() {
        def type = [bits] as UFixedType

        expect:
        type.maxValue == val as BigDecimal

        where:
        bits    | val
        8       | 0x100G
        40      | 0x10000000000G
        64      | 0x10000000000000000G
        128     | 0x100000000000000000000000000000000G
    }
}
