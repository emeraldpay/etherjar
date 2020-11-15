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

class BoolTypeSpec extends Specification {

    def "should parse string representation"() {
        when:
        def opt = BoolType.from 'bool'

        then:
        opt.present
        opt.get() in BoolType
    }

    def "should detect null string representation"() {
        when:
        BoolType.from null

        then:
        thrown NullPointerException
    }

    def "should ignore empty string representation"() {
        when:
        def opt = BoolType.from ''

        then:
        !opt.present
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = BoolType.from input

        then:
        !opt.present

        where:
        _ | input
        _ | 'uint40'
        _ | 'int256'
    }

    def "should create a correct default instance"() {
        expect:
        BoolType.DEFAULT.bits == 8
        !BoolType.DEFAULT.signed
    }

    def "should return a minimal value (inclusive)"() {
        expect:
        BoolType.DEFAULT.minValue == 0G
    }

    def "should return a maximal value (exclusive)"() {
        expect:
        BoolType.DEFAULT.maxValue == 2G
    }

    def "should return a canonical string representation"() {
        expect:
        BoolType.DEFAULT.canonicalName == 'bool'
    }

    def "should be converted to a string representation"() {
        expect:
        BoolType.DEFAULT as String == 'bool'
    }
}
