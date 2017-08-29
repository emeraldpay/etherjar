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

package io.infinitape.etherjar.rpc

import io.infinitape.etherjar.rpc.json.TraceItemJson
import spock.lang.Specification

import java.text.SimpleDateFormat

class JacksonRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    def "converts trace list"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0xdc6c6d.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 3
        act[0].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[0].subtraces == 2L
        act[0].traceAddress == []
        act[1].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[1].subtraces == 0L
        act[1].traceAddress == [0L]
        act[2].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[2].subtraces == 0L
        act[2].traceAddress == [1L]
    }
}
