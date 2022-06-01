/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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

class HexQuantitySpec extends Specification {

    def "Can parse string"() {
        expect:
        HexQuantity.from(hex).value == val
        where:
        hex       | val
        '0x100'   | 256
        '-0x100'  | -256
        '0x0'     | 0
    }

    def "Create from long"() {
        expect:
        HexQuantity.from(Long.valueOf(hex)).value == val
        where:
        hex   | val
        256   | 256
        -256  | -256
        0     | 0
    }

    def "Create from BigInteger"() {
        expect:
        HexQuantity.from(Long.valueOf(hex).toBigInteger()).value == val
        where:
        hex   | val
        256   | 256
        -256  | -256
        0     | 0
    }

    def "Format to hex"() {
        expect:
        HexQuantity.from(val).toHex() == hex
        where:
        hex      | val
        '0x100'  | 256
        '-0x100' | -256
        '0x0'    | 0
    }

    def "toString returns hex"() {
        expect:
        HexQuantity.from(val).toString() == hex
        where:
        hex       | val
        '0x100'   | 256
        '-0x100'  | -256
        '0x0'     | 0
    }

    def "Parses as null for 0x"() {
        when:
        def act = HexQuantity.from("0x")
        then:
        act == null
    }

    def "Unable to parse empty string"() {
        when:
        HexQuantity.from("")
        then:
        thrown(IllegalArgumentException)
    }

    def "Unable to parse non hex string"() {
        when:
        HexQuantity.from("100")
        then:
        thrown(IllegalArgumentException)
    }

    def "Unable to parse invalid string"() {
        when:
        HexQuantity.from("0xfoobar")
        then:
        thrown(IllegalArgumentException)
    }

    def "Return null for null input"() {
        when:
        def act = HexQuantity.from((Long)null)
        then:
        act == null

        when:
        act = HexQuantity.from((BigInteger)null)
        then:
        act == null

        when:
        act = HexQuantity.from((String)null)
        then:
        act == null
    }

    def "Unable to create from null"() {
        when:
        new HexQuantity(null)
        then:
        thrown(IllegalArgumentException)
    }


    def "Doesn't produce leading zeroes"() {
        when:
        def act = HexData.from("0x0110").asQuantity().toHex()
        then:
        act == "0x110"
    }

    def "Equal"() {
        def x = HexQuantity.from '0x0123456789abcdef'
        def y = HexQuantity.from '0x00'

        expect:
        x == HexQuantity.from('0x0123456789abcdef')
        x != HexQuantity.from('0x0123456789abcdee')

        and:
        y == HexQuantity.from('0x00')
        y != HexQuantity.from('0x01')
    }

    def "Equal is reflexive"() {
        def x = HexQuantity.from '0x0123456789abcdef'

        expect:
        x == x
    }

    def "Equal is symmetric"() {
        def x = HexQuantity.from '0x604f7bef'
        def y = HexQuantity.from '0x604f7bef'

        expect:
        x == y
        y == x
    }

    def "Equal is transitive"() {
        def x = HexQuantity.from '0x604f7bef716'
        def y = HexQuantity.from '0x604f7bef716'
        def z = HexQuantity.from '0x604f7bef716'

        expect:
        x == y
        y == x
        x == z
    }

    def "Converts to data"() {
        when:
        def act = HexQuantity.from("0x110").asData().toHex()
        then:
        act == "0x0110"
    }

    def "Converts to data max amount"() {
        when:
        def act = HexQuantity.from("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff").asData().toHex()
        then:
        act == "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    }

    def "hash code is consistent"() {
        def x = HexQuantity.from(256)
        def y = HexQuantity.from("0x100")
        def z = HexQuantity.from(BigInteger.valueOf(256))

        expect:
        x.hashCode() == y.hashCode()
        y.hashCode() == z.hashCode()
    }
}
