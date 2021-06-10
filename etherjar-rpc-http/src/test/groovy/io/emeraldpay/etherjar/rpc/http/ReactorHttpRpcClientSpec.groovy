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
package io.emeraldpay.etherjar.rpc.http

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.BlockHash
import io.emeraldpay.etherjar.rpc.Commands
import io.emeraldpay.etherjar.rpc.ReactorBatch
import io.emeraldpay.etherjar.rpc.RpcException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.Loggers
import spark.Spark
import spock.lang.Specification

import java.time.Duration
import java.util.regex.Pattern

class ReactorHttpRpcClientSpec extends Specification {


    def setup() {
        Spark.port(18545)
        Loggers.useVerboseConsoleLoggers()
    }

    def cleanup() {
        Spark.stop()
        Spark.awaitStop()
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

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
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call.result)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

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
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call1.result)
            .expectNext(68)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call2.result)
            .expectNext(Address.from("0x0000000000000000000000000000000000000000"))
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        requests.size() == 1
        requests[0] == "[{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1},{\"jsonrpc\":\"2.0\",\"method\":\"eth_coinbase\",\"params\":[],\"id\":2}]"

    }

    def "Make two calls using separated transport"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            if (req.body().contains('"id":1')) {
                return '{"jsonrpc":"2.0","id":1,"result":68}'
            } else if (req.body().contains('"id":2')) {
                return '{"jsonrpc":"2.0","id":2,"result":"0x0000000000000000000000000000000000000000"}'
            } else {
                return '{}'
            }
        }
        Spark.exception(Exception.class, { t, req, resp -> t.printStackTrace() })
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder()
            .connectTo("http://localhost:18545")
            .alwaysSeparate()
            .build()

        def expecting = [68, Address.from("0x0000000000000000000000000000000000000000")]

        when:
        ReactorBatch batch = new ReactorBatch()
        def call1 = batch.add(Commands.net().peerCount())
        def call2 = batch.add(Commands.eth().getCoinbase())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
        // response can come in different order, so just check for uniqueness
            .expectNextMatches({
                println("Received value 1: $it.value ${it.value.class}")
                return expecting.indexOf(it.value) >= 0 &&
                    expecting.remove(expecting.indexOf(it.value)) &&
                    it.error == null
            }).as("receive value 1")
            .expectNextMatches({
                println("Received value 2: $it.value ${it.value.class}")
                return expecting.indexOf(it.value) >= 0 &&
                    expecting.remove(expecting.indexOf(it.value)) &&
                    it.error == null
            }).as("receive value 2")
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call1.result)
            .expectNext(68)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call2.result)
            .expectNext(Address.from("0x0000000000000000000000000000000000000000"))
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        requests.size() == 2
        requests.sort()[0] == '{"jsonrpc":"2.0","method":"eth_coinbase","params":[],"id":2}'
        requests.sort()[1] == '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":1}'
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        def resp = client.execute(Commands.net().peerCount())

        then:
        StepVerifier.create(resp)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        requests.size() == 1
        requests[0] == "[{\"jsonrpc\":\"2.0\",\"method\":\"net_peerCount\",\"params\":[],\"id\":1}]"
    }

    def "Produce empty on null response"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": null}]'
        }
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        def resp = client.execute(Commands.eth().getBlock(BlockHash.empty()))

        then:
        StepVerifier.create(resp)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        requests.size() == 1
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError()
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call.result)
            .expectError()
            .verify(Duration.ofSeconds(5))

        requests.size() == 1
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError()
            .verify(Duration.ofSeconds(5))

        StepVerifier.create(call.result)
            .expectError()
            .verify(Duration.ofSeconds(5))

        requests.size() == 1
    }

    def "Error on no connection using separated transport"() {
        setup:
        def client = ReactorHttpRpcClient.newBuilder()
            .connectTo("http://localhost:18546")
            .alwaysSeparate()
            .build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError(RpcException)
            .verify(Duration.ofSeconds(3))

        StepVerifier.create(call.result)
            .expectError(RpcException)
            .verify(Duration.ofSeconds(3))
    }

    def "Error on no connection using separated transport using two calls"() {
        setup:
        def client = ReactorHttpRpcClient.newBuilder()
            .connectTo("http://localhost:18546")
            .alwaysSeparate()
            .build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call1 = batch.add(Commands.net().peerCount())
        def call2 = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp)
            .expectError(RpcException)
            .verify(Duration.ofSeconds(3))

        StepVerifier.create(call1.result)
            .expectError(RpcException)
            .verify(Duration.ofSeconds(3))
        StepVerifier.create(call2.result)
            .expectError(RpcException)
            .verify(Duration.ofSeconds(3))
    }

    def "Response mono processed without waiting for batch flux"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }
        Spark.awaitInitialization()
        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        client.execute(batch)

        then:
        StepVerifier.create(call.result)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(3))
        requests.size() == 1
    }

    def "Makes call only if actual result requested"() {
        setup:
        def requests = []
        Spark.post("/") {req, resp ->
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '[{"jsonrpc":"2.0","id":1, "result": 1}]'
        }
        Spark.awaitInitialization()
        def client = ReactorHttpRpcClient.newBuilder().connectTo("http://localhost:18545").build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def execution = client.execute(batch)
        def withoutExecution = Flux.just(2).concatWith(execution.map { 1 })
            .buffer(1).take(1)
        def withoutCall = Flux.just(100).concatWith(call.result.map { 101 })
            .buffer(1).take(1)

        then:
        StepVerifier.create(withoutExecution)
            .expectNext([2])
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        StepVerifier.create(withoutCall)
            .expectNext([100])
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        requests.size() == 0
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo(Mono.just("http://localhost:18545")).build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(5))
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder().connectTo(new URI("http://localhost:18545")).build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(5))
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
        Spark.awaitInitialization()

        def client = ReactorHttpRpcClient.newBuilder()
            .connectTo(new URI("http://localhost:18545"))
            .basicAuth("Aladdin", "OpenSesame")
            .build()

        when:
        ReactorBatch batch = new ReactorBatch()
        def call = batch.add(Commands.net().peerCount())
        def resp = client.execute(batch)

        then:
        StepVerifier.create(resp.then(call.result))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(5))

        requests[1] == "Basic QWxhZGRpbjpPcGVuU2VzYW1l"
    }

    int readId(String json) {
        Pattern p = ~/"id":(\d+)/
        return p.matcher(json).group(1).toInteger()
    }
}
