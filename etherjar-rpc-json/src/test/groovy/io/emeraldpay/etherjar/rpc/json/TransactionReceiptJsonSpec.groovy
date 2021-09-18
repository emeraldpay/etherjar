/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class TransactionReceiptJsonSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = new ObjectMapper()


    def "Parse receipt 0x5929b3"() {
        InputStream json = TransactionReceiptJsonSpec.classLoader.getResourceAsStream("receipt/0x5929b3.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.blockHash.toHex() == "0x2c3cfd4c7f2b58859371f5795eaf8524caa6e63145ac7e9df23c8d63aab891ae"
        act.blockNumber == 2177930
        act.contractAddress == null
        act.cumulativeGasUsed == 21000
        act.gasUsed == 21000
        act.transactionHash.toHex() == '0x5929b36be4586c57bd87dfb7ea6be3b985c1f527fa3d69d221604b424aeb4197'
        act.transactionIndex == 0
        act.logs.size() == 0
        act.status == null
    }

    def "Parse receipt 0x3f34b1 with status 1"() {
        InputStream json = TransactionReceiptJsonSpec.classLoader.getResourceAsStream("receipt/0x3f34b1.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.status == 1
    }

    def "Parse receipt 0x8ca5b1 with status 0"() {
        InputStream json = TransactionReceiptJsonSpec.classLoader.getResourceAsStream("receipt/0x8ca5b1.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.status == 0
    }

    def "Parse receipt 0x8883dd"() {
        InputStream json = TransactionReceiptJsonSpec.classLoader.getResourceAsStream("receipt/0x8883dd.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
        act.blockNumber == 1709877
        act.contractAddress == null
        act.cumulativeGasUsed == 0x045715
        act.gasUsed == 0x01c6d5
        act.transactionHash.toHex() == '0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46'
        act.transactionIndex == 8
        act.logs.size() == 1
        act.logs[0].address.toHex() == '0x4b8e1ad58657f8b4b036ad12afbcef54d24ac9ba'
        act.logs[0].blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
        act.logs[0].blockNumber == 1709877
        act.logs[0].data.toHex() == "0xbc2ddc901129318b063f3853f46f626f768f8cdaffeec4577eb7febe8e37f29000000000000000000000000000000000000000000000000000000000000000027368613235360000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000202fb928f34a04238701090b138c1d4652d6694a06f9aeea0706e6c474e801673a"
        act.logs[0].logIndex == 0
        act.logs[0].topics*.toHex() == [
            "0x006409c471c01f75fa2c8509f25aae87aa4e1d13b3eda6dcf9cabd084c053265",
            "0x000000000000000000000000e7827ba56a848dff35ccff016f6c0055603ec454"
        ]
        act.logs[0].transactionHash.toHex() == "0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46"
        act.logs[0].transactionIndex == 8
    }

    def "deserialize 0x8ca5b1"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("receipt/0x8ca5b1.json")

        when:
        def receipt = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        receipt != null
        receipt.blockHash.toHex() == "0x57e733d37ea97789768c6b2b236436ffa78cc590ba8444df11dc9e243cb19b87"
        receipt.blockNumber == 0xb1e133
        receipt.contractAddress == null
        receipt.cumulativeGasUsed == 0x2f4542
        receipt.from.toHex() == "0xcb6019dbce59dffb826fb1bb75046986740dc011"
        receipt.gasUsed == 0xade4
        receipt.logs.size() == 0
        receipt.logsBloom.toHex() == "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
        receipt.status == 0x0
        receipt.to.toHex() == "0x0000000006a0403952389b70d8ee4e45479023db"
        receipt.transactionHash.toHex() == "0x8ca5b1926e6286b1a0555b017ac7cbf5ebb7f289b5ae16d79458100f6e1f866f"
        receipt.transactionIndex == 0x39
    }

    def "serialize 0x8ca5b1"() {
        setup:
        InputStream origJson = this.class.classLoader.getResourceAsStream("receipt/0x8ca5b1.json")
        def orig = jacksonRpcConverter.fromJson(origJson, TransactionReceiptJson)

        when:
        def json = objectMapper.writeValueAsString(orig)
        def read = objectMapper.readValue(json, TransactionReceiptJson)

        then:
        orig == read
        json.contains("0x57e733d37ea97789768c6b2b236436ffa78cc590ba8444df11dc9e243cb19b87")
    }

    def "deserialize 0x8883dd"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("receipt/0x8883dd.json")

        when:
        def receipt = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        receipt != null
        receipt.blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
        receipt.blockNumber == 0x1a1735
        receipt.contractAddress == null
        receipt.cumulativeGasUsed == 0x045715
        receipt.gasUsed == 0x01c6d5
        receipt.logs.size() == 1
        with(receipt.logs[0]) {
            address.toHex() == "0x4b8e1ad58657f8b4b036ad12afbcef54d24ac9ba"
            blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
            blockNumber == 0x1a1735
            data.toHex() == "0xbc2ddc901129318b063f3853f46f626f768f8cdaffeec4577eb7febe8e37f29000000000000000000000000000000000000000000000000000000000000000027368613235360000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000202fb928f34a04238701090b138c1d4652d6694a06f9aeea0706e6c474e801673a"
            logIndex == 0x0
            topics.collect { it.toHex() } == [
                "0x006409c471c01f75fa2c8509f25aae87aa4e1d13b3eda6dcf9cabd084c053265",
                "0x000000000000000000000000e7827ba56a848dff35ccff016f6c0055603ec454"
            ]
            transactionHash.toHex() == "0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46"
            transactionIndex == 0x8
        }
        receipt.transactionHash.toHex() == "0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46"
        receipt.transactionIndex == 0x08
    }

    def "serialize 0x8883dd"() {
        setup:
        InputStream origJson = this.class.classLoader.getResourceAsStream("receipt/0x8883dd.json")
        def orig = jacksonRpcConverter.fromJson(origJson, TransactionReceiptJson)

        when:
        def json = objectMapper.writeValueAsString(orig)
        def read = objectMapper.readValue(json, TransactionReceiptJson)

        then:
        orig == read
        json.contains("0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d")
    }

    def "deserialize 0xec00aa"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("receipt/0xec00aa.json")

        when:
        def receipt = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        receipt != null
        receipt.blockHash.toHex() == "0x57c09bd9f137287b3a4d2691bf5259df2107fcf3c1c69f5a50c9324f528d1793"
        receipt.blockNumber == 0xb93079
        receipt.contractAddress == null
        receipt.cumulativeGasUsed == 0xb0a50b
        receipt.from.toHex() == "0xcdbe8a515abe014b57a0af0a989cbe5dd9820d8e"
        receipt.gasUsed == 0x2e00c
        receipt.logs.size() == 9

        with(receipt.logs[0]) {
            address.toHex() == "0x32a7c02e79c4ea1008dd6564b35f131428673c41"
            blockHash.toHex() == "0x57c09bd9f137287b3a4d2691bf5259df2107fcf3c1c69f5a50c9324f528d1793"
            blockNumber == 0xb93079
            data.toHex() == "0x0000000000000000000000000000000000000000000000026d497efcdef80f67"
            removed != null && !removed
            logIndex == 0x148
            topics.collect { it.toHex() } == [
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x000000000000000000000000cdbe8a515abe014b57a0af0a989cbe5dd9820d8e",
                "0x0000000000000000000000002702bd7268793b5e1c7ee1ac2d9cae2ae2ecfe55"
            ]
            transactionHash.toHex() == "0xec00aa203d9db58d1a5cbf2a6f6430e0267faeed11f2bc1004925d88e69bb2cd"
            transactionIndex == 0x95
        }
        with(receipt.logs[3]) {
            address.toHex() == "0x2702bd7268793b5e1c7ee1ac2d9cae2ae2ecfe55"
            blockHash.toHex() == "0x57c09bd9f137287b3a4d2691bf5259df2107fcf3c1c69f5a50c9324f528d1793"
            blockNumber == 0xb93079
            data.toHex() == "0x00000000000000000000000000000000000000000000023306acfca54d5a842a000000000000000000000000000000000000000000000000000000b2baa1106a"
            logIndex == 0x14b
            removed != null && !removed
            topics.collect { it.toHex() } == [
                "0x1c411e9a96e071241c2f21f7726b17ae89e3cab4c78be50e062b03a9fffbbad1"
            ]
            transactionHash.toHex() == "0xec00aa203d9db58d1a5cbf2a6f6430e0267faeed11f2bc1004925d88e69bb2cd"
            transactionIndex == 0x95
        }

        receipt.logsBloom.toHex() == "0x10204004000000000000000080000000000000000000000000010000000000000000000000000000000000000000000002000000080000000000000000200000000000000000000008000008000000600000000000400000000000000000000000000000000000000000000000000000000000000000040000000018000000000000004000000000004000000000000000000000010000080020004800000000020000000000220200000000000000000000000000000000000010000040000000000002000000000000000000000000000000000000111000000002000020000018200000000000000000000000000000000000000000000080000020000000"
        receipt.status == 0x1
        receipt.to.toHex() == "0x7a250d5630b4cf539739df2c5dacb4c659f2488d"
        receipt.transactionHash.toHex() == "0xec00aa203d9db58d1a5cbf2a6f6430e0267faeed11f2bc1004925d88e69bb2cd"
        receipt.transactionIndex == 0x95
    }

    def "serialize 0xec00aa"() {
        setup:
        InputStream origJson = this.class.classLoader.getResourceAsStream("receipt/0xec00aa.json")
        def orig = jacksonRpcConverter.fromJson(origJson, TransactionReceiptJson)

        when:
        def json = objectMapper.writeValueAsString(orig)
        def read = objectMapper.readValue(json, TransactionReceiptJson)

        then:
        orig == read
        json.contains("0x57c09bd9f137287b3a4d2691bf5259df2107fcf3c1c69f5a50c9324f528d1793")
    }

    def "reads effectiveGasPrice"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("receipt/0xb8e7e1.json")

        when:
        def receipt = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        receipt.effectiveGasPrice != null
        receipt.effectiveGasPrice.toHex() == "0x1cc398813a"
    }

    def "reads type"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("receipt/0xb8e7e1.json")

        when:
        def receipt = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        receipt.type == 2
    }
}
