package io.emeraldpay.etherjar.erc20

import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class ERC20ResultSpec extends Specification {

    def "Encode balanceOf"() {
        setup:
        def result = new ERC20Result.BalanceOf().tap {
            setValue(new BigInteger("1f28d72868", 16))
        }
        when:
        def act = result.encode()
        then:
        act.toHex() == "0x0000000000000000000000000000000000000000000000000000001f28d72868"
    }

    def "Decode balanceOf"() {
        setup:
        def result = new ERC20Result.BalanceOf()
        when:
        result.decode(HexData.from("0x0000000000000000000000000000000000000000000000000000001f28d72868"))
        def act = result.getValue()
        then:
        act.toString(16) == "1f28d72868"
    }

    def "Decode short symbol"() {
        setup:
        def result = new ERC20Result.Symbol()
        when:
        result.decode(HexData.from("0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000045745544800000000000000000000000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "WETH"
    }

    def "Decode longer symbol"() {
        setup:
        def result = new ERC20Result.Symbol()
        when:
        result.decode(HexData.from("0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000001779794441492b79555344432b79555344542b7954555344000000000000000000"))
        def act = result.get()
        then:
        act == "yyDAI+yUSDC+yUSDT+yTUSD"
    }

    def "Decode non-standard symbol"() {
        setup:
        def result = new ERC20Result.Symbol()
        when:
        result.decode(HexData.from("0x4d4b520000000000000000000000000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "MKR"
    }

    def "Decode non-ascii symbol"() {
        // see 0x30bcd71b8d21fe830e493b30e90befba29de9114
        setup:
        def result = new ERC20Result.Symbol()
        when:
        result.decode(HexData.from("0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000004f09f909f00000000000000000000000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "🐟"
    }

    def "Decode short name"() {
        setup:
        def result = new ERC20Result.Name()
        when:
        result.decode(HexData.from("0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000d5772617070656420457468657200000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "Wrapped Ether"
    }

    def "Decode long name"() {
        setup:
        def result = new ERC20Result.Name()
        when:
        result.decode(HexData.from("0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000025796561726e2043757276652e666920794441492f79555344432f79555344542f7954555344000000000000000000000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "yearn Curve.fi yDAI/yUSDC/yUSDT/yTUSD"
    }

    def "Decode non-standard name"() {
        setup:
        def result = new ERC20Result.Name()
        when:
        result.decode(HexData.from("0x4d616b6572000000000000000000000000000000000000000000000000000000"))
        def act = result.get()
        then:
        act == "Maker"
    }

    def "Decode decimal 18"() {
        setup:
        def result = new ERC20Result.Decimals()
        when:
        result.decode(HexData.from("0x0000000000000000000000000000000000000000000000000000000000000012"))
        def act = result.get()
        then:
        act == 18
    }

    def "Decode decimal 6"() {
        setup:
        def result = new ERC20Result.Decimals()
        when:
        result.decode(HexData.from("0x0000000000000000000000000000000000000000000000000000000000000006"))
        def act = result.get()
        then:
        act == 6
    }
}
