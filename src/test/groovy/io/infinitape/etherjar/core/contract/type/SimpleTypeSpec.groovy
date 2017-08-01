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

package io.infinitape.etherjar.core.contract.type

import io.infinitape.etherjar.core.Hex32
import io.infinitape.etherjar.core.HexData
import spock.lang.Specification

class SimpleTypeSpec extends Specification {

    static class SimpleTypeImpl<T> implements SimpleType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        Hex32 encodeSimple(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decodeSimple(Hex32 hex32) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as SimpleTypeImpl

    def "should create a correct default instance"() {
        expect:
        DEFAULT.static
        DEFAULT.fixedSize == Hex32.SIZE_BYTES
    }

    def "should encode an object into hex data"() {
        def hex = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000123'

        def t = { hex } as SimpleType

        when:
        def x = t.encode 123

        then:
        x == hex
    }

    def "should decode hex data into an object"() {
        def hex = Hex32.from '0x0000000000000000000000000000000880000000000000000000000000000123'

        def t = { 123 } as SimpleType

        when:
        def obj = t.decode hex

        then:
        obj == 123
    }

    def "should catch empty or too long data to decode"() {
        when:
        DEFAULT.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | data
        _ | HexData.EMPTY
        _ | HexData.from('0x' + '12' * 48)
        _ | HexData.from('0x' + '00' * 64)
    }
}
