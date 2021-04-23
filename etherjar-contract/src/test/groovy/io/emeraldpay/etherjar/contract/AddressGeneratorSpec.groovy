package io.emeraldpay.etherjar.contract

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class AddressGeneratorSpec extends Specification {

    def "CREATE at tx"() {
        setup:
        def generator = new AddressGenerator()
        when:
        // tx 0xe9ebfecc2fa10100db51a4408d18193b3ac504584b51a4e55bdef1318f0a30f9
        def act = generator.create(Address.from("0x793ea9692ada1900fbd0b80fffec6e431fe8b391"), 0)
        then:
        act == Address.from("0xbb9bc244d798123fde783fcc1c72d3bb8c189413")
    }

    def "CREATE from contract"() {
        setup:
        def generator = new AddressGenerator()
        when:
        // tx 0x4fc1580e7f66c58b7c26881cce0aab9c3509afe6e507527f30566fbf8039bcd0
        def act = generator.create(Address.from("0x9c33eacc2f50e39940d3afaf2c7b8246b681a374"), 3)
        then:
        act == Address.from("0x7a250d5630b4cf539739df2c5dacb4c659f2488d")
    }

    def "CREATE large nonce"() {
        setup:
        def generator = new AddressGenerator()
        when:
        // tx 0xd76d9be88c3fabdd96b07056517006b352e02f2ea405897cf61b31870d6e1b83
        def act = generator.create(Address.from("0x0536806df512d6cdde913cf95c9886f65b1d3462"), 35991)
        then:
        act == Address.from("0x9e8e587b71c38b2dec6d7d7c906c64864b75e005")
    }

    def "CREATE2 spec examples"() {
        setup:
        def generator = new AddressGenerator()
        expect:
        generator.create2(
            Address.from(from), Hex32.from(salt), HexData.from(initCode)
        ) == Address.from(exp)
        where:
        exp                                          | from                                         | salt                                                                 | initCode
        "0x4D1A2e2bB4F88F0250f26Ffff098B0b30B26BF38" | "0x0000000000000000000000000000000000000000" | "0x0000000000000000000000000000000000000000000000000000000000000000" | "0x00"
        "0xB928f69Bb1D91Cd65274e3c79d8986362984fDA3" | "0xdeadbeef00000000000000000000000000000000" | "0x0000000000000000000000000000000000000000000000000000000000000000" | "0x00"
        "0xD04116cDd17beBE565EB2422F2497E06cC1C9833" | "0xdeadbeef00000000000000000000000000000000" | "0x000000000000000000000000feed000000000000000000000000000000000000" | "0x00"
        "0x70f2b2914A2a4b783FaEFb75f459A580616Fcb5e" | "0x0000000000000000000000000000000000000000" | "0x0000000000000000000000000000000000000000000000000000000000000000" | "0xdeadbeef"
        "0x60f3f640a8508fC6a86d45DF051962668E1e8AC7" | "0x00000000000000000000000000000000deadbeef" | "0x00000000000000000000000000000000000000000000000000000000cafebabe" | "0xdeadbeef"
        "0x1d8bfDC5D46DC4f61D6b6115972536eBE6A8854C" | "0x00000000000000000000000000000000deadbeef" | "0x00000000000000000000000000000000000000000000000000000000cafebabe" | "0xdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        "0xE33C0C7F7df4809055C3ebA6c09CFe4BaF1BD9e0" | "0x0000000000000000000000000000000000000000" | "0x0000000000000000000000000000000000000000000000000000000000000000" | "0x"
    }


}
