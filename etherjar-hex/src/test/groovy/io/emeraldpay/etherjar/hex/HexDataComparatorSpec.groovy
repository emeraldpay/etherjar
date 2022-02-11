/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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

class HexDataComparatorSpec extends Specification {

    def comparator = new HexDataComparator()

    def "cannot compare different size"() {
        when:
        comparator.compare(HexData.from("0x0000"), HexData.from("0x00"))
        then:
        thrown(IllegalArgumentException)
    }

    def "compare similar"() {
        when:
        def act = comparator.compare(HexData.from("0x1234"), HexData.from("0x1234"))
        then:
        act == 0
    }

    def "compare with higher"() {
        when:
        def act = comparator.compare(HexData.from("0x1234"), HexData.from("0x2345"))
        then:
        act < 0
    }

    def "compare with lower"() {
        when:
        def act = comparator.compare(HexData.from("0x1234"), HexData.from("0x0123"))
        then:
        act > 0
    }

    def "compare negative sign bytes"() {
        when:
        def act = comparator.compare(HexData.from("0xf234"), HexData.from("0x0123"))
        then:
        act > 0
    }

}
