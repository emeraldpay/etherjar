package io.emeraldpay.etherjar.domain

import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class EventIdSpec extends Specification {

    def "fromSignature of ERC-20 Transfer"() {
        when:
        def act = EventId.fromSignature("Transfer", "address", "address", "uint256")
        then:
        act.toHex() == "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    }

    def "fromSignature of ERC-20 Approval"() {
        when:
        def act = EventId.fromSignature("Approval", "address", "address", "uint256")
        then:
        act.toHex() == "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"
    }

    def "fromSignature with array types"() {
        when:
        // ex. https://etherscan.io/tx/0x04b626ee7a8faf89733c6d6a677002b5962b75f4939c120b8f380c691d8125d4#eventlog
        def act = EventId.fromSignature("AddLiquidity", "address", "uint256[2]", "uint256[2]", "uint256", "uint256")
        then:
        act.toHex() == "0x26f55a85081d24974e85c6c00045d0f0453991e95873f52bff0d21af4079a768"

        when:
        // ex. https://etherscan.io/tx/0xa83ac2bc5d65fcb2019604d7c06d64384e8c6662242fe825aecdeb4e0c33c48c#eventlog
        act = EventId.fromSignature("AddLiquidity", "address", "uint256[3]", "uint256[3]", "uint256", "uint256")
        then:
        act.toHex() == "0x423f6495a08fc652425cf4ed0d1f9e37e571d9b9529b1c1c23cce780b2e7df0d"


        when:
        // ex. https://etherscan.io/tx/0xd0496b5677b86ec824f8b75d6613efaba9d5dfd4a9ad6a64c1aa13450f51bb20#eventlog
        act = EventId.fromSignature("AddLiquidity", "address", "uint256[4]", "uint256[4]", "uint256", "uint256")
        then:
        act.toHex() == "0x3f1915775e0c9a38a57a7bb7f1f9005f486fb904e1f84aa215364d567319a58d"
    }

    def "fromSignature with short types"() {
        when:
        // ex. https://etherscan.io/tx/0x07b921ab749618533b76cfd12d7131ed1ae5baf61f73fe2346c1e32d14f32323#eventlog
        def act = EventId.fromSignature("Swap", "address", "address", "int256", "int256", "uint160", "uint128", "int24")
        then:
        act.toHex() == "0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67"
    }

    def "equal to Hex32"() {
        when:
        def act1 = EventId.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67") == Hex32.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67")
        def act2 = Hex32.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67") == EventId.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67")
        then:
        act1
        act2
    }

    def "equal to HexData"() {
        when:
        def act1 = EventId.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67") == HexData.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67")
        def act2 = HexData.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67") == EventId.from("0xc42079f94a6350d7e6235f29174924f928cc2ac818eb64fed8004e115fbcca67")
        then:
        act1
        act2
    }
}
