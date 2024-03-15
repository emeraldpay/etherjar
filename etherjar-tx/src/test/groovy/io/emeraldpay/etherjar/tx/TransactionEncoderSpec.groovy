/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class TransactionEncoderSpec extends Specification {

    TransactionDecoder decoder = new TransactionDecoder()
    TransactionEncoder encoder = new TransactionEncoder()

    def "re-encode transaction 0x4cd4de"() {
        // id: 0x4cd4deba8e414a0e60a41b4c0d8470853ebfb4f3d793f09d63b78de7437a220c

        setup:
        def tx = "f8cb82190785055ae8260082d2e2949ca222a6350c37c1b5014c5c59dc36892af3335880b864beabacc80000000000000000000000009ca222a6350c37c1b5014c5c59dc36892af333580000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000126a040f7779aa122cee043e26bce3c65ea8f8fcbc6eba8de5534e59227818e67d290a06b5a4e78a378231e4d95d681b106ea02daa967db5404683306ef51959ff938ab"
        when:
        def parsed = decoder.decode(Hex.decodeHex(tx))
        parsed.signature.message = parsed.hash()
        def act = encoder.encode(parsed, true)

        then:
        Hex.encodeHexString(act) == tx
    }

    def "encode transaction with nonce 127"() {
        // id: 0xc49a95e7aafc11acf436bda1b545a3aa4e8370244d264ea85efffa20265801f5
        setup:
        def tx = "f901287f843b9aca0082eb80945e07b6f1b98a11f7e04e7ffa8707b63f1c17775380b8c471b773440000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000005db51c820000000000000000000000000000000000000000000000000000000000000041a71d8021704d529b7bd1552395702fbad11abbe13cb4c04ec60c5ad26139ada60395c36474526b0edd88a7edfb6bbc16baa8cc3c28419d2c4c44f3757aef68c8010000000000000000000000000000000000000000000000000000000000000026a0055a78daaf221cc6edfe378dfa9f7401346616b69126061982fed9533f13a019a0287773ddfb9b2c6fd08a8da52830d888dc009780cc1eecec85c64cdb51f9ec7a"
        when:
        def parsed = decoder.decode(Hex.decodeHex(tx))
        parsed.signature.message = parsed.hash()
        def act = encoder.encode(parsed, true)

        then:
        Hex.encodeHexString(act) == tx
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
        def act = encoder.encodeStandard(tx, false, 1)
        then:
        Hex.encodeHexString(act) == "ec098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a764000080018080"
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
        def act = encoder.encodeStandard(tx,false, 0x25)
        then:
        Hex.encodeHexString(act) == exp
    }

    def "Encode unsigned tx with Access List"() {
        setup:
        TransactionWithAccess tx = new TransactionWithAccess()
        tx.tap {
            chainId = 1
            nonce = 0
            gasPrice = Wei.ofUnits(20, Wei.Unit.GWEI) // 0x4A817C800
            gas = 0x249F0 //150_000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1) // 0xDE0B6B3A7640000
            accessList = [
                new TransactionWithAccess.Access(
                    Address.from("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000003"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000007"),
                )
            ]
        }

        when:
        def act = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(act) == "01f88b01808504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f85bf85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a00000000000000000000000000000000000000000000000000000000000000007808080"
    }

    def "Encode signed tx with Access List"() {
        setup:
        TransactionWithAccess tx = new TransactionWithAccess()
        tx.tap {
            chainId = 1
            nonce = 0
            gasPrice = Wei.ofUnits(20, Wei.Unit.GWEI) // 0x4A817C800
            gas = 0x249F0 //150_000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1) // 0xDE0B6B3A7640000
            accessList = [
                new TransactionWithAccess.Access(
                    Address.from("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000003"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000007"),
                )
            ]
            signature = new SignatureEIP2930().tap {
                setYParity(1)
                r = new BigInteger(1, Hex.decodeHex("38c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255"))
                s = new BigInteger(1, Hex.decodeHex("3acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125"))
            }
        }

        when:
        def act = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(act) == "01f8cb01808504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f85bf85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a0000000000000000000000000000000000000000000000000000000000000000701a038c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255a03acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125"
    }

    def "Encode signed tx with Access List 2"() {
        setup:
        TransactionWithAccess tx = new TransactionWithAccess()
        tx.tap {
            chainId = 1
            nonce = 1
            gasPrice = Wei.ofUnits(20, Wei.Unit.GWEI) // 0x4A817C800
            gas = 0x249F0 //150_000
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1) // 0xDE0B6B3A7640000
            accessList = [
                new TransactionWithAccess.Access(
                    Address.from("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000003"),
                    Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000007"),
                ),
                new TransactionWithAccess.Access(
                    Address.from("0xbb9bc244d798123fde783fcc1c72d3bb8c189413")
                )
            ]
            signature = new SignatureEIP2930().tap {
                setYParity(0)
                r = new BigInteger(1, Hex.decodeHex("b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7a"))
                s = new BigInteger(1, Hex.decodeHex("431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894"))
            }
        }

        when:
        def act = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(act) == "01f8e201018504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f872f85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a00000000000000000000000000000000000000000000000000000000000000007d694bb9bc244d798123fde783fcc1c72d3bb8c189413c080a0b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7aa0431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894"
    }

    def "Encode signed tx with gas priority"() {
        // 0xe2c9ad4b92dfdea74203f83c503b769525ada75b9a53745f70113f23c077162c
        setup:
        TransactionWithGasPriority tx = new TransactionWithGasPriority()
        tx.tap {
            nonce = 150
            maxGasPrice = new Wei(82684598939)
            priorityGasPrice = new Wei(4000000000)
            gas = 51_101
            to = Address.from("0x7bebd226154e865954a87650faefa8f485d36081")
            value = Wei.ZERO
            data = HexData.from("0x095ea7b300000000000000000000000003f7724180aa6b939894b5ca4314783b0b36b329ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            chainId = 1
            accessList = []
            signature = new SignatureEIP2930().tap {
                setYParity(1)
                r = new BigInteger(1, Hex.decodeHex("d978ed98e78dd480b2aec86d1521962a8fe4009e44fb19f45b70d8005e602182"))
                s = new BigInteger(1, Hex.decodeHex("347c933f78131995c1abd07c1d0be67d8f04c2cf99cd79510657e97ead8c1a9f"))
            }
            with((SignatureEIP2930)signature) {
                YParity == 1
                r.toString(16) == "d978ed98e78dd480b2aec86d1521962a8fe4009e44fb19f45b70d8005e602182"
                s.toString(16) == "347c933f78131995c1abd07c1d0be67d8f04c2cf99cd79510657e97ead8c1a9f"
            }
        }

        when:
        def act = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(act) == "02f8b101819684ee6b280085134062da9b82c79d947bebd226154e865954a87650faefa8f485d3608180b844095ea7b300000000000000000000000003f7724180aa6b939894b5ca4314783b0b36b329ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc001a0d978ed98e78dd480b2aec86d1521962a8fe4009e44fb19f45b70d8005e602182a0347c933f78131995c1abd07c1d0be67d8f04c2cf99cd79510657e97ead8c1a9f"
    }
}
