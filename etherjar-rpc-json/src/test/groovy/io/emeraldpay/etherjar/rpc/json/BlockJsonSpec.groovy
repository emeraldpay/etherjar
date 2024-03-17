package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.BlockHash
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BlockJsonSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = jacksonRpcConverter.getObjectMapper()
    DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z").withZone(ZoneId.of('UTC'))

    def "Parse block 1"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 1
        act.hash.toHex() == '0x88e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6'
        sdf.format(act.timestamp) == '2015-07-30 15:26:28 +0000'
        act.transactions.size() == 0
        act.parentHash.toHex() == '0xd4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3'
        act.sha3Uncles.toHex() == '0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347'
        act.miner.toHex() == '0x05a56e2d52c817161883f50c441c3228cfe54d9f'
        act.difficulty == 17_171_480_576
        act.totalDifficulty == 34_351_349_760
        act.size == 537
        act.gasLimit == 5_000
        act.gasUsed == 0
        act.extraData.toHex() == '0x476574682f76312e302e302f6c696e75782f676f312e342e32'
    }

    def "Parse block 1920000"() {
        //http://gastracker.io/block/1920000
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson<TransactionRefJson>
        act.number == 1920000
        act.hash.toHex() == '0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f'
        sdf.format(act.timestamp) == '2016-07-20 13:20:39 +0000'
        act.transactions.size() == 4
        act.transactions.collect { it.hash.toHex() } == ["0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9",
                                                         "0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611",
                                                         "0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b",
                                                         "0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9"]
        act.parentHash.toHex() == '0xa218e2c611f21232d857e3c8cecdcdf1f65f25a4477f98f6f47e4063807f2308'
        act.sha3Uncles.toHex() == '0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347'
        act.miner.toHex() == '0x61c808d82a3ac53231750dadc13c777b59310bd9'
        act.difficulty == 62413376722602
        act.totalDifficulty == 39490964433395682584
        act.size == 978
        act.gasLimit == 4712384
        act.gasUsed == 84000
        act.extraData.toHex() == '0xe4b883e5bda9e7a59ee4bb99e9b1bc'
    }

    def "Parse block 1920000 with transactions"() {
        //http://gastracker.io/block/1920000
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson<TransactionJson>
        act.number == 1920000
        act.hash.toHex() == '0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f'

        act.transactions.size() == 4
        act.transactions*.blockHash*.toHex().unique() == ['0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f']
        act.transactions*.blockNumber.unique() == [1920000]

        act.transactions[0] instanceof TransactionJson
        act.transactions[0].hash.toHex() == '0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9'
        act.transactions[0].from.toHex() == '0x6ebeb2af2e734fbba2b58c5b922628af442527ce'
        act.transactions[0].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[0].gas == 21000
        act.transactions[0].gasPrice == new Wei(0x04a817c800)
        act.transactions[0].input == null
        act.transactions[0].nonce == 1
        act.transactions[0].transactionIndex == 0
        act.transactions[0].value == new Wei(0x8b6cfa3afc058000)

        act.transactions[1].hash.toHex() == '0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611'
        act.transactions[1].from.toHex() == '0xee62a6740b3069781fc0ed138e94dcaa89f8eb05'
        act.transactions[1].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[1].gas == 21000
        act.transactions[1].gasPrice == new Wei(0x04a817c800)
        act.transactions[1].input == null
        act.transactions[1].nonce == 1
        act.transactions[1].transactionIndex == 1
        act.transactions[1].value == new Wei(0x0116db7272d6d94000)

        act.transactions[2].hash.toHex() == '0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b'
        act.transactions[2].from.toHex() == '0x57ec8ef62a9af59b9fbbc6d7dba05516558f5018'
        act.transactions[2].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[2].gas == 21000
        act.transactions[2].gasPrice == new Wei(0x04a817c800)
        act.transactions[2].input == null
        act.transactions[2].nonce == 1
        act.transactions[2].transactionIndex == 2
        act.transactions[2].value == new Wei(0x14da2c24e0d37014)

        act.transactions[3].hash.toHex() == '0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9'
        act.transactions[3].from.toHex() == '0x80a103beced8a6854a7a82ac2d48cdab0eb21cc0'
        act.transactions[3].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[3].gas == 21000
        act.transactions[3].gasPrice == new Wei(0x04a817c800)
        act.transactions[3].input == null
        act.transactions[3].nonce == 1
        act.transactions[3].transactionIndex == 3
        act.transactions[3].value == new Wei(0x0e301365046d5000)
    }

    def "Parse block 1920001"() {
        //http://gastracker.io/block/1920001
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920001.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 1920001
        act.hash.toHex() == '0xab7668dfd3bedcf9da505d69306e8fd12ad78116429cf8880a9942c6f0605b60'
        sdf.format(act.timestamp) == '2016-07-20 13:21:17 +0000'
        act.transactions.size() == 41
        act.transactions.unique().size() == 41
        act.parentHash.toHex() == '0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f'
        act.sha3Uncles.toHex() == '0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347'
        act.miner.toHex() == '0xea674fdde714fd979de3edf0f56aa9716b898ec8'
        act.difficulty == 62352426290470
        act.totalDifficulty == 39491026785821973054
        act.size == 5041
        act.gasLimit == 4712388
        act.gasUsed == 872136
        act.extraData.toHex() == '0x65746865726d696e652e6f7267202855533129'
    }

    def "Parse block 2050000"() {
        //http://gastracker.io/block/2050000
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-2050000.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 2050000
        act.hash.toHex() == '0xdb87647a46c2418c22250ecb23a3861bd6a223632d85b5c5af12303a04387339'
        sdf.format(act.timestamp) == '2016-08-11 13:12:39 +0000'
        act.transactions.size() == 2
        act.parentHash.toHex() == '0xecdf74d16a971ba6618a8f28bfac28daaca7612894993d1050a09214b66dac33'
        act.sha3Uncles.toHex() == '0xfecf4c589cab09da47205aa2587e7c8d76f521be749672ab0b6e8fb4f6f97630'
        act.uncles*.toHex() == [
            "0x56f250f7de13d3fcb0cc4af795c14a303eaba5aaea981c646c2d7d139c405783"
        ]
        act.miner.toHex() == '0xbe57c30111a068b9aac079d0dcda1a9e4ad51881'
        act.difficulty == 8652774467253
        act.totalDifficulty == 40487114959367091714
        act.size == 1310
        act.gasLimit == 4712388
        act.gasUsed == 42000
        act.extraData.toHex() == '0xd98301040a844765746887676f312e362e328777696e646f7773'
    }

    def "Parse block 17172922"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-17172922.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 17172922
        act.size == 0x2bb75
        act.transactionsRoot.toHex() == "0xf9959b8e5dd137dcc1d1ddba57dba0d3efb8d5e8de0ed03de1414bd627d7bac7"
        act.stateRoot.toHex() == "0xe2aa2262e478ccb0028ed5a3ad34f54bafcb0fbd8570ae426f4697a8dcdc225d"
        act.withdrawalsRoot.toHex() == "0x3490d74ee78919d255a0676e1c0575c163268860a05cbc36c57a274be763aeae"
        act.withdrawals.size() == 16
        with(act.withdrawals[0]) {
            it.index == 0x21b3d0
            it.validatorIndex == 0x835d8
            it.address.toHex() == "0xb9d7934878b5fb9610b3fe8a5e441e8fad7e293f"
            it.amount.toHex() == "0xbdb6f3"
        }
    }

    def "Re-parse serialized block 17172922"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-17172922.json")

        when:
        def original = jacksonRpcConverter.fromJson(json, BlockJson)
        def serialized = objectMapper.writeValueAsString(original)
        def act = objectMapper.readValue(serialized, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 17172922
        act == original
    }

    def "Serialized block 17172922"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-17172922.json")

        when:
        def original = jacksonRpcConverter.fromJson(json, BlockJson)
        def serialized = objectMapper.writeValueAsString(original)

        then:
        serialized.contains('"gasUsed":"0xd42912"')
        serialized.contains('"difficulty":"0x0"')
        serialized.contains('"transactions":["0x127cf0f46f247d48a169afc1604ea5c1d92b4e47b58956c0286ba25dafd7b4d8","0x4fcdc4523cfafa63eb22ae2fb3d468b6b96c036ab0cbe1fd754f01d5128a33a4"')
        serialized.contains('"size":"0x2bb75"')
        serialized.contains('"timestamp":"0x6450f9ef"')
        serialized.contains('"logsBloom":"0x1cab412361e57d22b01114529c')
    }

    def "Parse full block 17172922"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-17172922-full.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 17172922
        act.size == 0x2bb75
        act.transactionsRoot.toHex() == "0xf9959b8e5dd137dcc1d1ddba57dba0d3efb8d5e8de0ed03de1414bd627d7bac7"
        act.stateRoot.toHex() == "0xe2aa2262e478ccb0028ed5a3ad34f54bafcb0fbd8570ae426f4697a8dcdc225d"
        act.withdrawalsRoot.toHex() == "0x3490d74ee78919d255a0676e1c0575c163268860a05cbc36c57a274be763aeae"
        act.withdrawals.size() == 16
        with(act.withdrawals[0]) {
            it.index == 0x21b3d0
            it.validatorIndex == 0x835d8
            it.address.toHex() == "0xb9d7934878b5fb9610b3fe8a5e441e8fad7e293f"
            it.amount.toHex() == "0xbdb6f3"
        }
        act.transactions.size() == 148
        with(act.transactions[0]) {
            it instanceof TransactionJson
            with(it as TransactionJson) {
                it.blockHash.toHex() == "0xf44a5b9e759bbe35c49644b6ef1cb78b4c2f2657164452b791602be0d9509b43"
                it.hash.toHex() == "0x127cf0f46f247d48a169afc1604ea5c1d92b4e47b58956c0286ba25dafd7b4d8"
            }
        }
    }

    def "Re-parse serialized full block 17172922"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-17172922-full.json")

        when:
        def original = jacksonRpcConverter.fromJson(json, BlockJson)
        def serialized = objectMapper.writeValueAsString(original)
        def act = objectMapper.readValue(serialized, BlockJson)

        then:
        act instanceof BlockJson
        act.number == 17172922
        act == original
    }

    def "Makes an identical copy of an empty block"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Makes an identical copy of a block with tx hash"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Makes an identical copy of a block with full tx"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.copy()
        then:
        orig == copy
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Erase tx details when exists"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        def origErased = orig.copy()
        origErased.transactions = []
        def copyErased = copy.copy()
        copyErased.transactions = []
        then:
        copy.hash == BlockHash.from("0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f")
        copy.transactions == [
            new TransactionRefJson(TransactionId.from("0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9")),
            new TransactionRefJson(TransactionId.from("0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611")),
            new TransactionRefJson(TransactionId.from("0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b")),
            new TransactionRefJson(TransactionId.from("0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9")),
        ]
        origErased == copyErased
        System.identityHashCode(orig) != System.identityHashCode(copy)
    }

    def "Returns same without tx if contains only references"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1920000.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        then:
        copy.hash == BlockHash.from("0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f")
        copy.transactions == [
            new TransactionRefJson(TransactionId.from("0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9")),
            new TransactionRefJson(TransactionId.from("0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611")),
            new TransactionRefJson(TransactionId.from("0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b")),
            new TransactionRefJson(TransactionId.from("0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9")),
        ]
        orig == copy
    }

    def "Returns same without tx if empty"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-1.json")
        when:
        def orig = jacksonRpcConverter.fromJson(json, BlockJson)
        def copy = orig.withoutTransactionDetails()
        then:
        copy.hash == BlockHash.from("0x88e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6")
        copy.transactions == []
        orig == copy
    }

    def "Reads logsBloom"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-11388816.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)
        then:
        act.logsBloom.toHex() == "0x562568502e1a684125a8e28fc6e8dd81a11593a24c818b6eb159afe20082613b4cbd49456602d909fe2e5957029b87304e308817af4b9205c53122a2e07fa6024804ce70021487b86a71a7cae008f0f81698046882e18e4019c4c34ed9405af9739001448735b59b451c2271d444983f8512c411c68827c2c0a6103741b114c812bb2337c16e4a0585594442e06124bbc130d9074dc8375840d771f214d10f02924127c24400a122248b83ac4f51241ba6c2b525a4820a18dc0e323a372c412c014a808e4006148138a6903010988acc828306821c0b40b900fb211e01f1ec6d7cbcb434af400d28216820613b9180b93785a161a96675d036708a0266a3815a"
    }

    def "Reads baseFee"() {
        setup:
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-13104052.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)
        then:
        act.baseFeePerGas.getAmount().toString() == "94932363924"
    }

    def "Parse uncle"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/uncle-e7b70b42.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)

        then:
        act instanceof BlockJson
        act.hash.toHex() == '0xa0437cab40119e21bc92d1ce5be52c89c64fa3b914489d51b4fe209a33ed31a5'
        act.transactions == null
    }

    def "Parse with blobs info"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-19443068.json")

        when:
        def act = jacksonRpcConverter.objectMapper.readValue(json, BlockJson)

        then:
        act instanceof BlockJson
        act.hash.toHex() == '0x4d9557d1933a02fb780063b4c35c0cd9fb590014f851f50641befa034dbdc7e4'
        act.blobGasUsed == 0xc0000
        act.excessBlobGas == 0x60000
        act.parentBeaconBlockRoot.toHex() == "0x7c132a0e0eef72627f2af7c994e8c3cf27c5e6d296473124fb270023db9cf81d"
    }

    def "Parse with blobs info and transactions"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("block/block-19443105-full.json")

        when:
        BlockJson<TransactionJson> act = jacksonRpcConverter.objectMapper.readValue(json, BlockJson)

        then:
        act instanceof BlockJson
        act.hash.toHex() == '0x52806b8fab72317f97413323cc739de2cd2afc5915298a24beed5d184d798d4b'
        act.blobGasUsed == 0x60000
        act.excessBlobGas == 0x0
        with((TransactionJson)act.transactions.find { it.hash == TransactionId.from("0x109332e227bb505e5731fcbe231dc8d9c0136c14300cd34e40097abf72c51105") }) {
            maxFeePerBlobGas == Wei.fromHex("0x22ecb25c00")
            blobVersionedHashes == [
                Hex32.from("0x01ebcede241d2473b6b1a6fa08f0a281858bc8901f742a39ff74d371e2f2664f")
            ]
        }
    }
}
