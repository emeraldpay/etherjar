package io.emeraldpay.etherjar.erc20

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.rpc.json.TransactionLogJson
import spock.lang.Specification

class ERC20EventSpec extends Specification {

    def "Transfer event is correct"() {
        when:
        def event = ERC20Event.TRANSFER

        then:
        event.eventId.toHex() == "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    }

    def "Approval event is correct"() {
        when:
        def event = ERC20Event.APPROVAL

        then:
        event.eventId.toHex() == "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"
    }

    def "Extract transfer details"() {
        setup:
        // https://etherscan.io/tx/0x6564204d12b63b06e06e38b6f3e40dcb9da9b5bf5deb585ed6726599a131f15b#eventlog
        def log = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                Hex32.from("0x0000000000000000000000003cd751e6b0078be393132286c442345e5dc49699"),
                Hex32.from("0x00000000000000000000000065968e42789eb8b257c34eb7dd66059708c791b0")
            ]
            it.data = HexData.from("0x000000000000000000000000000000000000000000000000000000000dbed330")
        }

        when:
        def type = ERC20Event.extractFrom(log)

        then:
        type == ERC20Event.TRANSFER

        when:
        def details = ERC20Event.TRANSFER.factory.readFrom(log) as ERC20Event.TransferDetails

        then:
        details.from == Address.from("0x3cd751e6b0078be393132286c442345e5dc49699")
        details.to == Address.from("0x65968e42789eb8b257c34eb7dd66059708c791b0")
        details.amount == new BigInteger("230609712")
    }

    def "Write transfer details"() {
        setup:
        // https://etherscan.io/tx/0x6564204d12b63b06e06e38b6f3e40dcb9da9b5bf5deb585ed6726599a131f15b#eventlog
        def details = new ERC20Event.TransferDetails(
            Address.from("0x3cd751e6b0078be393132286c442345e5dc49699"),
            Address.from("0x65968e42789eb8b257c34eb7dd66059708c791b0"),
            new BigInteger("230609712")
        )
        def exp = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                Hex32.from("0x0000000000000000000000003cd751e6b0078be393132286c442345e5dc49699"),
                Hex32.from("0x00000000000000000000000065968e42789eb8b257c34eb7dd66059708c791b0")
            ]
            it.data = HexData.from("0x000000000000000000000000000000000000000000000000000000000dbed330")
        }

        when:
        def act = new TransactionLogJson()
        details.writeTo(act)

        then:
        act == exp
    }

    def "Extract approval details"() {
        setup:
        // https://etherscan.io/tx/0x0600075e3fd5f9a141f73f9fecefe0d389371c53c202dd64265f63133045f7fe#eventlog
        def log = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"),
                Hex32.from("0x000000000000000000000000a9ba157770045cffe977601fd46b9cc3c4429604"),
                Hex32.from("0x0000000000000000000000004b92d19c11435614cd49af1b589001b7c08cd4d5")
            ]
            it.data = HexData.from("0x00000000000000000000000000000000000000000000000000000f3b2b2d196e")
        }

        when:
        def type = ERC20Event.extractFrom(log)

        then:
        type == ERC20Event.APPROVAL

        when:
        def details = ERC20Event.APPROVAL.factory.readFrom(log) as ERC20Event.ApprovalDetails

        then:
        details.owner == Address.from("0xa9ba157770045cffe977601fd46b9cc3c4429604")
        details.spender == Address.from("0x4b92d19c11435614cd49af1b589001b7c08cd4d5")
        details.amountLimit == new BigInteger("16746801863022")
    }

    def "Extract approval details for a maximum amount"() {
        setup:
        // https://etherscan.io/tx/0x1174e2eb78eb434f0de5d212051c6a0b22de17137ff6f6b5496bdd3b48377527#eventlog
        def log = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"),
                Hex32.from("0x000000000000000000000000a9ba157770045cffe977601fd46b9cc3c4429604"),
                Hex32.from("0x00000000000000000000000068b3465833fb72a70ecdf485e0e4c7bd8665fc45")
            ]
            it.data = HexData.from("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        }

        when:
        def details = ERC20Event.APPROVAL.factory.readFrom(log) as ERC20Event.ApprovalDetails

        then:
        details.owner == Address.from("0xa9ba157770045cffe977601fd46b9cc3c4429604")
        details.spender == Address.from("0x68b3465833fb72a70ecdf485e0e4c7bd8665fc45")
        details.amountLimit == Hex32.full().asUInt()
    }

    def "Write approval details"() {
        setup:
        // https://etherscan.io/tx/0x0600075e3fd5f9a141f73f9fecefe0d389371c53c202dd64265f63133045f7fe#eventlog
        def details = new ERC20Event.ApprovalDetails(
            Address.from("0xa9ba157770045cffe977601fd46b9cc3c4429604"),
            Address.from("0x4b92d19c11435614cd49af1b589001b7c08cd4d5"),
            new BigInteger("16746801863022")
        )
        def exp = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"),
                Hex32.from("0x000000000000000000000000a9ba157770045cffe977601fd46b9cc3c4429604"),
                Hex32.from("0x0000000000000000000000004b92d19c11435614cd49af1b589001b7c08cd4d5")
            ]
            it.data = HexData.from("0x00000000000000000000000000000000000000000000000000000f3b2b2d196e")
        }

        when:
        def act = new TransactionLogJson()
        details.writeTo(act)

        then:
        act == exp
    }

    def "Write approval details with maximum amount"() {
        setup:
        // https://etherscan.io/tx/0x0600075e3fd5f9a141f73f9fecefe0d389371c53c202dd64265f63133045f7fe#eventlog
        def details = new ERC20Event.ApprovalDetails(
            Address.from("0xa9ba157770045cffe977601fd46b9cc3c4429604"),
            Address.from("0x4b92d19c11435614cd49af1b589001b7c08cd4d5"),
            new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16)
        )
        def exp = new TransactionLogJson().tap {
            it.topics = [
                Hex32.from("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"),
                Hex32.from("0x000000000000000000000000a9ba157770045cffe977601fd46b9cc3c4429604"),
                Hex32.from("0x0000000000000000000000004b92d19c11435614cd49af1b589001b7c08cd4d5")
            ]
            it.data = HexData.from("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        }

        when:
        def act = new TransactionLogJson()
        details.writeTo(act)

        then:
        act == exp
    }
}
