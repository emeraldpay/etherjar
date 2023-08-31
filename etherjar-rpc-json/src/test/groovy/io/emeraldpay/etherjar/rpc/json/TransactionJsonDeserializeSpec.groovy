package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.rpc.EtherjarModule
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class TransactionJsonDeserializeSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = new ObjectMapper().tap {
        it.registerModule(new EtherjarModule())
    }

    def "Parse unprotected tx 0x5c7851"() {
        InputStream json = TransactionJsonDeserializeSpec.classLoader.getResourceAsStream("tx/0x5c7851.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.hash.toString() == "0x5c7851f4b2dc93860ed5a6624a422d4f17975d41c68667b64453de3ffaa8af49"
        act.signature != null
        act.signature.chainId == null
        !act.signature.protected
        act.signature.r.toHex() == '0xe28800dc73a56b5c687ad6b38789f5a485a5ead8236b3d1fc04143fbc1b12c40'
        act.signature.s.toHex() == '0x690014487d34d1881461f89edf6662e2efb833f08c5d6913bace4289f2f7736e'
        act.signature.v == 27
    }

    def "Parse unprotected tx 0x5c7851 as resulting json"() {
        InputStream json = TransactionJsonDeserializeSpec.classLoader.getResourceAsStream("tx/0x5c7851-result.json")

        when:
        def act = objectMapper.readValue(json, TransactionJson)

        then:
        act.hash.toString() == "0x5c7851f4b2dc93860ed5a6624a422d4f17975d41c68667b64453de3ffaa8af49"
        act.signature != null
        act.signature.chainId == null
        !act.signature.protected
        act.signature.r.toHex() == '0xe28800dc73a56b5c687ad6b38789f5a485a5ead8236b3d1fc04143fbc1b12c40'
        act.signature.s.toHex() == '0x690014487d34d1881461f89edf6662e2efb833f08c5d6913bace4289f2f7736e'
        act.signature.v == 27
    }

    def "Parse protected tx 0xb35804"() {
        InputStream json = TransactionJsonDeserializeSpec.classLoader.getResourceAsStream("tx/0xb35804.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.signature != null
        act.signature.protected
        act.signature.r.toHex() == '0x9873696b852c34c3da10dda993f02eb800892e30cb5acacd335d47109e73e51b'
        act.signature.s.toHex() == '0x45adb2148a0f37d30d0a2b4ac3bf4ec0f4d8b938181b45635f02e054ae750759'
        act.signature.v == 157
    }

    def "Parse tx 0x1e694e"() {
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x1e694e.json")

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
        act.value == Wei.ZERO
        act.nonce == 1
    }

    def "Parse tx 0x847149"() {
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x847149.json")

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
        act.value.toEthers(4) == 102.5666
        act.nonce == 259
    }

    def "Parse tx 0x19442f without v"() {
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x19442f.json")

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
        act.value == Wei.ZERO
        act.nonce == 15524
        act.signature == null
    }

    def "Parse tx with large V"() {
        InputStream json = this.class.classLoader.getResourceAsStream("tx/matic-0xaf4008.json")

        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.hash.toHex() == '0x6fe439fa7b6f3b4883aa48f85018405e3ae61de3ad72aec614db69bebbd522b5'
        act.signature.v == 0x135
    }

    def "Reads type"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x1ccad3.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.type == 2
    }

    def "Reads gas price for EIP-1559"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x1ccad3.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.gasPrice.getAmount().toString() == "96546783892"
        act.maxFeePerGas.getAmount().toString() == "122763633213"
        act.maxPriorityFeePerGas.getAmount().toString() == "1614419968"
    }

    def "Reads chainId"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x1ccad3.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.chainId == 1
    }

    def "Parse tx 0xb8e7e1 with access list"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0xb8e7e1.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.type == 2
        act.accessList != null
        act.accessList.size() == 4
        with(act.accessList[0]) {
            address.toHex() == "0xb270176ba6075196df88b855c3ec7776871fdb33"
            storageKeys.collect { it.toHex() } == [
                "0x000000000000000000000000000000000000000000000000000000000000000c",
                "0x0000000000000000000000000000000000000000000000000000000000000008",
                "0x0000000000000000000000000000000000000000000000000000000000000006",
                "0x0000000000000000000000000000000000000000000000000000000000000007",
                "0x0000000000000000000000000000000000000000000000000000000000000009",
                "0x000000000000000000000000000000000000000000000000000000000000000a"
            ]
        }
        with(act.accessList[1]) {
            address.toHex() == "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
            storageKeys.collect { it.toHex() } == [
                "0x23238df7996946dad2cb8b2e55d840b556de07df53ede7b83e4570631dda2caf",
                "0x951b3e3636ef9f551e87ecee3510715c527f06774fb11d3915f63d97193ebfdd",
                "0x6efe9ae5914dd7c0ece33ffec9f1fb19a1782fdc69b2ce12c0780d513613925b"
            ]
        }
        with(act.accessList[2]) {
            address.toHex() == "0x0c722a487876989af8a05fffb6e32e45cc23fb3a"
            storageKeys.collect { it.toHex() } == [
                "0x000000000000000000000000000000000000000000000000000000000000000c",
                "0x0000000000000000000000000000000000000000000000000000000000000008",
                "0x0000000000000000000000000000000000000000000000000000000000000006",
                "0x0000000000000000000000000000000000000000000000000000000000000007",
                "0x0000000000000000000000000000000000000000000000000000000000000009",
                "0x000000000000000000000000000000000000000000000000000000000000000a"
            ]
        }
        with(act.accessList[3]) {
            address.toHex() == "0x77777feddddffc19ff86db637967013e6c6a116c"
            storageKeys.collect { it.toHex() } == [
                "0x0000000000000000000000000000000000000000000000000000000000000008",
                "0x6da271455a6c2533df15ea7c52a0751ad98bfdff3c4ae4a185a640749b5f82dd",
                "0x9e4f5f97e03aa4efae12a106e7836019d87ddeb1f4c0a5c8013e28b7ec1f0f31"
            ]
        }
    }
    def "Parse tx 0x408dc2 with access list"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0x408dc2.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)

        then:
        act.accessList != null
        act.accessList.size() == 5
    }

    def "Reads tx with v=0"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0xd67a48.json")
        when:
        def act = jacksonRpcConverter.fromJson(json, TransactionJson)
        then:
        act.signature.v == 0
    }
}
