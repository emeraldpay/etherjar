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

package io.infinitape.etherjar.rpc.transport

import io.infinitape.etherjar.rpc.RpcConverter
import org.apache.http.client.HttpClient
import spock.lang.Specification

import java.util.concurrent.Executors

class DefaultRpcTransportSpec extends Specification {

    DefaultRpcTransport defaultRpcTransport

    RpcConverter rpcConverterMock
    HttpClient httpClientMock

    def setup() {
        rpcConverterMock = Mock(RpcConverter)
        httpClientMock = Mock(HttpClient)
        defaultRpcTransport = new DefaultRpcTransport(
                new URI('http://localhost:8545'),
                rpcConverterMock,
                Executors.newFixedThreadPool(1),
                httpClientMock
        )
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
