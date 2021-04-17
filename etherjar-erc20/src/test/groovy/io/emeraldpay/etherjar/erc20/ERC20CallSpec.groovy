package io.emeraldpay.etherjar.erc20

import io.emeraldpay.etherjar.domain.Address
import spock.lang.Specification

class ERC20CallSpec extends Specification {

    def "Encode transfer"() {
        when:
        def act = new ERC20Call.Transfer().tap {
            setTo(Address.from("0xe3c2e70acbc9ba12de6cd1e453c050efc9a56253"))
            setValue(new BigInteger("cea3e81c01910000", 16))
        }.encode().toData()
        then:
        act.toHex() == "0xa9059cbb" +
            "000000000000000000000000e3c2e70acbc9ba12de6cd1e453c050efc9a56253" +
            "000000000000000000000000000000000000000000000000cea3e81c01910000"
    }

    def "Encode transferFrom"() {
        when:
        def act = new ERC20Call.TransferFrom().tap {
            setTo(Address.from("0xe3c2e70acbc9ba12de6cd1e453c050efc9a56253"))
            setFrom(Address.from("0xd3e52099a6a48f132cb23b1364b7dee212d862f6"))
            setValue(new BigInteger("cea3e81c01910000", 16))
        }.encode().toData()
        then:
        act.toHex() == "0x23b872dd" +
            "000000000000000000000000d3e52099a6a48f132cb23b1364b7dee212d862f6" +
            "000000000000000000000000e3c2e70acbc9ba12de6cd1e453c050efc9a56253" +
            "000000000000000000000000000000000000000000000000cea3e81c01910000"
    }

    def "Encode approve"() {
        when:
        def act = new ERC20Call.Approve().tap {
            setSpender(Address.from("0xe3c2e70acbc9ba12de6cd1e453c050efc9a56253"))
            setValue(new BigInteger("cea3e81c01910000", 16))
        }.encode().toData()
        then:
        act.toHex() == "0x095ea7b3" +
            "000000000000000000000000e3c2e70acbc9ba12de6cd1e453c050efc9a56253" +
            "000000000000000000000000000000000000000000000000cea3e81c01910000"
    }

    def "Encode balanceOf"() {
        when:
        def act = new ERC20Call.BalanceOf().tap {
            setAddress(Address.from("0xe3c2e70acbc9ba12de6cd1e453c050efc9a56253"))
        }.encode().toData()
        then:
        act.toHex() == "0x70a08231" +
            "000000000000000000000000e3c2e70acbc9ba12de6cd1e453c050efc9a56253"
    }

    def "Encode totalSupply"() {
        when:
        def act = new ERC20Call.TotalSupply()
            .encode().toData()
        then:
        act.toHex() == "0x18160ddd"
    }

    def "Encode allowance"() {
        when:
        def act = new ERC20Call.Allowance().tap {
            setOwner(Address.from("0xe3c2e70acbc9ba12de6cd1e453c050efc9a56253"))
            setSpender(Address.from("0xd3e52099a6a48f132cb23b1364b7dee212d862f6"))
        }.encode().toData()
        then:
        act.toHex() == "0xdd62ed3e" +
            "000000000000000000000000e3c2e70acbc9ba12de6cd1e453c050efc9a56253" +
            "000000000000000000000000d3e52099a6a48f132cb23b1364b7dee212d862f6"
    }

    def "Encode symbol"() {
        when:
        def act = new ERC20Call.Symbol()
            .encode().toData()
        then:
        act.toHex() == "0x95d89b41"
    }

    def "Encode name"() {
        when:
        def act = new ERC20Call.Name()
            .encode().toData()
        then:
        act.toHex() == "0x06fdde03"
    }

    def "Encode decimals"() {
        when:
        def act = new ERC20Call.Decimals()
            .encode().toData()
        then:
        act.toHex() == "0x313ce567"
    }
}
