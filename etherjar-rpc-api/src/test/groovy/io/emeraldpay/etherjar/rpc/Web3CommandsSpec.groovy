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
package io.emeraldpay.etherjar.rpc

import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class Web3CommandsSpec extends Specification {

    def clientVersion() {
        when:
        def call = Commands.web3().clientVersion()

        then:
        call.method == "web3_clientVersion"
        call.params == []
        call.jsonType == String
        call.resultType == String
    }

    def sha3() {
        when:
        def call = Commands.web3().sha3(HexData.from("0x00"))

        then:
        call.method == "web3_sha3"
        call.params == ["0x00"]
        call.jsonType == String
        call.resultType == io.emeraldpay.etherjar.hex.Hex32
    }
}
