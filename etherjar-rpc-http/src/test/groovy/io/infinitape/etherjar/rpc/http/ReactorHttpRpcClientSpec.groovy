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
import io.infinitape.etherjar.rpc.ReactorBatch
import io.infinitape.etherjar.rpc.http.ReactorHttpRpcClient
import io.infinitape.etherjar.rpc.json.BlockTag
import io.netty.buffer.ByteBuf
import jdk.nashorn.internal.runtime.regexp.joni.Regex
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spock.lang.Specification

import java.time.Duration
import java.util.regex.Pattern

class ReactorHttpRpcClientSpec extends Specification {

    ReactorHttpRpcClient client = ReactorHttpRpcClient.newBuilder().build()

    def setup() {
        Spark.port(18545)
    }

    def cleanup() {
        Spark.stop()
        Spark.awaitStop()
    }

    def "Convert to JSON an empty call"() {
        setup:
        ReactorBatch batch = new ReactorBatch()
        when:
        def act = client.convertToJson(batch).getT1()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[]'
    }

    def "Convert to JSON a single call"() {
        setup:
        ReactorBatch batch = new ReactorBatch()
        batch.add(Commands.eth().blockNumber)
        when:
        def act = client.convertToJson(batch).getT1()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}]'
    }

    def "Convert to JSON two calls"() {
        setup:
        ReactorBatch batch = new ReactorBatch()
        batch.add(Commands.eth().blockNumber)
        batch.add(Commands.eth().getBalance(Address.EMPTY, BlockTag.LATEST))
        when:
        def act = client.convertToJson(batch).getT1()
        def actStr = new String(readBytes(act))
        then:
        actStr == '[' +
            '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}' +
            ',' +
            '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x0000000000000000000000000000000000000000","latest"],"id":2}' +
            ']'
    }

    def "Make simple call"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectNextMatches({
                println("Received value: $it.value")
                return it.value == 1 && it.error == null
            }).as("receive value")
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        StepVerifier.create(call.result)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        requests.size() == 1
        requests[0] == "[{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1}]"
    }

    def "Make two calls"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '['+
                '{"jsonrpc":"2.0","id":1,"result":68},' +
                '{"jsonrpc":"2.0","id":2,"result":"0x0000000000000000000000000000000000000000"}' +
                ']'
        }
        Spark.exception(Exception.class, { t, req, resp -> t.printStackTrace()})


        def client = ReactorHttpRpcClient.newBuilder().setTarget("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call1 = batch.add(Commands.net().peerCount())
        def call2 = batch.add(Commands.eth().getCoinbase())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectNextMatches({
                println("Received value: $it.value ")
                return it.value == 68 && it.error == null
            }).as("receive value for peers")
            .expectNextMatches({
                println("Received value: $it.value ${it.value.class}")
                return it.value == Address.from("0x0000000000000000000000000000000000000000") && it.error == null
            }).as("receive value for coinbase")
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        StepVerifier.create(call1.result)
            .expectNext(68)
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        StepVerifier.create(call2.result)
            .expectNext(Address.from("0x0000000000000000000000000000000000000000"))
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        requests.size() == 1
        requests[0] == "[{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1},{\"jsonrpc\":\"2.0\",\"method\":\"eth_coinbase\",\"params\":[],\"id\":2}]"

    }

    def "Make simple call with shortcut call"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget("http://localhost:18545").build()

        when:
        def resp = client.execute(Commands.net().peerCount())

        then:
        StepVerifier.create(resp)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        requests.size() == 1
        requests[0] == "[{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1}]"

    }

    def "Error on invalid http code"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            requests.add(req.body())
            resp.status(501)
            resp.type("application/json")
            return '[]'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError()
            .verify(Duration.ofSeconds(1))

        StepVerifier.create(call.result)
            .expectError()
            .verify(Duration.ofSeconds(1))

    }

    def "Error on invalid response"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError()
            .verify(Duration.ofSeconds(1))

        StepVerifier.create(call.result)
            .expectError()
            .verify(Duration.ofSeconds(1))

    }

    def "Accept mono url"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget(Mono.just("http://localhost:18545")).build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1))
    }

    def "Accept URI object"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }


        def client = ReactorHttpRpcClient.newBuilder().setTarget(new URI("http://localhost:18545")).build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1))
    }

    def "Uses basic auth"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()} ${req.headers()}")
            requests.add(req.body())
            requests.add(req.headers("Authorization"))
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }


        def client = ReactorHttpRpcClient.newBuilder()
            .setTarget(new URI("http://localhost:18545"))
            .setBasicAuth("Aladdin", "OpenSesame")
            .build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1))

        requests[1] == "Basic QWxhZGRpbjpPcGVuU2VzYW1l"
    }

    int readId(String json) {
        Pattern p = ~/"id":(\d+)/
        return p.matcher(json).group(1).toInteger()
    }

    byte[] readBytes(Flux<ByteBuf> input) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream()
        input.reduce(buf, { x, val ->
            x.write(val.array())
            return x
        }).subscribe()
        return buf.toByteArray()
    }
}
