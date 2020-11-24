package io.emeraldpay.etherjar.tx

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.HexData
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class SignerSpec extends Specification {

    Signer signer = new Signer()

    def "Sign basic tx"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 1
            gasPrice = 21000000000
            gas = 21000
            to = Address.from("0x3f4E0668C20E100d7C2A27D4b177Ac65B2875D26")
            value = Wei.ofEthers(1)
        }
        PrivateKey pk = PrivateKey.create("0x00b413b37c71bfb92719d16e28d7329dea5befa0d0b8190742f89e55617991cf")
        def exp = "f86c" +
            "01" + // nonce
            "85" + // gasprice
            "04e3b29200825208" +
            "94" + //tp
            "3f4e0668c20e100d7c2a27d4b177ac65b2875d26" +
            "88" + // value
            "0de0b6b3a7640000" +
            "80" +
            "25" + // v
            "a0" +
            "3b74616467add207e580193b7142bdd8ea7698fac1a9758ae3a22811f7b9320f" +
            "a0" +
            "6f2f42efb11ec5ca9e7cd559af43ca7d6d1a88e4e0499087a389f13e979f7172"
        when:
        def act = signer.sign(tx, pk, 1)
        then:
        act instanceof SignatureEip155
        act.r.toString(16) == "3b74616467add207e580193b7142bdd8ea7698fac1a9758ae3a22811f7b9320f"
        act.s.toString(16) == "6f2f42efb11ec5ca9e7cd559af43ca7d6d1a88e4e0499087a389f13e979f7172"
        act.v == 37

        when:
        tx.signature = act
        def rlp = tx.toRlp(true)
        then:
        Hex.encodeHexString(rlp) == exp
    }

    def "Sign with nonce 0"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 0
            gasPrice = 0x1a13b86000
            gas = 0x01d4c0
            to = Address.from("0x45BbD70553c994fDA4EEEF822F60EE962B9Ba9B4")
            value = new Wei(0x1aa535d3d0c0000)
        }
        PrivateKey pk = PrivateKey.create("0x3705ab5901b316781e4238c4c0774799efc68f5d7c914f687f3a40603b47be77")
        def exp = "f86d" +
            "80" +
            "85" +
            "1a13b860008301d4c0" +
            "94" +
            "45bbd70553c994fda4eeef822f60ee962b9ba9b4" +
            "88" +
            "01aa535d3d0c0000" +
            "80" +
            "25" +
            "a0" +
            "76feea02cf6ca6bce23b2ced62a01b42bddb77c330b70f039bfe051f896f4981" +
            "a0" +
            "4a8eefd1604f8499154b556d481d45359acec28567c0ea0ca6d74ed88961501d"
        when:
        def act = signer.sign(tx, pk, 1)
        tx.signature = act
        def rlp = tx.toRlp(true)

        then:
        Hex.encodeHexString(rlp) == exp
    }

    def "Sign create contract"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 0x11
            gasPrice = 0x09184e72a000
            gas = 0x105fa
            value = Wei.ZERO
            // not a contract but junk data, but doesn't matter here
            data = HexData.from("0x00112233445566778899aabbcceeddff")
        }
        PrivateKey pk = PrivateKey.create("0x3705ab5901b316781e4238c4c0774799efc68f5d7c914f687f3a40603b47be77")
        def exp = "f862" +
            "11" +
            "86" +
            "09184e72a000" +
            "83" +
            "0105fa" +
            "80" +
            "80" +
            "90" +
            "00112233445566778899aabbcceeddff" +
            "26" +
            "a0" +
            "11d379e8663534ce64d9b8e85f45aea3e2d058df4425445aca2f31e8859c85e4" +
            "a0" +
            "7c0bef5e92b34d5872a91841991b76840f8bfb8106bdcd900236ca9189cc562a"

        when:
        def act = signer.sign(tx, pk, 1)
        tx.signature = act
        def rlp = tx.toRlp(true)

        then:
        Hex.encodeHexString(rlp) == exp
    }

    def "Sign EIP-155 official"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 9
            gasPrice = BigInteger.valueOf(20000000000)
            gas = 0x5208 //21000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1)
        }
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        when:
        def act = signer.sign(tx, pk, 1)
        then:
        act.r.toString() == "18515461264373351373200002665853028612451056578545711640558177340181847433846"
        act.s.toString() == "46948507304638947509940763649030358759909902576025900602547168820602576006531"
        act.v == 37

        when:
        tx.signature = act
        def rlp = tx.toRlp(true)
        then:
        Hex.encodeHexString(rlp) == "f86c098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a76400008025a028ef61340bd939bc2195fe537567866003e1a15d3c71ff63e1590620aa636276a067cbe9d8997f761aecb703304b3800ccf555c9f3dc64214b297fb1966a3b6d83"
    }
}
