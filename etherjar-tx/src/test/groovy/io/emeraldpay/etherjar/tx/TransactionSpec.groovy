/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.etherjar.tx

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class TransactionSpec extends Specification {

    def "read transaction 0x19442f"() {
        // id: 0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31
        setup:
        def tx = Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash()

        then:
        act.nonce == 15524
        act.gasPrice.toHex() == "0x59b9b95f0"
        act.gas == 0x03d090
        act.to.toHex() == "0x8b3b3b624c3c0397d3da8fd861512393d51dcbac"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x667a2f58"
        act.signature instanceof Signature
        !(act.signature instanceof SignatureEip155)
        act.signature != null
        act.signature.v == 28
        act.signature.r.toString(16) == "d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64"
        act.signature.s.toString(16) == "39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"

        act.signature.recoverAddress().toHex() == "0xed059bc543141c8c93031d545079b3da0233b27f"
    }

    def "re-encode transaction 0x4cd4de"() {
        // id: 0x4cd4deba8e414a0e60a41b4c0d8470853ebfb4f3d793f09d63b78de7437a220c

        setup:
        def tx = "f8cb82190785055ae8260082d2e2949ca222a6350c37c1b5014c5c59dc36892af3335880b864beabacc80000000000000000000000009ca222a6350c37c1b5014c5c59dc36892af333580000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000126a040f7779aa122cee043e26bce3c65ea8f8fcbc6eba8de5534e59227818e67d290a06b5a4e78a378231e4d95d681b106ea02daa967db5404683306ef51959ff938ab"
        when:
        def parsed = Transaction.fromRlp(Hex.decodeHex(tx))
        parsed.signature.message = parsed.hash()
        def act = parsed.toRlp(true)

        then:
        Hex.encodeHexString(act) == tx
    }

    def "encode transaction with nonce 127"() {
        // id: 0xc49a95e7aafc11acf436bda1b545a3aa4e8370244d264ea85efffa20265801f5
        setup:
        def tx = "f901287f843b9aca0082eb80945e07b6f1b98a11f7e04e7ffa8707b63f1c17775380b8c471b773440000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000005db51c820000000000000000000000000000000000000000000000000000000000000041a71d8021704d529b7bd1552395702fbad11abbe13cb4c04ec60c5ad26139ada60395c36474526b0edd88a7edfb6bbc16baa8cc3c28419d2c4c44f3757aef68c8010000000000000000000000000000000000000000000000000000000000000026a0055a78daaf221cc6edfe378dfa9f7401346616b69126061982fed9533f13a019a0287773ddfb9b2c6fd08a8da52830d888dc009780cc1eecec85c64cdb51f9ec7a"
        when:
        def parsed = Transaction.fromRlp(Hex.decodeHex(tx))
        parsed.signature.message = parsed.hash()
        def act = parsed.toRlp(true)

        then:
        Hex.encodeHexString(act) == tx
    }

    def "Unsigned hash"() {
        setup:
        def tx = Transaction.fromRlp(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def hash = tx.hash()
        then:
        Hex.encodeHexString(hash) == "383caae49692ae021fb2189933518ca58fd04d88e99b41a4d18f5ae5fb5f52aa"
    }

    def "Get transaction id"() {
        setup:
        def tx = Transaction.fromRlp(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def txid = tx.transactionId()
        then:
        txid.toString() == "0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31"
    }

    def "Parse goerli 0x6d4d85"() {
        // 0x6d4d85482c59b6fe2f416996c802ceae2e30b9fe6bc27fe5c72d2fa9b1b2e28b
        setup:
        def tx = Hex.decodeHex("f8cb82afdc843b9aca008303d090947ef66b77759e12caf3ddb3e4aff524e577c59d8d80b864e9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb08641ba01e8a3bacc31fc91ade73278d0267b70d38b53623ab0a28d1e20e133286f8a85ca02f2e0ac2c4e9e4410804fa62c9cba70e18eed8b93ec2a2ad5762279de8288b63")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash()

        then:
        act.nonce == 45020
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI)
        act.gas == 250000
        act.to.toHex() == "0x7ef66b77759e12caf3ddb3e4aff524e577c59d8d"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0xe9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb0864"
        act.signature != null

        act.signature.recoverAddress() == Address.from("0x79047abf3af2a1061b108d71d6dc7bdb06474790")
    }

    def "Parse goerli 0x8d8367"() {
        // 0x8d8367acc4c8f17fa6b9e8a856833b3406b200f96a5fa98018128411b1f0c6d1
        setup:
        def tx = Hex.decodeHex("f86c8227b2843b9aca008275309413ac1a2c6d1a4efc492a40d8f9d4e9f14b7c726887b1a2bc2ec50000001ca077313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23a079c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365")
        when:
        def act = Transaction.fromRlp(tx)
        act.signature.message = act.hash()

        then:
        act.nonce == 10162
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI)
        act.gas == 30000
        act.to.toHex() == "0x13ac1a2c6d1a4efc492a40d8f9d4e9f14b7c7268"
        act.value == Wei.ofEthers(0.05)
        act.data.toHex() == "0x00"
        act.signature != null
        act.signature.v == 28
        act.signature.r.toString(16) == "77313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23"
        act.signature.s.toString(16) == "79c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365"

        act.signature.recoverAddress().toHex() == "0x8ced5ad0d8da4ec211c17355ed3dbfec4cf0e5b9"
    }

    def "Encode EIP-155 official"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 9
            gasPrice = BigInteger.valueOf(20000000000)
            gas = 0x5208 //21000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1)
        }
        when:
        def act = tx.toRlp(false, 1)
        then:
        Hex.encodeHexString(act) == "ec098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a764000080018080"
    }

    def "Hash EIP-155 official"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 9
            gasPrice = BigInteger.valueOf(20000000000)
            gas = 0x5208 //21000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1)
        }
        when:
        def act = tx.hash(1)
        then:
        Hex.encodeHexString(act) == "daf5a779ae972f972197303d7b574746c7ef83eadac0f2791ad23db92e4c8e53"
    }

    def "Encode basic"() {
        setup:
        Transaction tx = new Transaction()
        tx.tap {
            nonce = 1
            gasPrice = BigInteger.valueOf(0x4e3b29200)
            gas = 0x5208 //21000
            to = Address.from("0x3eaf0b987b49c4d782ee134fdc1243fd0ccdfdd3")
            value = new Wei(0xDE0B6B3A764000)
        }
        def exp = "" +
            "eb" + //total size = 1 +6 +3 +21 +8 +1 +1 +1 +1 + 0xc0
            "01" + //nonce
            "85" + "04e3b29200" + //gasprice
            "82" + "5208" + //gas
            "94" + "3eaf0b987b49c4d782ee134fdc1243fd0ccdfdd3" + // to
            "87" + "de0b6b3a764000" + //value
            "80" + //data
            "25" + //chain id
            "80" + //r
            "80" //s
        when:
        def act = tx.toRlp(false, 0x25)
        then:
        Hex.encodeHexString(act) == exp
    }
}
