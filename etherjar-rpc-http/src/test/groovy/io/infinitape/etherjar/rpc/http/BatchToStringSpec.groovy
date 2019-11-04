/*
 * Copyright (c) 2016-2019 Igor Artamonov
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
package io.infinitape.etherjar.rpc.http

import io.infinitape.etherjar.domain.Address
import io.infinitape.etherjar.rpc.Commands
import io.infinitape.etherjar.rpc.JacksonRpcConverter
import io.infinitape.etherjar.rpc.ReactorBatch
import io.infinitape.etherjar.rpc.json.BlockTag
import io.netty.buffer.ByteBuf
import reactor.core.publisher.Flux
import spock.lang.Specification

class BatchToStringSpec extends Specification {

    def "Convert to JSON an empty call"() {
        setup:
        BatchToString converter = new BatchToString(new JacksonRpcConverter())
        ReactorBatch batch = new ReactorBatch()
        when:
        def act = converter.convertToJson(batch).getBatch()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[]'
    }

    def "Convert to JSON a single call"() {
        setup:
        BatchToString converter = new BatchToString(new JacksonRpcConverter())

        ReactorBatch batch = new ReactorBatch()
        batch.add(Commands.eth().blockNumber)
        when:
        def act = converter.convertToJson(batch).getBatch()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}]'
    }

    def "Convert to JSON two calls"() {
        setup:
        BatchToString converter = new BatchToString(new JacksonRpcConverter())

        ReactorBatch batch = new ReactorBatch()
        batch.add(Commands.eth().blockNumber)
        batch.add(Commands.eth().getBalance(Address.EMPTY, BlockTag.LATEST))
        when:
        def act = converter.convertToJson(batch).getBatch()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[' +
            '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}' +
            ',' +
            '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x0000000000000000000000000000000000000000","latest"],"id":2}' +
            ']'
    }

    // ----

    byte[] readBytes(Flux<ByteBuf> input) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream()
        input.reduce(buf, { x, val ->
            x.write(val.array())
            return x
        }).subscribe()
        return buf.toByteArray()
    }
}
