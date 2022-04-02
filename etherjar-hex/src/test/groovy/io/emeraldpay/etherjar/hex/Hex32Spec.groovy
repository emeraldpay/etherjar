package io.emeraldpay.etherjar.hex

import spock.lang.Specification

class Hex32Spec extends Specification {

    def "Extend from Address"() {
        expect:
        Hex32.extendFrom(HexData.from(address)).toHex() == hex
        where:
        address                                      | hex
        "0xa2ef178e4888b4826b3e26f92dd57181bf48d5d7" | "0x000000000000000000000000a2ef178e4888b4826b3e26f92dd57181bf48d5d7"
        "0x015bbdd99ab5185094eea43a56b3a7448b71a426" | "0x000000000000000000000000015bbdd99ab5185094eea43a56b3a7448b71a426"
    }

    def "Extend from existing hex32"() {
        when:
        def act = Hex32.extendFrom(HexData.from("0xefe39acb01aaae1060fd2711f388f99eb31efc11caab724e5c5d72307fe78d62"))
        then:
        act.toHex() == "0xefe39acb01aaae1060fd2711f388f99eb31efc11caab724e5c5d72307fe78d62"
    }

    def "Extend from quantity"() {
        expect:
        Hex32.extendFrom(HexQuantity.from(q)).toHex() == hex
        where:
        q          | hex
        0xa3140c0L | "0x000000000000000000000000000000000000000000000000000000000a3140c0"
        0          | "0x0000000000000000000000000000000000000000000000000000000000000000"
        1          | "0x0000000000000000000000000000000000000000000000000000000000000001"
        0xf000     | "0x000000000000000000000000000000000000000000000000000000000000f000"
    }

    def "Extend from long"() {
        expect:
        Hex32.extendFrom(q).toHex() == hex
        where:
        q          | hex
        0xa3140c0L | "0x000000000000000000000000000000000000000000000000000000000a3140c0"
        0          | "0x0000000000000000000000000000000000000000000000000000000000000000"
        1          | "0x0000000000000000000000000000000000000000000000000000000000000001"
        0xf000     | "0x000000000000000000000000000000000000000000000000000000000000f000"
    }

    def "Convert to UInt"() {
        expect:
        Hex32.from(hex).asUInt() == BigInteger.valueOf(exp)
        where:
        hex                                                                    | exp
        "0x0000000000000000000000000000000000000000000000000000000000000000"   | 0
        "0x0000000000000000000000000000000000000000000000000000000000000001"   | 1
        "0x000000000000000000000000000000000000000000000000000000000a3140c0"   | 0xa3140c0
    }

    def "Convert to Int"() {
        expect:
        Hex32.from(hex).asInt() == BigInteger.valueOf(exp)
        where:
        hex                                                                    | exp
        "0x0000000000000000000000000000000000000000000000000000000000000000"   | 0
        "0x0000000000000000000000000000000000000000000000000000000000000001"   | 1
        "0x000000000000000000000000000000000000000000000000000000000a3140c0"   | 0xa3140c0
        "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffcff76"   | -196746 // == 0xfcff76 − 0xffffff - 1
    }

    def "Extend from negative long"() {
        expect:
        Hex32.extendFrom(q).toHex() == hex
        where:
        q          | hex
        -1         | "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        -2         | "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"
        // https://docs.soliditylang.org/en/latest/types.html?#explicit-conversions
        -3         | "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffd"
        // https://ethereum.stackexchange.com/questions/96426/how-to-assign-to-negative-numbers-in-yul
        -15        | "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1"
        -196746    | "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffcff76"
    }

    def "Extend from negative big int"() {
        expect:
        Hex32.extendFrom(BigInteger.valueOf(q)).toHex() == hex
        where:
        q          | hex
        -1         | "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        -196746    | "0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffcff76"
    }
}
