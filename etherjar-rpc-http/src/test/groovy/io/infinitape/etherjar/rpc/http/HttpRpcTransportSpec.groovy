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

package io.infinitape.etherjar.rpc.http

import io.infinitape.etherjar.rpc.DefaultBatch
import io.infinitape.etherjar.rpc.JacksonRpcConverter
import io.infinitape.etherjar.rpc.RpcCall
import io.infinitape.etherjar.rpc.RpcException
import io.infinitape.etherjar.rpc.RpcResponseError
import io.infinitape.etherjar.rpc.http.HttpRpcTransport
import io.infinitape.etherjar.rpc.transport.RpcTransport
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.client.HttpClient
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HttpRpcTransportSpec extends Specification {

    HttpRpcTransport defaultRpcTransport

    HttpClient httpClientMock

    def setup() {
        httpClientMock = Mock(HttpClient)
        defaultRpcTransport = HttpRpcTransport.newBuilder()
            .setHttpClient(httpClientMock)
            .build()
    }

    def "Fail batch items on batch exception"() {
        when:
        def req = new RpcTransport.RpcRequest(RpcCall.create("test"), 1)
        def callF = defaultRpcTransport.execute([req])
        def act = callF.get(1, TimeUnit.SECONDS)
        then:
        1 * httpClientMock.execute(_, _) >> { throw new IOException("Test error") }
        !callF.isCompletedExceptionally()
        with(act[0]) {
            error != null
            with (error) {
                code == RpcResponseError.CODE_INTERNAL_ERROR
                rpcMessage == "Test error"
            }
            payload == null
        }
    }

    def "Fail batch items on non-OK response"() {
        setup:
        def respMock = Mock(HttpResponse)
        when:
        def f = defaultRpcTransport.execute([
            new RpcTransport.RpcRequest(RpcCall.create("test"), 1)
        ])
        def act = f.get(1, TimeUnit.SECONDS)
        then:
        1 * httpClientMock.execute(_, _) >> respMock
        1 * respMock.getStatusLine() >> new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), 503, "Test")
        !f.isCompletedExceptionally()
        with(act[0]) {
            error != null
            with (error) {
                code == RpcResponseError.CODE_INTERNAL_ERROR
                rpcMessage == "Server returned error response: 503"
            }
            payload == null
        }
    }

    def "Empty batch"() {
        when:
        def f = defaultRpcTransport.execute([])
        def act = f.get()
        then:
        act.size() == 0
        0 * httpClientMock.execute(_, _)
    }

}
