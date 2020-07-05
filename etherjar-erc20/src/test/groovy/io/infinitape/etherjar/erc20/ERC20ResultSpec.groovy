package io.infinitape.etherjar.erc20

import io.infinitape.etherjar.hex.HexData
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

}
