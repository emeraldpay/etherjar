package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.BlockHash
import io.emeraldpay.etherjar.domain.Bloom
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.hex.HexQuantity
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import io.emeraldpay.etherjar.rpc.RpcCall
import spock.lang.Specification

import java.time.Instant

class BlockSimulatedJsonSpec extends Specification {
    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = jacksonRpcConverter.getObjectMapper()

    BlockSimulatedJson block1 = new BlockSimulatedJson().tap {
        baseFeePerGas = new Wei(9)
        blobGasUsed = 0
        calls = [
            new BlockSimulatedJson.CallResultLog().tap {
                returnData = null
                logs = [
                    new TransactionLogJson().tap {
                        address = Address.from("0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                        topics = [
                            Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                            Hex32.from("0x000000000000000000000000c000000000000000000000000000000000000000"),
                            Hex32.from("0x000000000000000000000000c000000000000000000000000000000000000001")
                        ]
                        data = HexData.from("0x0000000000000000000000000000000000000000000000000000000000000001")
                        blockNumber = 0x13d2747
                        transactionHash = TransactionId.from("0xe7217784e0c3f7b35d39303b1165046e9b7e8af9b9cf80d5d5f96c3163de8f51")
                        transactionIndex = 0x0
                        blockHash = BlockHash.from("0x5e28f54a56dc9df973a058cd54b3eeef8c67a1a613cb5db1df8a0a434c931d56")
                        logIndex = 0x0
                        removed = false
                    }
                ]
                gasUsed = HexQuantity.from(0x5208)
                status = 0x1
            },
            new BlockSimulatedJson.CallResultLog().tap {
                returnData = null
                logs = [
                    new TransactionLogJson().tap {
                        address = Address.from("0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                        topics = [
                            Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                            Hex32.from("0x000000000000000000000000c000000000000000000000000000000000000000"),
                            Hex32.from("0x000000000000000000000000c000000000000000000000000000000000000002")
                        ]
                        data = HexData.from("0x0000000000000000000000000000000000000000000000000000000000000001")
                        blockNumber = 0x13d2747
                        transactionHash = TransactionId.from("0xf0182201606ec03701ba3a07d965fabdb4b7d06b424f226ea7ec3581802fc6fa")
                        transactionIndex = 0x1
                        blockHash = BlockHash.from("0x5e28f54a56dc9df973a058cd54b3eeef8c67a1a613cb5db1df8a0a434c931d56")
                        logIndex = 0x1
                        removed = false
                    }
                ]
                gasUsed = HexQuantity.from(0x5208)
                status = 0x1
            }
        ]
        difficulty = new BigInteger("0")
        excessBlobGas = 0
        extraData = null
        gasLimit = 0x1c9c380
        gasUsed = 0xa410
        hash = BlockHash.from("0x5e28f54a56dc9df973a058cd54b3eeef8c67a1a613cb5db1df8a0a434c931d56")
        logsBloom = Bloom.from("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
        miner = Address.from("0x4838b106fce9647bdf1e7877bf73ce8b0bad5f97")
        mixHash = Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000000")
        nonce = HexData.from("0x0000000000000000")
        number = 0x13d2747
        parentBeaconBlockRoot = Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000000")
        parentHash = BlockHash.from("0xd24222b93a05a066cf79dc20e333f5aa6bb06d36eb50eb2b6b0b744b937e7975")
        receiptsRoot = Hex32.from("0x75308898d571eafb5cd8cde8278bf5b3d13c5f6ec074926de3bb895b519264e1")
        sha3Uncles = Hex32.from("0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347")
        size = 0x298
        stateRoot = Hex32.from("0xbb0740745211507e2a2a6cdb627dfa171ef5050ad2a01e5401c2e3df4be5b919")
        timestamp = Instant.ofEpochSecond(0x66ec2853)
        totalDifficulty = new BigInteger("c70d815d562d3cfa955", 16)
        transactions = [
            new TransactionRefJson(TransactionId.from("0xe7217784e0c3f7b35d39303b1165046e9b7e8af9b9cf80d5d5f96c3163de8f51")),
            new TransactionRefJson(TransactionId.from("0xf0182201606ec03701ba3a07d965fabdb4b7d06b424f226ea7ec3581802fc6fa"))
        ]
        transactionsRoot = Hex32.from("0x9bdb74f3ce41f5893a02a631e904ae0d21ae8c4e416786d8dbd9cb5c54f1dc0f")
        uncles = []
        withdrawals = []
        withdrawalsRoot = Hex32.from("0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421")
    }

    def "Encodes request"() {
        setup:
        JsonNode expected = objectMapper.readTree(BlockSimulatedJsonSpec.classLoader.getResourceAsStream("simulate/simulated-1.json"))

        when:
        block1.calls[0].returnData = HexData.empty()
        block1.calls[1].returnData = HexData.empty()
        block1.extraData = HexData.empty()
        JsonNode act = objectMapper.readTree(objectMapper.writeValueAsString(block1))

        then:
        act == expected
    }

    def "Decodes request"() {
        setup:
        String json = BlockSimulatedJsonSpec.classLoader.getResourceAsStream("simulate/simulated-1.json").text

        when:
        def act = objectMapper.readValue(json, BlockSimulatedJson)

        then:
        act.calls.size() == block1.calls.size()
        act.calls.size() == 2
        act.calls[0] == block1.calls[0]
        act.calls[1] == block1.calls[1]
        act == block1
    }

    def "Decodes full JSON RPC response"() {
        setup:
        String json = BlockSimulatedJsonSpec.classLoader.getResourceAsStream("simulate/simulated-1.json").text
        String jsonRpc = """
            {
                "jsonrpc": "2.0",
                "id": 1,
                "result": [$json]
            }
        """
        // as in EthCommands
        def rpcCall = RpcCall.create("eth_simulateV1", BlockSimulatedJson.class, "foor", "latest").asArray();

        when:
        def act = jacksonRpcConverter.fromJson(new ByteArrayInputStream(jsonRpc.bytes), rpcCall.jsonType)

        then:
        act instanceof BlockSimulatedJson[]
        with (act as BlockSimulatedJson[]) {
            it[0].calls.size() == block1.calls.size()
            it[0].calls.size() == 2
            it[0].calls[0] == block1.calls[0]
            it[0].calls[1] == block1.calls[1]
            it[0] == block1
        }
    }
}
