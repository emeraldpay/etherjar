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

package io.infinitape.etherjar.contract.type

import io.infinitape.etherjar.core.HexData
import spock.lang.Specification

class StaticTypeSpec extends Specification {

    static class StaticTypeImpl<T> implements StaticType<T> {

        @Override
        String getCanonicalName() {
            throw new UnsupportedOperationException()
        }

        @Override
        int getFixedSize() {
            throw new UnsupportedOperationException()
        }

        @Override
        HexData encode(T obj) {
            throw new UnsupportedOperationException()
        }

        @Override
        T decode(HexData data) {
            throw new UnsupportedOperationException()
        }
    }

    final static DEFAULT = [] as StaticTypeImpl<Void>

    def "should create a correct default instance"() {
        expect:
        DEFAULT.static
    }

    def "should accept visitor"() {
        def visitor = new Type.VisitorImpl<Boolean>() {

            @Override
            <T> Boolean visit(StaticType<T> type) { true }
        }

        expect:
        DEFAULT.visit visitor
    }
}
