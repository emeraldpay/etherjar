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

package io.emeraldpay.etherjar.contract

import io.emeraldpay.etherjar.abi.BytesType
import io.emeraldpay.etherjar.abi.DynamicBytesType
import io.emeraldpay.etherjar.abi.StringType
import io.emeraldpay.etherjar.abi.UIntType
import spock.lang.Specification

class MethodIdBuilderSpec extends Specification {

    def "should parse free-form signature with returns section"() {
        when:
        def parsed = MethodIdBuilder.parse(signature)

        then:
        parsed == expected

        where:
        signature                                                                           | expected
        'function reverse(bytes, uint256) view returns (string, address, address)'          | MethodIdBuilder.fromSignature('reverse', DynamicBytesType.DEFAULT, UIntType.DEFAULT)
        'function text(bytes32 node, string key) view returns (string)'                     | MethodIdBuilder.fromSignature('text', new BytesType(32), StringType.DEFAULT)
        'text(bytes32 node, string key) external pure returns (string value)'               | MethodIdBuilder.fromSignature('text', new BytesType(32), StringType.DEFAULT)
    }

    def "should parse a signature without parameters"() {
        expect:
        MethodIdBuilder.parse('function totalSupply() view returns (uint256)') == MethodIdBuilder.fromSignature('totalSupply')
    }

    def "should reject malformed signatures"() {
        when:
        MethodIdBuilder.parse(signature)

        then:
        thrown IllegalArgumentException

        where:
        signature << [
                '',
                'function bad(uint256',
                'function bad(uint256) returns string',
                'function bad(unknown) returns (uint256)'
        ]
    }

    def "should parse ERC20 method signatures and match known method IDs"() {
        when:
        def parsed = MethodIdBuilder.parse(signature)

        then:
        parsed.toHex() == expectedHex

        where:
        signature                                                                           | expectedHex
        'function totalSupply() view returns (uint256)'                                     | '0x18160ddd'
        'function balanceOf(address _owner) view returns (uint256)'                         | '0x70a08231'
        'function transfer(address _to, uint256 _value) returns (bool)'                     | '0xa9059cbb'
        'function approve(address _spender, uint256 _value) returns (bool)'                 | '0x095ea7b3'
        'function transferFrom(address _from, address _to, uint256 _value) returns (bool)'  | '0x23b872dd'
        'function allowance(address _owner, address _spender) view returns (uint256)'       | '0xdd62ed3e'
    }
}

