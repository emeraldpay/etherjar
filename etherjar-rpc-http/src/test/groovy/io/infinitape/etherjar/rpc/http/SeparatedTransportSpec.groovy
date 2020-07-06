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

import io.infinitape.etherjar.rpc.BatchCallContext
import io.infinitape.etherjar.rpc.Commands
import io.infinitape.etherjar.rpc.JacksonRpcConverter
import io.infinitape.etherjar.rpc.ReactorBatch
import io.infinitape.etherjar.rpc.RpcCallResponse
import io.infinitape.etherjar.rpc.RpcConverter
import io.infinitape.etherjar.rpc.RpcException
import io.infinitape.etherjar.rpc.RpcResponseError
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.test.StepVerifier
import spark.Spark
import spock.lang.Specification

import java.time.Duration

class SeparatedTransportSpec extends Specification {

    RpcConverter rpcConverter = new JacksonRpcConverter()

    // port may be held open a couple of seconds after stopping the test, need new port each time to avoid collision
    static int port = 18545

    def setup() {
        Spark.port(++port)
    }

    def cleanup() {
        Spark.stop()
        Spark.awaitStop()
    }

    def "Make a simple call"() {
        setup:
        def requests = []
        Spark.post("/") { req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(200)
            resp.type("application/json")
            return '{"jsonrpc":"2.0","id":1,"result": "0x68"}'
        }
        Spark.awaitInitialization()

        def transport = new SeparatedTransport(HttpClient.create(), Mono.just("http://localhost:${port}".toString()), rpcConverter)

        def batch = new ReactorBatch();
        def call = Commands.net().peerCount()
        def item = batch.add(call)

        def context = new BatchCallContext()
        context.add(item)

        when:
        def act = transport.execute(batch.getItems(), context)

        then:
        StepVerifier.create(act)
            .expectNext(new RpcCallResponse(call, 104))
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        requests == [
            '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":1}'
        ]
    }

    def "Fail on non-ok response"() {
        setup:
        def requests = []
        Spark.post("/") { req, resp ->
            println("Received request: ${req.body()}")
            requests.add(req.body())
            resp.status(403)
            resp.type("application/json")
            return '{"jsonrpc":"2.0","id":1}'
        }
        Spark.awaitInitialization()

        def transport = new SeparatedTransport(HttpClient.create(), Mono.just("http://localhost:${port}".toString()), rpcConverter)

        def batch = new ReactorBatch();
        def call = Commands.net().peerCount()
        def item = batch.add(call)

        def context = new BatchCallContext()
        context.add(item)

        when:
        def act = transport.execute(batch.getItems(), context)

        then:
        StepVerifier.create(act)
            .expectError()
            .verify(Duration.ofSeconds(3))
    }

    def "Return RpcException on no connection"() {
        setup:
        def transport = new SeparatedTransport(HttpClient.create(), Mono.just("http://localhost:18000"), rpcConverter)

        def batch = new ReactorBatch();
        def call = Commands.net().peerCount()
        def item = batch.add(call)

        def context = new BatchCallContext()
        context.add(item)

        when:
        def act = transport.execute(batch.getItems(), context)

        then:
        StepVerifier.create(act)
            .expectErrorMatches { t ->
                println("Error: ${t}")
                t.class == RpcException &&
                    ((RpcException)t).code == RpcResponseError.CODE_UPSTREAM_CONNECTION_ERROR &&
                    ((RpcException)t).rpcMessage == "Connection error"
            }
            .verify(Duration.ofSeconds(3))
    }
}
