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

import spock.lang.Specification

class BlockHashSpec extends Specification {

    def "Valid hashes"() {
        expect:
        BlockHash.from(hash).toHex() == hash
        where:
        hash << [
                '0x000019c05a62ab070fa4e3a1f6007d6ecf8d3f3d150b469eac04fa168c3a42ed',
                '0x0001067fee6c4e3870bccc1bd2d540b56ba83ba9d7c7dd1e795cb44bda8ff188',
                '0xfff69a04364c7d6e10bbad5274f16b7bd5b3dda95d87283ac90bdb78ab4f8d6a',
                '0x88decd925ad49ac90adb55bf2f4851fe7afc31b71b230a8c7c31e96b4c0a3279',
                '0x28fe7d915c8ef556c9ca80015894aa19545d66c99d1793d84a6de49f7cbdbc8a'
        ]
    }

    def "Sort"() {
        setup:
        def hashes = [
            '0x000019c05a62ab070fa4e3a1f6007d6ecf8d3f3d150b469eac04fa168c3a42ed',
            '0x0001067fee6c4e3870bccc1bd2d540b56ba83ba9d7c7dd1e795cb44bda8ff188',
            '0xfff69a04364c7d6e10bbad5274f16b7bd5b3dda95d87283ac90bdb78ab4f8d6a',
            '0x88decd925ad49ac90adb55bf2f4851fe7afc31b71b230a8c7c31e96b4c0a3279',
            '0x28fe7d915c8ef556c9ca80015894aa19545d66c99d1793d84a6de49f7cbdbc8a',
            '0xd8b26c503c80bdd012e5088e6d2f247d7514c732ba75b0574c73328393680f8b',
            '0x7d3425c1196a093c7d87d8274d4777c13c8955e79aa6dc24545dda8e9e7090fd',
            '0xf3c21a15c5d85df6cd2b134e335c8655e34f9ab6802c2e0fd4e4ca8c1552166a'
        ].collect { BlockHash.from(it) }
        when:
        Collections.sort(hashes)

        then:
        hashes.collect { it.toHex() } == [
            '0x000019c05a62ab070fa4e3a1f6007d6ecf8d3f3d150b469eac04fa168c3a42ed',
            '0x0001067fee6c4e3870bccc1bd2d540b56ba83ba9d7c7dd1e795cb44bda8ff188',
            '0x28fe7d915c8ef556c9ca80015894aa19545d66c99d1793d84a6de49f7cbdbc8a',
            '0x7d3425c1196a093c7d87d8274d4777c13c8955e79aa6dc24545dda8e9e7090fd',
            '0x88decd925ad49ac90adb55bf2f4851fe7afc31b71b230a8c7c31e96b4c0a3279',
            '0xd8b26c503c80bdd012e5088e6d2f247d7514c732ba75b0574c73328393680f8b',
            '0xf3c21a15c5d85df6cd2b134e335c8655e34f9ab6802c2e0fd4e4ca8c1552166a',
            '0xfff69a04364c7d6e10bbad5274f16b7bd5b3dda95d87283ac90bdb78ab4f8d6a',
        ]
    }
}
