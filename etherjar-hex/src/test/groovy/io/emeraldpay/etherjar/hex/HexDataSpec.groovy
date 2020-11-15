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

class HexDataSpec extends Specification {

    def "should combine hex data"() {
        def x = [
            HexData.from('0x0123'),
            HexData.from('0x456789ab'),
            HexData.from('0xcdef')
        ]

        when:
        def y = HexData.combine(x as HexData[])

        then:
        y == HexData.from('0x0123456789abcdef')
    }

    def "should throw on combine null value"() {
        when:
        HexData.combine(null as HexData[])

        then:
        thrown NullPointerException

        when:
        HexData.combine(null as List<HexData>)

        then:
        thrown NullPointerException
    }

    def "should parse hex"() {
        def x = HexData.from hex

        expect:
        x.bytes == bytes

        where:
        hex             | bytes
        '0x0'           | [0] as byte[]
        '0x1'           | [1] as byte[]
        '0x00'          | [0] as byte[]
        '0x01'          | [1] as byte[]
        '0x0f'          | [15] as byte[]
        '0x23'          | [35] as byte[]
        '0xff'          | [-1] as byte[]
        '0x123'         | [1, 35] as byte[]
        '0x0001'        | [0, 1] as byte[]
        '0xABcD'        | [-85, -51] as byte[]
        '0xff01'        | [-1, 1] as byte[]
        '0x000000'      | [0, 0, 0] as byte[]
        '0x000001'      | [0, 0, 1] as byte[]
        '0xffffff'      | [-1, -1, -1] as byte[]
        '0x00000000'    | [0, 0, 0, 0] as byte[]
    }

    def "should throw on null value"() {
        when:
        new HexData(null as byte[])

        then:
        thrown NullPointerException

        when:
        HexData.from(null as String)

        then:
        thrown NullPointerException
    }

    def "should throw on invalid value"() {
        when:
        HexData.from str

        then:
        thrown IllegalArgumentException

        where:
        _ | str
        _ | ''
        _ | '0xfake'
    }

    def "should concat with another hex data"() {
        def x = HexData.from '0x0123456789abcdef'

        def c = [
            HexData.from('0x0123'),
            HexData.from('0x456789ab'),
            HexData.from('0xcdef')
        ]

        when:
        def y = x.concat(c as HexData[])

        then:
        y == HexData.from('0x0123456789abcdef0123456789abcdef')
    }

    def "should detect a case with null concat"() {
        when:
        HexData.EMPTY.concat(null as HexData[])

        then:
        thrown NullPointerException
    }

    def "should extract empty data"() {
        expect:
        HexData.EMPTY.extract(0).is(HexData.EMPTY)
    }

    def "should extract hex data"() {
        when:
        def x = hex.extract size, offset

        then:
        x == res

        where:
        hex                                | size | offset | res
        HexData.from('0x1234')             | 1    | 0      | HexData.from('0x12')
        HexData.from('0x1234')             | 1    | 1      | HexData.from('0x34')
        HexData.from('0x1234')             | 2    | 0      | HexData.from('0x1234')
        HexData.from('0x0123456789abcdef') | 8    | 0      | HexData.from('0x0123456789abcdef')
        HexData.from('0x0123456789abcdef') | 4    | 4      | HexData.from('0x89abcdef')
        HexData.from('0x0123456789abcdef') | 2    | 3      | HexData.from('0x6789')
        HexData.from('0x0123456789abcdef') | 1    | 2      | HexData.from('0x45')
    }

    def "should extract custom instances"() {
        def x = HexData.from '0x0123456789abcdef'

        when:
        def y = x.extract size, offset, conv

        then:
        y == res

        where:
        size    | offset    | conv              | res
        1       | 0         | { it.toHex() }    | '0x01'
        2       | 3         | { it.getSize() }  | 2
        4       | 2         | { it.bytes }      | [0x45, 0x67, 0x89, 0xab] as byte[]
    }

    def "should catch wrong extracted arguments"() {
        when:
        offset == 0 && hex.extract(size)
        offset != 0 && hex.extract(size, offset)

        then:
        thrown IllegalArgumentException

        where:
        hex                    | size | offset
        HexData.EMPTY          | -1   | 0
        HexData.EMPTY          | 0    | -1
        HexData.from('0x1234') | 3    | 0
        HexData.from('0x1234') | 1    | 2
    }

    def "should catch null extracted converter"() {
        when:
        HexData.from(1).extract(1, null)

        then:
        thrown NullPointerException
    }

    def "should split empty data"() {
        expect:
        !HexData.EMPTY.split(8)
    }

    def "should split hex data"() {
        when:
        def x = hex.split size, offset

        then:
        Arrays.equals(x, res as HexData[])

        where:
        hex                                | size | offset | res
        HexData.from('0x1234')             | 1    | 0      | [HexData.from('0x12'), HexData.from('0x34')]
        HexData.from('0x1234')             | 1    | 1      | [HexData.from('0x34')]
        HexData.from('0x1234')             | 2    | 0      | [HexData.from('0x1234')]
        HexData.from('0x0123456789abcdef') | 1    | 4      | [HexData.from('0x89'), HexData.from('0xab'), HexData.from('0xcd'), HexData.from('0xef')]
        HexData.from('0x0123456789abcdef') | 2    | 4      | [HexData.from('0x89ab'), HexData.from('0xcdef')]
        HexData.from('0x0123456789abcdef') | 3    | 2      | [HexData.from('0x456789'), HexData.from('0xabcdef')]
        HexData.from('0x0123456789abcdef') | 8    | 0      | [HexData.from('0x0123456789abcdef')]
        HexData.from('0x0123456789abcdef') | 8    | 8      | []
    }

    def "should split custom instances"() {
        def x = HexData.from '0x0123456789abcdef'

        when:
        def y = x.split size, offset, gen, conv

        then:
        Arrays.deepEquals(y, res.asType(gen(0).getClass()))

        where:
        size    | offset    | gen                   | conv              | res
        1       | 4         | { new String[it] }    | { it.toHex() }    | ['0x89', '0xab', '0xcd', '0xef']
        4       | 4         | { new byte[it][] }    | { it.bytes }      | [[0x89, 0xab, 0xcd, 0xef]] as byte[][]
        4       | 8         | { new byte[it][] }    | { it.bytes }      | [] as byte[][]
    }

    def "should catch wrong split arguments"() {
        when:
        offset == 0 && hex.split(size)
        offset != 0 && hex.split(size, offset)

        then:
        thrown IllegalArgumentException

        where:
        hex                                | size | offset
        HexData.EMPTY                      | -1   | 0
        HexData.EMPTY                      | 0    | -1
        HexData.from('0x1234')             | 2    | 1
        HexData.from('0x0123456789abcdef') | 3    | 0
        HexData.from('0x0123456789abcdef') | 4    | 2
        HexData.from('0x0123456789abcdef') | 2    | 3
    }

    def "should catch null split array generator"() {
        when:
        HexData.from(1).split(1, null, { it.toHex() })

        then:
        thrown NullPointerException
    }

    def "should catch null split type converter"() {
        when:
        HexData.from(1).split(1, { new int[it] }, null)

        then:
        thrown NullPointerException
    }

    def "should format to hex"() {
        def x = new HexData(bytes)

        expect:
        x.toHex() == str

        where:
        bytes                   | str
        [0] as byte[]           | '0x00'
        [1] as byte[]           | '0x01'
        [15] as byte[]          | '0x0f'
        [35] as byte[]          | '0x23'
        [-1] as byte[]          | '0xff'
        [1, 35] as byte[]       | '0x0123'
        [0, 1] as byte[]        | '0x0001'
        [-85, -51] as byte[]    | '0xabcd'
        [-1, 1] as byte[]       | '0xff01'
        [0, 0, 0] as byte[]     | '0x000000'
        [0, 0, 1] as byte[]     | '0x000001'
        [-1, -1, -1] as byte[]  | '0xffffff'
        [0, 0, 0, 0] as byte[]  | '0x00000000'
    }

    def "Equal"() {
        def x = HexData.from '0x0123456789abcdef'
        def y = HexData.from '0x00'

        expect:
        x == HexData.from('0x0123456789abcdef')
        x != HexData.from('0x0123456789abcdee')

        and:
        y == HexData.from('0x00')
        y != HexData.from('0x01')
    }

    def "Equal is reflexive"() {
        def x = HexData.from '0x0123456789abcdef'

        expect:
        x == x
    }

    def "Equal is symmetric"() {
        def x = HexData.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        def y = Hex32.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'

        expect:
        x == y
        y == x
    }

    def "Equal is transitive"() {
        def x = HexData.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        def y = Hex32.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        def z = Hex32.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'

        expect:
        x == y
        y == x
        x == z
    }

    def "Converts to quantity"() {
        def x = HexData.from '0x1234'

        expect:
        x.asQuantity() != null
        x.asQuantity().value == 4660
    }

    def "hash code is consistent"() {
        def x = HexData.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        def y = Hex32.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        def z = Hex32.from '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'

        expect:
        x.hashCode() == y.hashCode()
        y.hashCode() == z.hashCode()
    }
}
