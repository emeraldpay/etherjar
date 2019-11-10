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

import io.infinitape.etherjar.rpc.DefaultBatch
import io.infinitape.etherjar.rpc.RpcCall
import io.infinitape.etherjar.test.MockRpcTransport
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class DefaultRpcClientSpec extends Specification {

    def "Executed batch"() {
        def transportMock = new MockRpcTransport()
        transportMock.mock("test", [], "foo")
        def client = new DefaultRpcClient(transportMock)
        def batch = new DefaultBatch()
        def call = RpcCall.create("test", [])

        when:
        def f = batch.add(call)
        def act = client.execute(batch)
        then:
        act.size() == 1
        act[0].get(5, TimeUnit.SECONDS) == "foo"
        f.result.get(5, TimeUnit.SECONDS) == "foo"
    }

    def "Executed single method"() {
        def transportMock = new MockRpcTransport()
        transportMock.mock("test", [], "bar")
        def client = new DefaultRpcClient(transportMock)
        def call = RpcCall.create("test", [])

        when:
        def act = client.execute(call).get(5, TimeUnit.SECONDS)
        then:
        act== "bar"
    }


/*
    def "call to convert trace list"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x19442f.json")
        HttpResponse resp = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, null))
        resp.entity = new InputStreamEntity(json)

        List<TraceItemJson> trace = [
                new TraceItemJson(transactionHash: TransactionId.from('0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'))
        ]
        when:
        def act = defaultRpcTransport.executeSync("trace_transaction",
                ['0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'],
                TraceList
        )
        then:
        1 * rpcConverterMock.toJson(_) >> '{"test":"can_convert_trace"}'
        1 * httpClientMock.execute(_ as HttpUriRequest) >> resp
        1 * rpcConverterMock.fromJson(json, TraceList) >> trace
        act == trace
    }
*/
}
