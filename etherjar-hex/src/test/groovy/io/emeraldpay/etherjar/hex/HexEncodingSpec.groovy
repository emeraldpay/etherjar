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

package io.emeraldpay.etherjar.hex

import spock.lang.Specification

class HexEncodingSpec extends Specification {

    def "should hex encode & decode integer values"() {
        expect:
        HexEncoding.toHex(val) == hex
        HexEncoding.toNakedHex(val) == naked

        and:
        HexEncoding.toHex(val as BigInteger) == hex
        HexEncoding.toNakedHex(val as BigInteger) == naked

        and:
        HexEncoding.fromHex(hex) == val as BigInteger
        HexEncoding.fromHex(naked) == val as BigInteger

        where:
        val                 | hex                   | naked
        0L                  | '0x00'                | '0'
        4180L               | '0x1054'              | '1054'
        1659284L            | '0x195194'            | '195194'
        81985529216486895L  | '0x0123456789abcdef'  | '123456789abcdef'
    }

    def "should detect wrong hex-encoding strings"() {
        when:
        HexEncoding.fromHex hex

        then:
        thrown NumberFormatException

        where:
        _ | hex
        _ | ''
        _ | '_'
        _ | 'x'
        _ | '0x'
        _ | 'xyz'
    }
}
