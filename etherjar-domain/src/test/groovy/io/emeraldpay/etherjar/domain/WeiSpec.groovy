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

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

class WeiSpec extends Specification {

    def "should keep custom unit numbers"() {
        expect:
        Wei.ofUnits(val, unit).toUnits(unit) == val

        where:
        val                     | unit
        0.0                     | Wei.Unit.WEI
        1.234567890             | Wei.Unit.GWEI
        1234567890.0987654321   | Wei.Unit.METHER
    }

    def "should process small number of wei"() {
        when:
        def wei = new Wei(0x0b3266)

        then:
        wei.getAmount() == 733798L
        wei.toString() == '733798 wei'

        and:
        wei.toEthers(6) == 0
        wei.toEthers() == 7.33798E-13
    }

    def "should process large number of wei"() {
        when:
        def wei = new Wei(0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff)

        then:
        wei.getAmount() == 115792089237316195423570985008687907853269984665640564039457584007913129639935
        wei.toString() == '115792089237316195423570985008687907853269984665640564039457584007913129639935 wei'

        and:
        wei.toEthers(5) == 115792089237316195423570985008687907853269984665640564039457.58401
        wei.toEthers() == 115792089237316195423570985008687907853269984665640564039457.584007913129639935
    }

    def "should convert wei to Ether"() {
        expect:
        new Wei(num).toEthers() == ether

        where:
        num                     | ether
        0                       | 0.0
        123456000000000000      | 0.123456
        12345678901234567890    | 12.34567890123456789
        500000000000000000000   | 500
        14999999126000000000123 | 14999.999126000000000123
    }

    def "should meet equals and hashCode contract"() {
        expect:
        EqualsVerifier.forClass(Wei.class).verify()
    }
}
