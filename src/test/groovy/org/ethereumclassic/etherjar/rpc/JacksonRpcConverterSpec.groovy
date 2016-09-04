package org.ethereumclassic.etherjar.rpc

import org.ethereumclassic.etherjar.model.TransactionId
import org.ethereumclassic.etherjar.rpc.json.BlockJson
import org.ethereumclassic.etherjar.rpc.json.TransactionJson
import spock.lang.Specification

import java.text.SimpleDateFormat

/**
 *
 * @since
 * @author Igor Artamonov
 */
class JacksonRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    def "Parse block 1"() {
        setup:
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("block/block-1.json")
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
        act.difficulty.value.longValue() == 17_171_480_576L
        act.totalDifficulty.value.longValue() == 34_351_349_760L
        act.size == 537L
        act.gasLimit.value.longValue() == 5_000L
        act.gasUsed.value.longValue() == 0L
        act.extraData.toHex() == '0x476574682f76312e302e302f6c696e75782f676f312e342e32'
    }

    def "Parse block 1920000"() {
        setup:
        //http://gastracker.io/block/1920000
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920000.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, BlockJson)
        then:
        act instanceof BlockJson<TransactionId>
        act.number == 1920000
        act.hash.toHex() == '0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f'
        sdf.format(act.timestamp) == '2016-07-20 13:20:39 +0000'
        act.transactions.size() == 4
        act.transactions*.toHex() == ["0x6f75b64d9364b71b43cde81a889f95df72e6be004b28477f9083ed0ee471a7f9",
                             "0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611",
                             "0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b",
                             "0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9"]
        act.parentHash.toHex() == '0xa218e2c611f21232d857e3c8cecdcdf1f65f25a4477f98f6f47e4063807f2308'
        act.sha3Uncles.toHex() == '0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347'
        act.miner.toHex() == '0x61c808d82a3ac53231750dadc13c777b59310bd9'
        act.difficulty.value == new BigInteger('62413376722602')
        act.totalDifficulty.value == new BigInteger('39490964433395682584')
        act.size == 978L
        act.gasLimit.value.longValue() == 4712384L
        act.gasUsed.value.longValue() == 84000L
        act.extraData.toHex() == '0xe4b883e5bda9e7a59ee4bb99e9b1bc'
    }

    def "Parse block 1920000 with transactions"() {
        setup:
        //http://gastracker.io/block/1920000
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")
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
        act.transactions[0].gas.value == 21000
        act.transactions[0].gasPrice.value == new BigInteger('04a817c800', 16)
        act.transactions[0].input == null
        act.transactions[0].nonce == 1
        act.transactions[0].transactionIndex == 0
        act.transactions[0].value.value == new BigInteger('8b6cfa3afc058000', 16)

        act.transactions[1].hash.toHex() == '0x50d8156ee48d01b56cb17b6cb2ac8f29e1bf565be0e604b2d8ffb2fb50a0f611'
        act.transactions[1].from.toHex() == '0xee62a6740b3069781fc0ed138e94dcaa89f8eb05'
        act.transactions[1].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[1].gas.value == 21000
        act.transactions[1].gasPrice.value == new BigInteger('04a817c800', 16)
        act.transactions[1].input == null
        act.transactions[1].nonce == 1
        act.transactions[1].transactionIndex == 1
        act.transactions[1].value.value == new BigInteger('0116db7272d6d94000', 16)

        act.transactions[2].hash.toHex() == '0x4677a93807b73a0875d3a292eacb450d0af0d6f0eec6f283f8ad927ec539a17b'
        act.transactions[2].from.toHex() == '0x57ec8ef62a9af59b9fbbc6d7dba05516558f5018'
        act.transactions[2].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[2].gas.value == 21000
        act.transactions[2].gasPrice.value == new BigInteger('04a817c800', 16)
        act.transactions[2].input == null
        act.transactions[2].nonce == 1
        act.transactions[2].transactionIndex == 2
        act.transactions[2].value.value == new BigInteger('14da2c24e0d37014', 16)

        act.transactions[3].hash.toHex() == '0x2a5177e6d6cea40594c7d4b0115dcd087443be3ec2fa81db3c21946a5e51cea9'
        act.transactions[3].from.toHex() == '0x80a103beced8a6854a7a82ac2d48cdab0eb21cc0'
        act.transactions[3].to.toHex() == '0x53d284357ec70ce289d6d64134dfac8e511c8a3d'
        act.transactions[3].gas.value == 21000
        act.transactions[3].gasPrice.value == new BigInteger('04a817c800', 16)
        act.transactions[3].input == null
        act.transactions[3].nonce == 1
        act.transactions[3].transactionIndex == 3
        act.transactions[3].value.value == new BigInteger('0e301365046d5000', 16)
    }

    def "Parse block 1920001"() {
        setup:
        //http://gastracker.io/block/1920001
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920001.json")
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
        act.difficulty.value == new BigInteger('62352426290470')
        act.totalDifficulty.value == new BigInteger('39491026785821973054')
        act.size == 5041L
        act.gasLimit.value.longValue() == 4712388L
        act.gasUsed.value.longValue() == 872136L
        act.extraData.toHex() == '0x65746865726d696e652e6f7267202855533129'
    }

    def "Parse block 2050000"() {
        setup:
        //http://gastracker.io/block/2050000
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("block/block-2050000.json")
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
        act.difficulty.value == new BigInteger('8652774467253')
        act.totalDifficulty.value == new BigInteger('40487114959367091714')
        act.size == 1310L
        act.gasLimit.value.longValue() == 4712388L
        act.gasUsed.value.longValue() == 42000L
        act.extraData.toHex() == '0xd98301040a844765746887676f312e362e328777696e646f7773'
    }

    def "Parse tx 0x1e694e"() {
        setup:
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("tx/0x1e694e.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.hash.toHex() == '0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc'
        act.blockNumber == 2007232
        act.blockHash.toHex() == '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        act.from.toHex() == '0x72d61152f6c0c0e57c1fe2b0343a5eac055ff56e'
        act.to == null
        act.gas.value.longValue() == 3000000
        act.gasPrice.value.longValue() == 20000000000
        act.input.bytes.length == 7153
        act.value.value == BigInteger.ZERO
        act.nonce == 1
    }

    def "Parse tx 0x847149"() {
        setup:
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("tx/0x847149.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.hash.toHex() == '0x8471497131d77305416c760ebfed2b6d653c2066c51dac58c6551dcc7dfb3350'
        act.blockNumber == 1720231
        act.blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act.from.toHex() == '0x969837498944ae1dc0dcac2d0c65634c88729b2d'
        act.to.toHex() == '0xc0ee9db1a9e07ca63e4ff0d5fb6f86bf68d47b89'
        act.gas.value.longValue() == 4712388
        act.gasPrice.value.longValue() == 27000000000
        act.input.bytes.length == 4
        act.value.toString() == '102.5666 ether'
        act.nonce == 259L
    }

    def "Parse tx 0x19442f"() {
        setup:
        InputStream json = JacksonRpcConverterSpec.classLoader.getResourceAsStream("tx/0x19442f.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.hash.toHex() == '0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'
        act.blockNumber == 1720231
        act.blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act.from.toHex() == '0xed059bc543141c8c93031d545079b3da0233b27f'
        act.to.toHex() == '0x8b3b3b624c3c0397d3da8fd861512393d51dcbac'
        act.gas.value.longValue() == 250000
        act.gasPrice.value.longValue() == 24085501424
        act.input.bytes.length == 4
        act.value.value.longValue() == 0
        act.nonce == 15524L
    }
}
