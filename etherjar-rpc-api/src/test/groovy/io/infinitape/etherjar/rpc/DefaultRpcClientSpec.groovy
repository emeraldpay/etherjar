package io.infinitape.etherjar.rpc

import io.infinitape.etherjar.rpc.transport.BatchStatus
import io.infinitape.etherjar.rpc.transport.RpcTransport
import io.infinitape.etherjar.test.MockRpcTransport
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class DefaultRpcClientSpec extends Specification {

    def "Executed batch"() {
        def transportMock = new MockRpcTransport()
        transportMock.mock("test", [], "foo")
        def client = new DefaultRpcClient(transportMock)
        def batch = new Batch()
        def call = RpcCall.create("test", [])

        def status = BatchStatus.newBuilder().withTotal(1).withSucceed(1).withFailed(0).build()

        when:
        def f = batch.add(call)
        def act = client.execute(batch).get(5, TimeUnit.SECONDS)
        then:
        act == status
        f.get() == "foo"
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
}
