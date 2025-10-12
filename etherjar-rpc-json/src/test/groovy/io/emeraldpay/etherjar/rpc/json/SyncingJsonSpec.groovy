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

import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class SyncingJsonSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()

    def "Reads not syncing"() {
        setup:
        def json = '{' +
            '  "id":1,' +
            '  "jsonrpc": "2.0",' +
            '  "result": false' +
            '}'
        when:
        def act = jacksonRpcConverter.fromJson(new ByteArrayInputStream(json.getBytes()), SyncingJson)
        then:
        !act.syncing
    }

    def "Reads syncing"() {
        setup:
        def json = '{\n' +
            '  "id":1,\n' +
            '  "jsonrpc": "2.0",\n' +
            '  "result": {' +
            '    "startingBlock": "0x384",\n' +
            '    "currentBlock": "0x386",\n' +
            '    "highestBlock": "0x454"' +
            '  }\n' +
            '}'
        when:
        def act = jacksonRpcConverter.fromJson(new ByteArrayInputStream(json.getBytes()), SyncingJson)
        then:
        act.syncing
        act.currentBlock == 902
        act.highestBlock == 1108
        act.startingBlock == 900
    }

    def "Reads syncing in Erigon"() {
        setup:
        def json = '{\n' +
            '  "id":1,\n' +
            '  "jsonrpc": "2.0",\n' +
            '  "result": {' +
            '    "currentBlock": "0x8f5a95",\n' +
            '    "highestBlock": "0x8f5367",\n' +
            '    "stages": [\n' +
            '        {\n' +
            '          "stage_name": "OtterSync",\n' +
            '          "block_number": "0x8f5a95"\n' +
            '        },\n' +
            '        {\n' +
            '          "stage_name": "Headers",\n' +
            '          "block_number": "0x8f5a95"\n' +
            '        }\n' +
            '    ],\n' +
            '    "startingBlock": "0x0"' +
            '  }\n' +
            '}'
        when:
        def act = jacksonRpcConverter.fromJson(new ByteArrayInputStream(json.getBytes()), SyncingJson)
        then:
        act.syncing
        act.currentBlock == 0x8f5a95
        act.highestBlock == 0x8f5367
        act.startingBlock == 0
        act instanceof SyncingJson.AtBlock
        with (act as SyncingJson.AtBlock) {
            stages.size() == 2
            stages[0].stageName == "OtterSync"
            stages[0].block == 0x8f5a95
            stages[1].stageName == "Headers"
            stages[1].block == 0x8f5a95
        }
    }
}
