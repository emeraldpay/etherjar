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

package io.infinitape.etherjar.domain

import io.infinitape.etherjar.hex.HexData
import spock.lang.Specification
import spock.lang.Unroll

class AddressSpec extends Specification {

    def "should parse address"() {
        expect:
        Address.from(addr).toHex() == addr
        Address.from(addr).bytes == bytes

        where:
        addr                                            | bytes
        '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60'    | [0, 0, 0, 0, 0, 1, 91, 35, -57, -30, 11, 14, -91, -21, -40, 76, 57, -36, -66, 96] as byte[]
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff2'    | [-1, -1, -1, -1, -13, -104, 79, 86, -101, 76, 127, -11, 20, 52, -103, -39, 74, -66, 47,-14] as byte[]
        '0x0000000000000000000000000000000000000000'    | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
        '0xffffffffffffffffffffffffffffffffffffffff'    | [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1] as byte[]
    }

    def "should create from valid HexData"() {
        expect:
        Address.from(HexData.from(hex)).toHex() == hex
        where:
        hex << [
            '0x0000000000015b23c7e20b0ea5ebd84c39dcbe60',
            '0xfffffffff3984f569b4c7ff5143499d94abe2ff2',
            '0x0000000000000000000000000000000000000000',
            '0xffffffffffffffffffffffffffffffffffffffff'
        ]
    }

    def "should fail to create from invalid HexData"() {
        when:
        Address.from(hex)
        then:
        def err = thrown(IllegalArgumentException)

        where:
        hex << [
            '0x00000000015b23c7e20b0ea5ebd84c39dcbe60',
            '0x00000000000015b23c7e20b0ea5ebd84c39dcbe60',
            '0x00000000000015b23c7e20b0ea5ebd84c39dcbe6000',
            '0x00fffffffff3984f569b4c7ff5143499d94abe2ff2',
        ].collect { HexData.from(it) }
    }

    def "should validate address with checksum"() {
        expect:
        Address.isValidAddress '0x52908400098527886E0F7030069857D2E4169EE7'
        Address.isValidAddress '0xde709f2102306220921060314715629080e2fb77'
        Address.isValidAddress '0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed'
        Address.isValidAddress '0x5A4EAB120fB44eb6684E5e32785702FF45ea344D'

        and: "2nd 'A' changed to 'a'"
        !Address.isValidAddress('0x5a4EAB120fB44eb6684E5e32785702FF45ea344D')
    }

    def "should ignore invalid addresses"() {
        when:
        Address.from '0xK2908400098527886E0F7030069857D2E4169EE7'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0'
        then:
        thrown IllegalArgumentException

        when:
        Address.from null as String
        then:
        thrown IllegalArgumentException

        when:
        Address.from(null as byte[])
        then:
        thrown IllegalArgumentException

        when:
        Address.from ''
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0xfake'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0000000000015b23c7e20b0ea5ebd84c39dcbe'
        then:
        thrown IllegalArgumentException

        when:
        Address.from '0x0000000000015b23c7e20b0ea5ebd84c39dcbe6070'
        then:
        thrown(IllegalArgumentException)
    }

    def "toString makes valid checksum"() {
        expect:
        Address.from(source).toString() == checksumed
        println Address.from(source).toString()

        where:
        source                                          | checksumed
        '0x52908400098527886E0F7030069857D2E4169EE7'    | '0x52908400098527886E0F7030069857D2E4169EE7'
        '0x52908400098527886e0f7030069857d2e4169ee7'    | '0x52908400098527886E0F7030069857D2E4169EE7'
        '0xde709f2102306220921060314715629080e2fb77'    | '0xde709f2102306220921060314715629080e2fb77'
        '0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed'    | '0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed'
        '0x5a4eab120fb44eb6684e5e32785702ff45ea344d'    | '0x5A4EAB120fB44eb6684E5e32785702FF45ea344D'
        '0x78fd860124d3b95fee2be3438c102c4fcbd32d88'    | '0x78fD860124D3B95fEe2Be3438C102C4fcBd32D88'
        '0x24d3b95fee2be3438c102c4fcb78fd8601d32d86'    | '0x24d3B95FEE2bE3438c102c4fCB78Fd8601d32d86'
        '0x24d3b95fee2be3438c102c4fcb78fd8601d32d87'    | '0x24d3b95fee2BE3438c102c4FcB78FD8601D32d87'
        '0x24d3b95fee2be3438c102c4fcb78fd8601d32d88'    | '0x24D3B95fEe2Be3438C102c4fcB78Fd8601D32d88'
        '0x24d3b95fee2be3438c102c4fcb78fd8601d32d89'    | '0x24d3B95fee2BE3438c102c4FcB78FD8601d32D89'
    }
}
