package io.emeraldpay.etherjar.rpc.http

import io.emeraldpay.etherjar.rpc.Commands
import io.emeraldpay.etherjar.rpc.DefaultRpcClient
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@IgnoreIf({ System.getenv("ETHERJAR_TEST_ENABLED") != "true"})
class IntegrationSpec extends Specification {

    String rpc = System.getenv("ETHERJAR_TEST_RPC")

    def "Parse all data for latest 10 blocks"() {
        setup:
        def client = new DefaultRpcClient(
            HttpRpcTransport.newBuilder()
                .connectTo(rpc)
                .build()
        )

        when:
        long height = client.execute(Commands.eth().blockNumber).get(5, TimeUnit.SECONDS)
        long startHeight = height - 10
        int blocks = 0
        int transactions = 0
        int receipts = 0
        (startHeight..height).forEach {
            def block = client.execute(Commands.eth().getBlock(it)).get(15, TimeUnit.SECONDS)
            println("Process block ${block.number} / ${block.hash}")
            blocks++
            block.transactions.forEach { txref ->
                try {
                    def tx = client.execute(Commands.eth().getTransaction(txref.getHash())).get(30, TimeUnit.SECONDS)
                    transactions++
                    def receipt = client.execute(Commands.eth().getTransactionReceipt(tx.hash)).get(30, TimeUnit.SECONDS)
                    receipts++
                } catch(Throwable t) {
                    println("Failed to process ${txref.hash} / ${t.class} ${t.message ?: ""}")
                    throw t
                }
            }
        }

        then:
        blocks > 0
        transactions > 0
        transactions == receipts
    }
}
