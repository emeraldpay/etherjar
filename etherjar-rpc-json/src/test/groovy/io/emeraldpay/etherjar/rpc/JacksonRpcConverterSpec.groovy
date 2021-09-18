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


import io.emeraldpay.etherjar.rpc.json.BlockJson
import io.emeraldpay.etherjar.rpc.json.TraceItemJson
import io.emeraldpay.etherjar.rpc.json.TransactionJson
import spock.lang.Specification

import java.text.SimpleDateFormat

class JacksonRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    def "converts batch with one item"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("batch/one-item.json")
        def target = [
                1: TransactionJson
        ]
        when:
        def act = jacksonRpcConverter.parseBatch(json, target)
        then:
        act.size() == 1
        act[0].result instanceof TransactionJson
        act[0].result.with { TransactionJson tx ->
            tx.hash.toHex() == "0x1a2169d29474fe7bbddb7e601d8a7af8f7ece67e4628050d904a46218fdef3e8"
        }

    }

    def "converts batch with few similar items"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("batch/similar-items.json")
        def target = [
                1: BlockJson,
                2: BlockJson,
                3: BlockJson
        ]
        when:
        def act = jacksonRpcConverter.parseBatch(json, target)
        then:
        act.size() == 3
        act[0].result instanceof BlockJson
        act[0].result.with { BlockJson block ->
            block.hash.toHex() == "0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"
        }
        act[1].result instanceof BlockJson
        act[1].result.with { BlockJson block ->
            block.hash.toHex() == "0xcfef94017c51845ba69fa3c70ea6ae5dc9664649e6252c2a2d4e0300121c8454"
        }
        act[2].result instanceof BlockJson
        act[2].result.with { BlockJson block ->
            block.hash.toHex() == "0xe8df6e70c67cbb1a79304ec4b93d2e83b7578fc718dd344a4a26811be713da8a"
        }
    }

    def "converts batch with few different items"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("batch/many-items.json")
        def target = [
                1: BlockJson,
                2: TransactionJson,
                3: Boolean,
                4: String,
                5: String,
                6: String
        ]
        when:
        def act = jacksonRpcConverter.parseBatch(json, target)
        then:
        act.size() == 6
        act[0].id == 4
        act[0].result == "0x86ad41c878a0f27cbdb399ac71770a691b19f1fe"

        act[1].id == 3
        act[1].result == false

        act[2].id == 6
        act[2].result == "0x2a"

        act[3].id == 2
        act[3].result == null

        act[4].id == 1
        act[4].result instanceof BlockJson
        act[4].result.with { BlockJson block ->
            block.hash.toHex() == "0x4eeb9aa586c63c0f1ce033e6c6fb44b4db1ffe8c5e19c93e2b768c71b5f9cb9c"
        }

        act[5].id == 5
        act[5].result == "0x435901"
    }
}
