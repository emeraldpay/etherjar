package io.emeraldpay.etherjar.contract

import io.emeraldpay.etherjar.domain.Address
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

}
