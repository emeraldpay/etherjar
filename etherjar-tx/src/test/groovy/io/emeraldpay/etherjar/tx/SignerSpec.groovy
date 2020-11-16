package io.emeraldpay.etherjar.tx

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
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
            "26" + // v
            "a0" +
            "3b74616467add207e580193b7142bdd8ea7698fac1a9758ae3a22811f7b9320f" +
            "a0" +
            "90d0bd104ee13a3561832aa650bc35814d945401ceff0fb41c486d4e3896cfcf"
        when:
        def act = signer.sign(tx, pk, 1)
        then:
        act instanceof SignatureEip155
        act.r.toString(16) == "3b74616467add207e580193b7142bdd8ea7698fac1a9758ae3a22811f7b9320f"
        act.s.toString(16) == "90d0bd104ee13a3561832aa650bc35814d945401ceff0fb41c486d4e3896cfcf"
        act.v == 38
        // or s=6f2f42efb11ec5ca9e7cd559af43ca7d6d1a88e4e0499087a389f13e979f7172 and v=37 ?

        when:
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
