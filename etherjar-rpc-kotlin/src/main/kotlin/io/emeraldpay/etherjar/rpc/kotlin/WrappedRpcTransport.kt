package io.emeraldpay.etherjar.rpc.kotlin

import io.emeraldpay.etherjar.rpc.AbstractRpcTransport
import io.emeraldpay.etherjar.rpc.DefaultBatch
import io.emeraldpay.etherjar.rpc.RpcCallResponse
import kotlinx.coroutines.future.await


/**
 * Wraps a Java Futures based [AbstractRpcTransport] to provide a [CoroutineRpcTransport].
 * It just calls `.await` on the Java Future returned by the delegated transport.
 */
class WrappedRpcTransport(
    private val delegate: AbstractRpcTransport,
): CoroutineRpcTransport {

    override suspend fun execute(items: List<CoroutineBatchItem<*, *>>): List<RpcCallResponse<*, *>> {
        val batch = DefaultBatch()
        items.forEach {
            batch.add(it.call)
        }
        val result = delegate.execute(batch.items).await()
        return result.toList()
    }

    override fun close() {
        delegate.close()
    }

}
