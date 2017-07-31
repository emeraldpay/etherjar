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

package io.infinitape.etherjar.core

import spock.lang.Specification

class WeiSpec extends Specification {

    def "Convert wei to Ether"() {
        expect:
        Wei.from(hex).toEther() == ether
        where:
        hex                     | ether
        '0x0'                   |  0.0
        '0x1692343a32d9000'     |  0.101651
        '0x11527914c23af80'     |  0.078012
        '0x3f794375d8dc4c00'    |  4.573761
        '0xa9964ef1b825f600'    | 12.220041
        '0x1b1ae4d6e2ef500000'  | 500
        '0x32d26ce13c9584e4800' | 14999.999126
        '0x54b40aedd840a8e4800' | 24999.999126
    }

    def "Process large number of wei"() {
        setup:
        String hex = '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        when:
        def wei = Wei.from(hex)
        then:
        wei.value.toString() == '115792089237316195423570985008687907853269984665640564039457584007913129639935'
        wei.value.toString(16) == 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff'
        wei.toEther() == new BigDecimal('115792089237316195423570985008687907853269984665640564039457.584008')
        wei.getBytes().length == 33
        wei.getBytes().toList().tail().every { b -> b == (byte)-1 } //tail because first element is 0
        wei.toString() == '115792089237316195423570985008687907853269984665640564039457.5840 ether'
    }

    def "Process small number of wei"() {
        when:
        def wei = Wei.from('0x0b3266')
        then:
        wei.getValue().toLong() == 733798L
        wei.toEther() == 0.0
        wei.toString() == '0.0000 ether'
    }
}
