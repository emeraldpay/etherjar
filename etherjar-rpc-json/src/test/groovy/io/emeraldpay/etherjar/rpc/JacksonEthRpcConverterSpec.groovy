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

package io.emeraldpay.etherjar.rpc

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.rpc.json.*
import spock.lang.Specification

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class JacksonEthRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()

    def "Encode basic call data"() {
        def callData = new TransactionCallJson(
            to: Address.from('0x57d90b64a1a57749b0f932f1a3395792e12e7055'),
            data: HexData.from('0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2')
        )
        def req = new RequestJson(
            "eth_call",
            Arrays.asList(callData, 'latest'),
            1
        )

        when:
        def act = jacksonRpcConverter.toJson(req)

        then:
        act == '{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"0x57d90b64a1a57749b0f932f1a3395792e12e7055","data":"0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2"},"latest"],"id":1}'
    }

    def "Encode full call data"() {
        def callData = new TransactionCallJson(
            from: Address.from("0xb7819ff807d9d52a9ce5d713dc7053e8871e077b"),
            to: Address.from('0x57d90b64a1a57749b0f932f1a3395792e12e7055'),
            data: HexData.from('0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2'),
            gas: 100000,
            gasPrice: Wei.ofEthers(0.002),
            value: Wei.ofEthers(1.5)
        )
        def req = new RequestJson(
            "eth_call",
            Arrays.asList(callData, 'latest'),
            1
        )

        when:
        def act = jacksonRpcConverter.toJson(req)

        then:
        act == '{"jsonrpc":"2.0","method":"eth_call","params":[' +
            '{"from":"0xb7819ff807d9d52a9ce5d713dc7053e8871e077b",' +
            '"to":"0x57d90b64a1a57749b0f932f1a3395792e12e7055",' +
            '"gas":"0x0186a0",' +
            '"gasPrice":"0x071afd498d0000",' +
            '"value":"0x14d1120d7b160000",' +
            '"data":"0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2"}' +
            ',"latest"],"id":1}'
    }

    def "Can parse response with string id"() {
        setup:
        String json = "{" +
            "\"jsonrpc\": \"2.0\"," +
            "\"result\": \"0x0000000000000000000000000000000000000000000000000000000000000000\"," +
            "\"id\": \"c96fb505261cff21040d5939393b21c1\"" +
            "}"
        when:
        def act = jacksonRpcConverter.fromJson(new ByteArrayInputStream(json.getBytes()), String, String)
        then:
        act == "0x0000000000000000000000000000000000000000000000000000000000000000"
    }

}
