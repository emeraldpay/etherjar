package io.emeraldpay.etherjar.rpc.kotlin

import io.emeraldpay.etherjar.rpc.Commands
import io.emeraldpay.etherjar.rpc.RpcCallResponse
import io.emeraldpay.etherjar.rpc.RpcException
import io.emeraldpay.etherjar.rpc.RpcResponseError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList

class DefaultCoroutineRpcClientTest : ShouldSpec({

    lateinit var mockTransport: CoroutineRpcTransport
    lateinit var client: DefaultCoroutineRpcClient

    beforeEach {
        mockTransport = mockk {
            every { close() } just Runs
        }
        client = DefaultCoroutineRpcClient(mockTransport)
    }

    afterEach {
        client.close()
        clearMocks(mockTransport)
    }

    should("execute single RPC call successfully") {
        // Given
        val call = Commands.web3().clientVersion()
        val expectedResult = "Geth/v1.10.0-stable/linux-amd64/go1.17"
        val mockResponse = RpcCallResponse(call, expectedResult)

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns listOf(mockResponse)

        // When
        val result = client.execute(call)

        // Then
        result shouldBe expectedResult
        coVerify(exactly = 1) { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) }
    }

    should("throw exception when RPC call fails") {
        // Given
        val call = Commands.web3().clientVersion()
        val rpcException = RpcException(RpcResponseError.CODE_INTERNAL_ERROR, "Server error")
        val mockResponse = RpcCallResponse(call, rpcException)

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns listOf(mockResponse)

        // When & Then
        val exception = shouldThrow<RpcException> {
            client.execute(call)
        }
        exception.rpcMessage shouldBe "Server error"
        exception.error.code shouldBe RpcResponseError.CODE_INTERNAL_ERROR
    }

    should("execute batch of RPC calls") {
        // Given
        val batch = client.createBatch()
        val call1 = Commands.web3().clientVersion()
        val call2 = Commands.web3().clientVersion()

        batch.add(call1)
        batch.add(call2)

        val mockResponse1 = RpcCallResponse(call1, "Geth/v1.10.0")
        val mockResponse2 = RpcCallResponse(call2, "Parity/v2.5.0")

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns
            listOf(mockResponse1, mockResponse2)

        // When
        val responses = client.execute(batch)

        // Then
        responses shouldHaveSize 2
        responses[0].value shouldBe "Geth/v1.10.0"
        responses[1].value shouldBe "Parity/v2.5.0"
        coVerify(exactly = 1) { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) }
    }

    should("create batch with proper items") {
        // Given
        val batch = client.createBatch()

        // When
        val item1 = batch.add(Commands.web3().clientVersion())
        val item2 = batch.add(Commands.web3().clientVersion())

        // Then
        batch.size shouldBe 2
        item1.call.method shouldBe "web3_clientVersion"
        item2.call.method shouldBe "web3_clientVersion"
        item1.id shouldBe 1
        item2.id shouldBe 2
    }

    should("execute call as Flow") {
        // Given
        val call = Commands.web3().clientVersion()
        val expectedResult = "Geth/v1.10.0-stable"
        val mockResponse = RpcCallResponse(call, expectedResult)

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns listOf(mockResponse)

        // When
        val flowResults = client.executeFlow(call).toList()

        // Then
        flowResults shouldHaveSize 1
        flowResults[0] shouldBe expectedResult
    }

    should("execute batch as Flow") {
        // Given
        val batch = client.createBatch()
        val call1 = Commands.web3().clientVersion()
        val call2 = Commands.web3().clientVersion()

        batch.add(call1)
        batch.add(call2)

        val mockResponse1 = RpcCallResponse(call1, "Geth/v1.10.0")
        val mockResponse2 = RpcCallResponse(call2, "Parity/v2.5.0")

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns
            listOf(mockResponse1, mockResponse2)

        // When
        val flowResults = client.executeFlow(batch).toList()

        // Then
        flowResults shouldHaveSize 2
        flowResults[0].value shouldBe "Geth/v1.10.0"
        flowResults[1].value shouldBe "Parity/v2.5.0"
        coVerify(exactly = 1) { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) }
    }

    should("close transport when client is closed") {
        // When
        client.close()

        // Then
        verify(exactly = 1) { mockTransport.close() }
    }

    should("handle empty batch") {
        // Given
        val batch = client.createBatch()

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns emptyList()

        // When
        val responses = client.execute(batch)

        // Then
        responses shouldHaveSize 0
        batch.isEmpty shouldBe true
    }

    should("handle batch with mixed success and error responses") {
        // Given
        val batch = client.createBatch()
        val call1 = Commands.web3().clientVersion()
        val call2 = Commands.web3().clientVersion()

        batch.add(call1)
        batch.add(call2)

        val mockResponse1 = RpcCallResponse(call1, "Geth/v1.10.0")
        val rpcException = RpcException(RpcResponseError.CODE_INVALID_METHOD_PARAMS, "Invalid params")
        val mockResponse2 = RpcCallResponse(call2, rpcException)

        coEvery { mockTransport.execute(any<List<CoroutineBatchItem<*, *>>>()) } returns
            listOf(mockResponse1, mockResponse2)

        // When
        val responses = client.execute(batch)

        // Then
        responses shouldHaveSize 2
        responses[0].isSuccessful shouldBe true
        responses[0].value shouldBe "Geth/v1.10.0"
        responses[1].isError shouldBe true
        responses[1].error.rpcMessage shouldBe "Invalid params"
    }
})
