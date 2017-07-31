/*
 * Copyright (c) 2011-2017 Infinitape Inc, All Rights Reserved.
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

package io.infinitape.etherjar.hex

import spock.lang.Specification

class HexEncodingSpec extends Specification {

    def "should hex encode & decode integer values"() {
        expect:
        HexEncoding.toNakedHex(val) == naked
        HexEncoding.toFullHex(val) == full

        and:
        HexEncoding.fromHex(naked) == val
        HexEncoding.fromHex(full) == val

        where:
        val                 | naked             | full
        0G                  | '0'               | '0x00'
        4180G               | '1054'            | '0x1054'
        1659284G            | '195194'          | '0x195194'
        81985529216486895G  | '123456789abcdef' | '0x0123456789abcdef'
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
