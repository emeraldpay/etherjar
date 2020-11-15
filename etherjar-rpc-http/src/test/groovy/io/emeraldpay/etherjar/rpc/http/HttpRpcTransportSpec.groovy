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

package io.emeraldpay.etherjar.rpc.http

import io.emeraldpay.etherjar.rpc.DefaultBatch
import io.emeraldpay.etherjar.rpc.RpcCall
import io.emeraldpay.etherjar.rpc.RpcException
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.client.HttpClient
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class HttpRpcTransportSpec extends Specification {

    HttpRpcTransport defaultRpcTransport

    HttpClient httpClientMock

    def setup() {
        httpClientMock = Mock(HttpClient)
        defaultRpcTransport = HttpRpcTransport.newBuilder()
            .httpClient(httpClientMock)
            .build()
    }

    def "Fail batch items on batch exception"() {
        when:
        def batch = new DefaultBatch()
        def req = batch.add(RpcCall.create("test"))
        def callF = defaultRpcTransport.execute(batch.getItems())
        def act = callF.get(1, TimeUnit.SECONDS)
        then:
        def t = thrown(ExecutionException)
        RpcException == t.cause.class
        1 * httpClientMock.execute(_, _) >> { throw new IOException("Test error") }
        callF.isCompletedExceptionally()
    }

    def "Fail batch items on non-OK response"() {
        setup:
        def respMock = Mock(HttpResponse)
        when:
        def batch = new DefaultBatch()
        def req = batch.add(RpcCall.create("test"))

        def f = defaultRpcTransport.execute(batch.getItems())
        def act = f.get(1, TimeUnit.SECONDS)
        then:
        def t = thrown(ExecutionException)
        RpcException == t.cause.class

        1 * httpClientMock.execute(_, _) >> respMock
        1 * respMock.getStatusLine() >> new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), 503, "Test")
        f.isCompletedExceptionally()
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
