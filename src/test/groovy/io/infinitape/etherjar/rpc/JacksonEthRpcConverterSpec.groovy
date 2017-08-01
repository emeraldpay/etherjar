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

package io.infinitape.etherjar.rpc

import io.infinitape.etherjar.core.Address
import io.infinitape.etherjar.core.HexData
import io.infinitape.etherjar.core.TransactionId
import io.infinitape.etherjar.core.Wei
import io.infinitape.etherjar.rpc.json.*
import spock.lang.Specification

import java.text.SimpleDateFormat

class JacksonEthRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    def "Parse block 1"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("block/block-1.json")

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
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920000.json")

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
        act.difficulty == 62413376722602
        act.totalDifficulty == 39490964433395682584
        act.size == 978
        act.gasLimit == 4712384
        act.gasUsed == 84000
        act.extraData.toHex() == '0xe4b883e5bda9e7a59ee4bb99e9b1bc'
    }

    def "Parse block 1920000 with transactions"() {
        //http://gastracker.io/block/1920000
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920000-full.json")

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
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("block/block-1920001.json")

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
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("block/block-2050000.json")

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

    def "Parse tx 0x1e694e"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("tx/0x1e694e.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.hash.toHex() == '0x1e694eba2778d34855fa1e01e0765acb31ce75a9abe8667882ffc2c12f4372bc'
        act.blockNumber == 2007232
        act.blockHash.toHex() == '0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946'
        act.from.toHex() == '0x72d61152f6c0c0e57c1fe2b0343a5eac055ff56e'
        act.to == null
        act.gas == 3000000
        act.gasPrice == new Wei(20000000000)
        act.input.bytes.length == 7153
        act.value == new Wei()
        act.nonce == 1
    }

    def "Parse tx 0x847149"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("tx/0x847149.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.hash.toHex() == '0x8471497131d77305416c760ebfed2b6d653c2066c51dac58c6551dcc7dfb3350'
        act.blockNumber == 1720231
        act.blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act.from.toHex() == '0x969837498944ae1dc0dcac2d0c65634c88729b2d'
        act.to.toHex() == '0xc0ee9db1a9e07ca63e4ff0d5fb6f86bf68d47b89'
        act.gas == 4712388
        act.gasPrice == new Wei(27000000000)
        act.input.bytes.length == 4
        act.value.toEther(4) == 102.5666
        act.nonce == 259
    }

    def "Parse tx 0x19442f"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("tx/0x19442f.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.hash.toHex() == '0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'
        act.blockNumber == 1720231
        act.blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act.from.toHex() == '0xed059bc543141c8c93031d545079b3da0233b27f'
        act.to.toHex() == '0x8b3b3b624c3c0397d3da8fd861512393d51dcbac'
        act.gas == 250000
        act.gasPrice == new Wei(24085501424)
        act.input.bytes.length == 4
        act.value == new Wei()
        act.nonce == 15524
        act.signature == null
    }

    def "Parse receipt 0x5929b3"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("receipt/0x5929b3.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.blockHash.toHex() == "0x2c3cfd4c7f2b58859371f5795eaf8524caa6e63145ac7e9df23c8d63aab891ae"
        act.blockNumber == 2177930
        act.contractAddress == null
        act.cumulativeGasUsed == 21000
        act.gasUsed == 21000
        act.transactionHash.toHex() == '0x5929b36be4586c57bd87dfb7ea6be3b985c1f527fa3d69d221604b424aeb4197'
        act.transactionIndex == 0
        act.logs.size() == 0
    }

    def "Parse receipt 0x8883dd"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("receipt/0x8883dd.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionReceiptJson)

        then:
        act.blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
        act.blockNumber == 1709877
        act.contractAddress == null
        act.cumulativeGasUsed == 0x045715
        act.gasUsed == 0x01c6d5
        act.transactionHash.toHex() == '0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46'
        act.transactionIndex == 8
        act.logs.size() == 1
        act.logs[0].address.toHex() == '0x4b8e1ad58657f8b4b036ad12afbcef54d24ac9ba'
        act.logs[0].blockHash.toHex() == "0xb9789dbb3ed309ab88997cc5d3b0cf2c89e35ac41d16b0f11489678da6ad278d"
        act.logs[0].blockNumber == 1709877
        act.logs[0].data.toHex() == "0xbc2ddc901129318b063f3853f46f626f768f8cdaffeec4577eb7febe8e37f29000000000000000000000000000000000000000000000000000000000000000027368613235360000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000202fb928f34a04238701090b138c1d4652d6694a06f9aeea0706e6c474e801673a"
        act.logs[0].logIndex == 0
        act.logs[0].topics*.toHex() == [
                "0x006409c471c01f75fa2c8509f25aae87aa4e1d13b3eda6dcf9cabd084c053265",
                "0x000000000000000000000000e7827ba56a848dff35ccff016f6c0055603ec454"
        ]
        act.logs[0].transactionHash.toHex() == "0x8883dd2f424407e7ecfa1181496fcb5a17e2dc8cd38507582b6af239aa215f46"
        act.logs[0].transactionIndex == 8
    }

    def "Parse unprotected tx 0x5c7851"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("tx/0x5c7851.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.signature != null
        act.signature.chainId == null
        !act.signature.protected
        act.signature.r.toHex() == '0xe28800dc73a56b5c687ad6b38789f5a485a5ead8236b3d1fc04143fbc1b12c40'
        act.signature.s.toHex() == '0x690014487d34d1881461f89edf6662e2efb833f08c5d6913bace4289f2f7736e'
        act.signature.v == 27
        act.signature.publicKey.toHex() == '0x2d4fa87b8e395b5e3f9ff674e0ec109e32d9db99b2b20387cf2f2079c9c041aa2199a67390b8696457020e0aae324cfd1b5e21f86a17683b8afd3660c3ed8c01'
    }

    def "Parse protected tx 0xb35804"() {
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("tx/0xb35804.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.signature != null
        act.signature.chainId.value == 61
        act.signature.protected
        act.signature.r.toHex() == '0x9873696b852c34c3da10dda993f02eb800892e30cb5acacd335d47109e73e51b'
        act.signature.s.toHex() == '0x45adb2148a0f37d30d0a2b4ac3bf4ec0f4d8b938181b45635f02e054ae750759'
        act.signature.v == 157
        act.signature.publicKey.toHex() == '0x7ac35ec4a59a772186573ed9d26787889cde2042110572ddf79a1f9ef2c7bc28b55b35d4fc5497c50e90cdf53b5a6be6174c456d98ba479a86ff4753d652fba3'
    }

    def "Encode basic call data"() {
        def callData = new TransactionCallJson(
                to: Address.from('0x57d90b64a1a57749b0f932f1a3395792e12e7055'),
                data: HexData.from('0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2')
        )
        def req = new RequestJson(
                "eth_call",
                Arrays.asList(callData, 'latest'),
                1
        )

        when:
        def act = jacksonRpcConverter.toJson(req)

        then:
        act == '{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"0x57d90b64a1a57749b0f932f1a3395792e12e7055","data":"0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2"},"latest"],"id":1}'
    }

    def "Encode full call data"() {
        def callData = new TransactionCallJson(
                from: Address.from("0xb7819ff807d9d52a9ce5d713dc7053e8871e077b"),
                to: Address.from('0x57d90b64a1a57749b0f932f1a3395792e12e7055'),
                data: HexData.from('0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2'),
                gas: 100000,
                gasPrice: Wei.fromEther(0.002),
                value: Wei.fromEther(1.5)
        )
        def req = new RequestJson(
                "eth_call",
                Arrays.asList(callData, 'latest'),
                1
        )

        when:
        def act = jacksonRpcConverter.toJson(req)

        then:
        act == '{"jsonrpc":"2.0","method":"eth_call","params":[' +
                '{"from":"0xb7819ff807d9d52a9ce5d713dc7053e8871e077b",' +
                 '"to":"0x57d90b64a1a57749b0f932f1a3395792e12e7055",' +
                 '"gas":"0x0186a0",' +
                 '"gasPrice":"0x071afd498d0000",' +
                 '"value":"0x14d1120d7b160000",' +
                 '"data":"0xa9059cbb00000000000000000000000014dd45d07d1d700579a9b7cfb3a4536890aafdc2"}' +
                ',"latest"],"id":1}'
    }
}
