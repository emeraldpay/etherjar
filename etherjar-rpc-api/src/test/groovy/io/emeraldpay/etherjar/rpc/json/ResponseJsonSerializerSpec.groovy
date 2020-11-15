/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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
package io.emeraldpay.etherjar.rpc.json

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import io.emeraldpay.etherjar.rpc.RpcResponseError
import spock.lang.Specification

class ResponseJsonSerializerSpec extends Specification {

    def objectMapper = new JacksonRpcConverter()

    def "Serialize when result is string"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = "foo"
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":"foo"}'
    }

    def "Serialize when id/result are string"() {
        setup:
        def val = new ResponseJson()
        val.id = "19591"
        val.result = "foo"
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":"19591","result":"foo"}'
    }

    def "Serialize when id is long"() {
        setup:
        def val = new ResponseJson()
        val.id = 1057264140543346L
        val.result = "foo"
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1057264140543346,"result":"foo"}'
    }

    def "Serialize when id/result are number"() {
        setup:
        def val = new ResponseJson()
        val.id = 5019
        val.result = 100
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":5019,"result":100}'
    }

    def "Serialize when result is null"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = null
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":null}'
    }

    def "Serialize when result is txid"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = TransactionId.from("0x1d69f92bd36ecc935bc8c31c3a161db0e856d27e0d10884aa9d831e0d54201a4")
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":"0x1d69f92bd36ecc935bc8c31c3a161db0e856d27e0d10884aa9d831e0d54201a4"}'
    }

    def "Serialize when result is address"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = Address.from("0x4D3C2271Bb98E41D29896318D29f9E017B1c1669")
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":"0x4d3c2271bb98e41d29896318d29f9e017b1c1669"}'
    }

    def "Serialize when result is Wei"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = Wei.ofEthers(15.616)
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":"0xd8b72d434c800000"}'
    }

    def "Serialize when result is Transaction"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        def tx = new TransactionJson()
        tx.from = Address.from("0x4D3C2271Bb98E41D29896318D29f9E017B1c1669")
        tx.to = Address.from("0x4D3C2271Bb98E41D29896318D29f9E017B1c1669")
        tx.nonce = 1015
        tx.value = Wei.ofEthers(15.616)
        tx.gas = BigInteger.valueOf(21000)
        tx.gasPrice = Wei.ofEthers(0.0001)
        val.result = tx
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"result":' +
                '{"nonce":"0x3f7",' +
                '"from":"0x4d3c2271bb98e41d29896318d29f9e017b1c1669",' +
                '"to":"0x4d3c2271bb98e41d29896318d29f9e017b1c1669",' +
                '"value":"0xd8b72d434c800000",' +
                '"gasPrice":"0x5af3107a4000",' +
                '"gas":"0x5208"}}'

    }

    def "Serialize w/o result when has error"() {
        setup:
        def val = new ResponseJson()
        val.id = 1
        val.result = Wei.ofEthers(15.616)
        val.error = new RpcResponseError()
        val.error.message = "foobar"
        val.error.code = -32101
        when:
        def act = objectMapper.toJson(val)
        then:
        act == '{"jsonrpc":"2.0","id":1,"error":{"code":-32101,"message":"foobar"}}'
    }
}
