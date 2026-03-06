/*
 * Copyright (c) 2026 EmeraldPay Ltd, All Rights Reserved.
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

package io.emeraldpay.etherjar.abi

import spock.lang.Specification

class DefaultRepositorySpec extends Specification {

    def "should return the same singleton repository instance"() {
        expect:
        DefaultRepository.instance.is(DefaultRepository.instance)
    }

    def "should resolve standard ethereum primitive types"() {
        when:
        def type = DefaultRepository.instance.search(input).get()

        then:
        type.class == expectedType
        type.canonicalName == canonicalName

        where:
        input       | expectedType      | canonicalName
        'bool'      | BoolType          | 'bool'
        'uint'      | UIntType          | 'uint256'
        'int'       | IntType           | 'int256'
        'address'   | AddressType       | 'address'
        'function'  | FunctionType      | 'function'
        'fixed'     | FixedType         | 'fixed128x128'
        'ufixed'    | UFixedType        | 'ufixed128x128'
        'string'    | StringType        | 'string'
        'bytes'     | DynamicBytesType  | 'bytes'
        'bytes32'   | BytesType         | 'bytes32'
    }

    def "should resolve array types using the same repository recursively"() {
        expect:
        DefaultRepository.instance.search(input).present
        DefaultRepository.instance.search(input).get().class == expectedType
        DefaultRepository.instance.search(input).get().canonicalName == canonicalName

        where:
        input           | expectedType      | canonicalName
        'uint[3]'       | ArrayType         | 'uint256[3]'
        'bool[]'        | DynamicArrayType  | 'bool[]'
        'bytes32[2][]'  | DynamicArrayType  | 'bytes32[2][]'
    }
}

