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
}
