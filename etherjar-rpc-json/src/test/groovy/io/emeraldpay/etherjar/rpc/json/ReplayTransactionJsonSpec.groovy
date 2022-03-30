package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class ReplayTransactionJsonSpec extends Specification {

    ObjectMapper objectMapper = new ObjectMapper()

    def "stateDiff only - 0x2fb5f1"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("replayTx/0x2fb5f1.json")

        when:
        def act = objectMapper.readValue(json, ReplayTransactionJson)

        then:
        act.transactionHash == TransactionId.from("0x2fb5f1389706508e88f54907a87c9e9e54e68a587d67d40b04d4bc97e6931d74")
        act.output != null
        act.output.isEmpty()
        act.trace == null || act.trace.isEmpty()
        act.vmTrace == null
        act.stateDiff != null
        with(act.stateDiff) {
            it.getChanges().size() == 15
            with(it.getDiff(Address.from("0x0000000000007f150bd6f54c40a34d7c3d5e9f56"))) {
                it != null

                it.balance != null
                it.balance.type == StateDiffJson.ChangeType.REPLACE
                it.balance.before == Wei.from("0x5be273c9585aa1ce8c")
                it.balance.after == Wei.from("0x5be36bd43b0eddaf88")

                it.code != null
                it.code.type == StateDiffJson.ChangeType.NOTHING

                it.nonce != null
                it.nonce.type == StateDiffJson.ChangeType.NOTHING

                it.storage != null
                with(it.storage[Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000032")]) {
                    it != null
                    it.before == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000c4be4")
                    it.after == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000c4beb")
                }
            }

            with(it.getDiff(Address.from("0x1ea335202509e40a29c2cd1e40f15113565aedba"))) {
                it != null
                it.balance != null
                it.balance.type == StateDiffJson.ChangeType.REMOVE
                it.balance.before == Wei.ZERO

                it.code != null
                it.code.type == StateDiffJson.ChangeType.REMOVE
                it.code.before == HexData.from("0x6d7f150bd6f54c40a34d7c3d5e9f563318585733ff")

                it.nonce != null
                it.nonce.type == StateDiffJson.ChangeType.REMOVE
                it.nonce.before == 1

                it.storage == null || it.storage.isEmpty()
            }
        }
    }

    def "stateDiff only - 0xe8c9d2"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("replayTx/0xe8c9d2.json")

        when:
        def act = objectMapper.readValue(json, ReplayTransactionJson)

        then:
        act.transactionHash == TransactionId.from("0xe8c9d2f5868a697cc42a0eea8ff0413fe36ac7c1379bdfe1b5b2a26384e0349a")
        act.output != null
        act.output.isEmpty()
        act.trace == null || act.trace.isEmpty()
        act.vmTrace == null
        act.stateDiff != null
        with(act.stateDiff) {
            it.getChanges().size() == 3

            with(it.getDiff(Address.from("0x18f3414db9e334ec8ab1daf6e13ddd90fe91f135"))) {
                it.balance.type == StateDiffJson.ChangeType.REPLACE
                it.balance.before == Wei.from("0xdec70b06a7e6000")
                it.balance.after == Wei.from("0x0")

                it.code.type == StateDiffJson.ChangeType.NOTHING

                it.nonce.type == StateDiffJson.ChangeType.REPLACE
                it.nonce.before == 0
                it.nonce.after == 1

                it.storage == null || it.storage.isEmpty()
            }

            with(it.getDiff(Address.from("0x98d9522a22b7c4fd86c63c4035946494afa2434e"))) {
                it.balance.type == StateDiffJson.ChangeType.REPLACE
                it.balance.before == Wei.from("0x7e2d0c7778e7d400")
                it.balance.after == Wei.from("0x8c0d078ed22c0400")

                it.code.type == StateDiffJson.ChangeType.NOTHING
                it.nonce.type == StateDiffJson.ChangeType.NOTHING
                it.storage == null || it.storage.isEmpty()
            }

            with(it.getDiff(Address.from("0xb3b7874f13387d44a3398d298b075b7a3505d8d4"))) {
                it.balance.type == StateDiffJson.ChangeType.REPLACE
                it.balance.before == Wei.from("0x322107e896eec45724b")
                it.balance.after == Wei.from("0x322108aff07fd7fa24b")

                it.code.type == StateDiffJson.ChangeType.NOTHING
                it.nonce.type == StateDiffJson.ChangeType.NOTHING
                it.storage == null || it.storage.isEmpty()
            }
        }
    }

}
