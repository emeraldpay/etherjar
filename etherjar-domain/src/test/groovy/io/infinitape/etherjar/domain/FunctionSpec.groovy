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

import spock.lang.Specification

class FunctionSpec extends Specification {

    def "Parse function"() {
        expect:
        Function.from(addr).toString() == addr
        Function.from(addr).bytes == bytes
        where:
        addr                                                    | bytes
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff201020304'      | [-1, -1, -1, -1, -13, -104, 79, 86, -101, 76, 127, -11, 20, 52, -103, -39, 74, -66, 47,-14, 1, 2, 3, 4] as byte[]
        '0x000000000000000000000000000000000000000000000000'    | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
        '0xffffffffffffffffffffffffffffffffffffffffffffffff'    | [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1] as byte[]
    }

    def "Ignore Invalid function"() {
        when:
        Function.from('0x0')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from(null as String)
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from(null as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0xfake')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe12345678')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe607012345678')
        then:
        thrown(IllegalArgumentException)
    }
}
