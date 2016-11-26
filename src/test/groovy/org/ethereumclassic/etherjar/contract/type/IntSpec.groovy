package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification


class IntSpec extends Specification {
    def "check is dynamic"() {
        setup:
        Int intType = new Int(8)

        expect:
        intType.isDynamic() == false
    }

    def "check invalid bits size" () {
        when:
        Int intType = new Int(size as int)

        then:
        thrown IllegalArgumentException

        where:
        _ | size
        _ | 0
        _ | -1
        _ | 7
        _ | 257
    }

    def "check get fixed size"() {
        setup:
        Int intType = new Int(size as int)

        expect:
        intType.getBytesFixedSize() == (size / 8)

        where:
        _ | size
        _ | 8
        _ | 40
        _ | 128
        _ | 256
    }

    def "check name" () {
        setup:
        Int intType = new Int(size as int)

        expect:
        intType.getName() == String.format("int%d", size)

        where:
        _ | size
        _ | 8
        _ | 40
        _ | 128
        _ | 256
    }

    def "check invalid bytes encode"() {
        setup:
        Int intType = new Int(bits)
        BigInteger par = new BigInteger(str, 16)

        when:
        intType.encode(par)

        then:
        thrown IllegalArgumentException

        where:
        bits | str
        8   | "ffffff"
        16  | "ffeeddcc"
        120 | "112233445566778899aabbccddeeff112233"
        256 | "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    }

    def "check encode"() {
        when:
        Int intType = new Int(bits)
        BigInteger par = new BigInteger(str, 16);

        then:
        intType.encode(par)[0].toString() == encoded

        where:
        bits | str                 | encoded
        8   | "1"                  | "0x0000000000000000000000000000000000000000000000000000000000000001"
        8   | "-1"                 | "0x00000000000000000000000000000000000000000000000000000000000000ff"
        8   | "7f"                 | "0x000000000000000000000000000000000000000000000000000000000000007f"
        8   | "-80"                | "0x0000000000000000000000000000000000000000000000000000000000000080"
        16  | "1"                  | "0x0000000000000000000000000000000000000000000000000000000000000001"
        16  | "-1"                 | "0x000000000000000000000000000000000000000000000000000000000000ffff"
        16  | "7fff"               | "0x0000000000000000000000000000000000000000000000000000000000007fff"
        16  | "-8000"              | "0x0000000000000000000000000000000000000000000000000000000000008000"
        40  | "1"                  | "0x0000000000000000000000000000000000000000000000000000000000000001"
        40  | "-1"                 | "0x000000000000000000000000000000000000000000000000000000ffffffffff"
        40  | "7fffffffff"                       | "0x0000000000000000000000000000000000000000000000000000007fffffffff"
        40  | "-8000000000"                      | "0x0000000000000000000000000000000000000000000000000000008000000000"
        64  | "1"                                | "0x0000000000000000000000000000000000000000000000000000000000000001"
        64  | "-1"                               | "0x000000000000000000000000000000000000000000000000ffffffffffffffff"
        64  | "7fffffffffffffff"                 | "0x0000000000000000000000000000000000000000000000007fffffffffffffff"
        64  | "-8000000000000000"                | "0x0000000000000000000000000000000000000000000000008000000000000000"
        120 | "1"                                | "0x0000000000000000000000000000000000000000000000000000000000000001"
        120 | "-1"                               | "0x0000000000000000000000000000000000ffffffffffffffffffffffffffffff"
        120 | "7fffffffffffffffffffffffffffff"   | "0x00000000000000000000000000000000007fffffffffffffffffffffffffffff"
        120 | "-800000000000000000000000000000"  | "0x0000000000000000000000000000000000800000000000000000000000000000"
        256 | "1"                                | "0x0000000000000000000000000000000000000000000000000000000000000001"
        256 | "-1"                               | "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        256 | "7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"  | "0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        256 | "-8000000000000000000000000000000000000000000000000000000000000000" | "0x8000000000000000000000000000000000000000000000000000000000000000"
    }

    def "check decode"() {
        setup:
        Int intType = new Int(bits)
        Hex32[] par = [new Hex32(array as byte[])]
        BigInteger decoded = new BigInteger(array as byte[])

        expect:
        intType.decode(par) == decoded

        where:
        bits | array                                                         | str
        8   | [0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]              | "+1"
        16   | [0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]               | "+1"
        16   | [0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]   | "-8000"
//        40   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]               | "+1"
//        40   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        64   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        64   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        120   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        120   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        256   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
//        256   | [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01]                    | "+1"
    }
}
