package io.emeraldpay.etherjar.domain

import io.emeraldpay.etherjar.hex.Hex32
import spock.lang.Ignore
import spock.lang.Specification

class BloomSpec extends Specification {

    def "Correct byte position"() {
        expect:
        Bloom.bytePosition(bitpos) == bytepos
        where:
        bitpos  | bytepos
        0       | 0
        1       | 0
        2       | 0
        3       | 0
        4       | 0
        5       | 0
        6       | 0
        7       | 0
        8       | 1
        9       | 1
        2047    | 255
    }

    def "Correct bytemask"() {
        expect:
        Bloom.byteMask(bytepos, bitpos) == mask
        where:
        bitpos  | bytepos  | mask
        0       | 0        | 0b0000_0001
        1       | 0        | 0b0000_0010
        2       | 0        | 0b0000_0100
        7       | 0        | 0b1000_0000
        8       | 1        | 0b0000_0001
        9       | 1        | 0b0000_0010
        16      | 2        | 0b0000_0001
        2047    | 255      | 0b1000_0000
    }

    def "Zero topic bloom"() {
        setup:
        // topic hash: 290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563
        def exp = "0x" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000002000000000000000000080000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000002000" +
            "0000000000000000000000000000000000000000000000000000000000000000"
        when:
        def act = Bloom.newBuilder()
            .add(Hex32.EMPTY)
            .build()
        then:
        act.toHex() == exp
    }

    def "Bloom for tx 0x4a2a53"() {
        // 0x4a2a534600f7a8486e33f84a06645ed838a220fc1ffb019b1d8cf84cb3b04be0
        setup:
        def exp = "0x" +
            "0000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000010000000000000000000000000000000000" +
            "0000000000000000000000080000000000000000000000000000000000000000" +
            "0000000000000000000000000004000000000000000000000000001000000080" +
            "0000000000000000000020000000000000000000000000000000000000100000" +
            "0000000000000000000000800000000000000000000000004000000000000800" +
            "0000000200000000000000000000000000000000000000000000001000000000" +
            "0000000000000000000000000000000000000000000000000000000000000000"
        when:
        def act = Bloom.newBuilder()
            .add(Address.from("0xdac17f958d2ee523a2206206994597c13d831ec7"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x0000000000000000000000003ee28d54eb2c4ad8249702e6eccb45f26d72890e"))
            .add(Hex32.from("0x000000000000000000000000a8db2baf1b852c45481f59c6d31002557e95a9d0"))
            .build()
        then:
        act.toHex().replaceAll("0+", ".") == exp.replaceAll("0+", ".")
        act.toHex() == exp
    }

    def "Find by address"() {
        setup:
        def bloom = Bloom.newBuilder()
            .add(Address.from("0xdac17f958d2ee523a2206206994597c13d831ec7"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x0000000000000000000000003ee28d54eb2c4ad8249702e6eccb45f26d72890e"))
            .add(Hex32.from("0x000000000000000000000000a8db2baf1b852c45481f59c6d31002557e95a9d0"))
            .build()
        def filter = Bloom.newBuilder()
            .add(Address.from("0xdac17f958d2ee523a2206206994597c13d831ec7"))
            .buildFilter()
        when:
        def act = filter.isSet(bloom)
        then:
        act

        when:
        def anotherAddress = Bloom.newBuilder()
            .add(Address.from("0x523a2206206994597c13d831ec7dac17f958d2ee"))
            .buildFilter()
        act = anotherAddress.isSet(bloom)
        then:
        !act
    }

    def "Bloom for tx 0xce9ec4"() {
        // 0xce9ec405185898009d96e54b661ba2c2ff927728c80aa3652d6cf30ed8b23744
        setup:
        def exp = "0x" +
            "4020000000000000000000008000000000000000000000000001000000000000" +
            "0000002000000000000000000000000042000000080000000000000000200000" +
            "0000000000000000000000080000002000010000004008000000000200002000" +
            "0000000000000000000000000000000000000000000004000000001000000000" +
            "0800000000000000004000000000000000400000000000080000004000000000" +
            "0200000000000000000000000000000000000000000000000000000000000000" +
            "0400000200000000000000000000000000000000000000100000000200002000" +
            "0010200000000000000000100000100000000000000000000000000000000000"

        when:
        def act = Bloom.newBuilder()
            .add(Address.from("0x1695936d6a953df699c38ca21c2140d497c08bd9"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x0000000000000000000000007ee7895f7595e0a4f8843ad659772152d863488d"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))

            .add(Address.from("0x1695936d6a953df699c38ca21c2140d497c08bd9"))
            .add(Hex32.from("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"))
            .add(Hex32.from("0x0000000000000000000000007ee7895f7595e0a4f8843ad659772152d863488d"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))

            .add(Address.from("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))

            .add(Address.from("0xdf27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Hex32.from("0x1c411e9a96e071241c2f21f7726b17ae89e3cab4c78be50e062b03a9fffbbad1"))

            .add(Address.from("0xdf27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Hex32.from("0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))

            .add(Address.from("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"))
            .add(Hex32.from("0x7fcf532c15f0a6db0bd6d0e038bea71d30d808c7d98cb3bf7268a95bf5081b65"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))
            .build()
        then:
        act.toHex() == exp
    }

    def "Merge two"() {
        setup:
        def bloom1 = Bloom.newBuilder()
            .add(Address.from("0x1695936d6a953df699c38ca21c2140d497c08bd9"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x0000000000000000000000007ee7895f7595e0a4f8843ad659772152d863488d"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .build()
        def bloom2 = Bloom.newBuilder()
            .add(Address.from("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))
            .build()
        def exp = Bloom.newBuilder()
            .add(Address.from("0x1695936d6a953df699c38ca21c2140d497c08bd9"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x0000000000000000000000007ee7895f7595e0a4f8843ad659772152d863488d"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Address.from("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"))
            .add(Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"))
            .add(Hex32.from("0x000000000000000000000000df27a38946a1ace50601ef4e10f07a9cc90d7231"))
            .add(Hex32.from("0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d"))
            .build()
        when:
        def act = bloom1.mergeWith(bloom2)
        then:
        act == exp
        act != bloom1
        act != bloom2
        bloom1 != bloom2
    }

}
