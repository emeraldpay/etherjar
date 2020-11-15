package io.emeraldpay.etherjar.contract

import io.emeraldpay.etherjar.domain.MethodId
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import spock.lang.Specification

class ContractDataSpec extends Specification {

    def "Extract null from empty"() {
        when:
        def act = ContractData.extract(HexData.EMPTY)
        then:
        act == null
    }

    def "Extract method only"() {
        when:
        def act = ContractData.extract(HexData.from("0x311b2da3"))
        then:
        act != null
        act.method == MethodId.from("0x311b2da3")
        act.arguments != null
        act.arguments.length == 0
    }

    def "Extract call with single argument"() {
        when:
        def act = ContractData.extract(HexData.from("0x9979ef450000000000000000000000000000000000000000000000000000000000008f5c"))
        then:
        act != null
        act.method == MethodId.from("0x9979ef45")
        act.arguments != null
        act.arguments.length == 1
        act.arguments[0] == Hex32.from("0x0000000000000000000000000000000000000000000000000000000000008f5c")
    }

    def "Extract ERC20 transfer"() {
        when:
        def act = ContractData.extract(HexData.from("0xa9059cbb000000000000000000000000c7dc5c95728d9ca387239af0a49b7bce8927d3090000000000000000000000000000000000000000000000000000005700ad9290"))
        then:
        act != null
        act.method == MethodId.from("0xa9059cbb")
        act.arguments != null
        act.arguments.length == 2
        act.arguments[0] == Hex32.from("0x000000000000000000000000c7dc5c95728d9ca387239af0a49b7bce8927d309")
        act.arguments[1] == Hex32.from("0x0000000000000000000000000000000000000000000000000000005700ad9290")
    }

    def "Extract ERC20 transfer with short address"() {
        when:
        def act = ContractData.extract(HexData.from("0xa9059cbb000000000000000000000000002ee48421ea53f4550e7d5a91d78e794b9a4a750000000000000000000000000000000000000000000000238fd42c5cf0400000"))
        then:
        act != null
        act.method == MethodId.from("0xa9059cbb")
        act.arguments != null
        act.arguments.length == 2
        act.arguments[0] == Hex32.from("0x000000000000000000000000002ee48421ea53f4550e7d5a91d78e794b9a4a75")
        act.arguments[1] == Hex32.from("0x0000000000000000000000000000000000000000000000238fd42c5cf0400000")
    }

    def "Extract ERC20 transferFrom"() {
        when:
        def act = ContractData.extract(HexData.from("0x23b872dd000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3000000000000000000000000f191a02f92209e87731811cc651b8f1c1126594200000000000000000000000000000000000000000000000000000000000342c3"))
        then:
        act != null
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 3
        act.arguments[0] == Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3")
        act.arguments[1] == Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942")
        act.arguments[2] == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")
    }

    def "Fail on invalid call"() {
        when:
        ContractData.extract(HexData.from("0x3101037800197ab705ac13"))
        then:
        def err = thrown(IllegalArgumentException)
        err.message.startsWith("Invalid size")
    }

    def "toData produces same result as input"() {
        expect:
        ContractData.extract(HexData.from(hex)).toData().toHex() == hex
        where:
        hex << [
            "0x311b2da3",
            "0x9979ef450000000000000000000000000000000000000000000000000000000000008f5c",
            "0xa9059cbb000000000000000000000000c7dc5c95728d9ca387239af0a49b7bce8927d3090000000000000000000000000000000000000000000000000000005700ad9290",
            "0x23b872dd000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3000000000000000000000000f191a02f92209e87731811cc651b8f1c1126594200000000000000000000000000000000000000000000000000000000000342c3"
        ]
    }

    def "Create through constructor"() {
        when:
        def act = new ContractData(
            MethodId.from("0x23b872dd"),
            [
                Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3"),
                Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942"),
                Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")
            ]
        )
        then:
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 3
        act.arguments[0] == Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3")
        act.arguments[1] == Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942")
        act.arguments[2] == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")

        when:
        act = new ContractData(
            MethodId.from("0x23b872dd"),
            [
                Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3"),
                Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942"),
                Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")
            ] as Hex32[]
        )
        then:
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 3
        act.arguments[0] == Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3")
        act.arguments[1] == Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942")
        act.arguments[2] == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")

        when:
        act = new ContractData(
            MethodId.from("0x23b872dd")
        )
        then:
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 0
    }

    def "Create with Builder"() {
        when:
        def act = ContractData.newBuilder()
            .method("0x23b872dd")
            .argument("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3")
            .argument("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942")
            .argument("0x00000000000000000000000000000000000000000000000000000000000342c3")
            .build()
        then:
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 3
        act.arguments[0] == Hex32.from("0x000000000000000000000000fc2a1e3f1ed2009fd9b9af790c788dea424d14e3")
        act.arguments[1] == Hex32.from("0x000000000000000000000000f191a02f92209e87731811cc651b8f1c11265942")
        act.arguments[2] == Hex32.from("0x00000000000000000000000000000000000000000000000000000000000342c3")

        when:
        act = ContractData.newBuilder()
            .method("0x23b872dd")
            .build()
        then:
        act.method == MethodId.from("0x23b872dd")
        act.arguments != null
        act.arguments.length == 0

        when:
        act = ContractData.newBuilder()
            .method("transfer", "address", "uint256")
            .build()
        then:
        act.method == MethodId.from("0xa9059cbb")
        act.arguments != null
        act.arguments.length == 0
    }

    def "should meet equals and hashCode contract"() {
        expect:
        EqualsVerifier.forClass(ContractData.class)
            .suppress(Warning.STRICT_INHERITANCE)
            .withNonnullFields("method", "arguments")
            .verify()
    }
}
