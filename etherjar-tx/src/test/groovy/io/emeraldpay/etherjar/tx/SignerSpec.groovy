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

class SignerSpec extends Specification {

    Signer signer = new Signer(1)
    TransactionEncoder encoder = new TransactionEncoder()

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
        def act = signer.sign(tx, pk)
        then:
        act instanceof SignatureEIP155
        act.r.toString(16) == "3b74616467add207e580193b7142bdd8ea7698fac1a9758ae3a22811f7b9320f"
        act.s.toString(16) == "6f2f42efb11ec5ca9e7cd559af43ca7d6d1a88e4e0499087a389f13e979f7172"
        act.v == 37

        when:
        tx.signature = act
        def rlp = encoder.encode(tx,true)
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
        def act = signer.sign(tx, pk)
        tx.signature = act
        def rlp = encoder.encode(tx, true)

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
        def act = signer.sign(tx, pk)
        tx.signature = act
        def rlp = encoder.encode(tx, true)

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
        def act = signer.sign(tx, pk)
        then:
        act.r.toString() == "18515461264373351373200002665853028612451056578545711640558177340181847433846"
        act.s.toString() == "46948507304638947509940763649030358759909902576025900602547168820602576006531"
        act.v == 37

        when:
        tx.signature = act
        def rlp = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(rlp) == "f86c098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a76400008025a028ef61340bd939bc2195fe537567866003e1a15d3c71ff63e1590620aa636276a067cbe9d8997f761aecb703304b3800ccf555c9f3dc64214b297fb1966a3b6d83"
    }

    def "Sign tx with Access List"() {
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
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        when:
        def hash = signer.hash(tx)

        then:
        Hex.encodeHexString(hash) == "57c3588c6ef4be66e68464a5364cef58fe154f57b2ff8d8d89909ac10cd0527b"

        when:
        def act = signer.sign(tx, pk)
        then:
        act instanceof SignatureEIP2930
        with((SignatureEIP2930)act) {
            r.toString(16) == "38c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255"
            s.toString(16) == "3acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125"
            YParity == 1
        }

        when:
        tx.signature = act
        def rlp = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(rlp) == "01f8cb01808504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f85bf85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a0000000000000000000000000000000000000000000000000000000000000000701a038c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255a03acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125"
    }

    def "Sign tx with Access List 2"() {
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
        }
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        when:
        def hash = signer.hash(tx)

        then:
        Hex.encodeHexString(hash) == "aef1156bbd124793e5d76bdf9fe9464e9ef79f2432abbaf0385e57e8ae8e8d5c"

        when:
        def act = signer.sign(tx, pk)

        then:
        act instanceof SignatureEIP2930
        with((SignatureEIP2930)act) {
            r.toString(16) == "b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7a"
            s.toString(16) == "431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894"
            YParity == 0
        }

        when:
        tx.signature = act
        def rlp = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(rlp) == "01f8e201018504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f872f85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a00000000000000000000000000000000000000000000000000000000000000007d694bb9bc244d798123fde783fcc1c72d3bb8c189413c080a0b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7aa0431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894"
    }

    def "Sign tx with Gas Priority"() {
        setup:
        TransactionWithGasPriority tx = new TransactionWithGasPriority()
        tx.tap {
            chainId = 1
            nonce = 1234 // 0x04d2
            maxGasPrice = Wei.ofUnits(20, Wei.Unit.GWEI)
            priorityGasPrice = Wei.ofUnits(1, Wei.Unit.GWEI)
            gas = 150_000 // 0x0249f0
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1.2345)
            accessList = []
        }
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        when:
        def hash = signer.hash(tx)

        then:
        Hex.encodeHexString(hash) == "68fe011ba5be4a03369d51810e7943abab15fbaf757f9296711558aee8ab772b"

        when:
        def act = signer.sign(tx, pk)
        then:
        act instanceof SignatureEIP2930
        with((SignatureEIP2930)act) {
            r.toString(16) == "f0b3347ec48e78bf5ef6075b332334518ebc2f90d2bf0fea080623179936382e"
            s.toString(16) == "5c58c5beeafb2398d5e79b40b320421112a9672167f27e7fc55e76d2d7d11062"
            YParity == 1
        }

        when:
        tx.signature = act
        def rlp = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(rlp) == "02f876018204d2843b9aca008504a817c800830249f0943535353535353535353535353535353535353535881121d3359738400080c001a0f0b3347ec48e78bf5ef6075b332334518ebc2f90d2bf0fea080623179936382ea05c58c5beeafb2398d5e79b40b320421112a9672167f27e7fc55e76d2d7d11062"
    }

    def "Sign Sepolia"() {
        setup:
        TransactionWithGasPriority tx = new TransactionWithGasPriority()
        tx.tap {
            chainId = 11155111
            nonce = 0x0123
            maxGasPrice = Wei.ofUnits(20, Wei.Unit.GWEI)
            priorityGasPrice = Wei.ofUnits(1, Wei.Unit.GWEI)
            gas = 150_000 // 0x0249F0
            to = Address.from("0x3535353535353535353535353535353535353535")
            value = Wei.ofEthers(1)
            accessList = []
        }
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        when:
        def hash = signer.hash(tx)

        then:
        Hex.encodeHexString(hash) == "c8dc2cc014237c5a09db43f575b41f89f4f55e4160a3a0e118250f102aec61ab"

        when:
        def act = signer.sign(tx, pk)
        then:
        act instanceof SignatureEIP2930
        with((SignatureEIP2930)act) {
            r.toString(16) == "ea3705d1137256ba5078c2d97a8886ea78e3c9ea4d3ffaa4985220705fdf02e1"
            s.toString(16) == "f05771ac81ddb47283f592790db1205c6b321c3cdc5164b16d89898d0802647"
            YParity == 1
        }

        when:
        tx.signature = act
        def rlp = encoder.encode(tx, true)
        then:
        Hex.encodeHexString(rlp) == "02f87983aa36a7820123843b9aca008504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080c001a0ea3705d1137256ba5078c2d97a8886ea78e3c9ea4d3ffaa4985220705fdf02e1a00f05771ac81ddb47283f592790db1205c6b321c3cdc5164b16d89898d0802647"
    }

    def "extract pubkey from base tx - 0x19442f"() {
        setup:
        Signature signature = new Signature()
        signature.message = Hex.decodeHex("383caae49692ae021fb2189933518ca58fd04d88e99b41a4d18f5ae5fb5f52aa")
        signature.v = 28
        signature.r = new BigInteger("d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64", 16)
        signature.s = new BigInteger("39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b", 16)

        when:
        def pubkey = Signer.ecrecover(signature)

        then:
        Hex.encodeHexString(pubkey) == "b5c5e005c6396a8f78dd4ac3f09c8a5dc88f56fe7764323f925e8c0d4991e730a12e0cfa5cac29b4fffc2852134fbd589ed3a99a8094a8f3d243a86204b72d01"
    }

    def "extract pubkey from EIP155 tx - 0x9d2b0a"() {
        setup:
        Signature signature = new SignatureEIP155(61)
        signature.message = Hex.decodeHex("ee83527ea74d7b08cca67de5f2adfe7bf371c5e6dbcf7e851db83cfa27e50afb")
        signature.v = 157
        signature.r = new BigInteger("271658e49edd3495771f43734d29fac87cac9e740bf4c60a5847a6606ea8b38e", 16)
        signature.s = new BigInteger("209874432a43edb6376afe704940b78521058fdbc7c85e55c6f5b2280c2b4942", 16)

        when:
        def pubkey = Signer.ecrecover(signature)

        then:
        Hex.encodeHexString(pubkey) == "653952d0020981cf0332c34fe3c931a8fc4c85e4380eb9c11bc16dcdcaae6ef961b4461519c04dd83b4d2937b2585f6734ef074b3859d58b0093183858ab0ca1"
    }

    def "Sign message"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        def message = "test-test-test"

        when:
        def act = signer.signMessageEncoded(message, pk)

        then:
        act.toHex() == "0xc26a3a1922d97e573db507e82cbace7b57e54106cc96d598d29ac16aabe48153313302cb629b7307baae0ae5e74f68e58564615ccfde0d03603381e1a233e0ed1c"
    }

    def "Sign message 2"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        def message = "test-test-test 2"

        when:
        def act = signer.signMessageEncoded(message, pk)

        then:
        act.toHex() == "0x86f13303ffc5c05b3bf500f7f6f8bce9074721ea792e41c9f3624318ee08eebc6c1e4c3f091b2c9611361d462af3103c64a6873918c1aacaf2171bd36615f9f61c"
    }

    def "Sign message 3"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        def message = "test-test-test 3"

        when:
        def act = signer.signMessageEncoded(message, pk)

        then:
        act.toHex() == "0xb16541fb0a35a5415c9ddc59afd410b45af88c97e7ca7b172306e9513951279a64d8fc0e4efe055417e604244d53f538422f0b7c686c10133ebad1c91df2980d1b"
    }

    def "Sign message 4"() {
        setup:
        PrivateKey pk = PrivateKey.create("0xb16541fb0a35a5415c9ddc59afd410b45af88c97e7ca7b172306e9513f538422")
        def message = "test b16541fb0a35a5415c9ddc59afd410b45af88c97e7ca7b172306e9513951279a64d8fc0e4efe055417e604244d53f538422f0b7c686c10133ebad1c91df2980d1b"

        when:
        def act = signer.signMessageEncoded(message, pk)

        then:
        act.toHex() == "0xde8d65f0d3de2fbdac8f9348b7e215bcaa7780f772ed28e7d6cdae458938b86b51411b1d50af102484b3bbd4cc1b8ace1ecbcd0747fbbf303d10beb579d67e4b1c"
    }

    def "Verify message signature 1"() {
        setup:
        def message = "test-test-test"
        def address = Address.from("0x9d8A62f656a8d1615C1294fd71e9CFb3E4855A4F")
        def addressWrong = Address.from("0x1c5E6f6F6C7866EF146B0c0220D857D12a9058F0")
        def signature = HexData.from("0xc26a3a1922d97e573db507e82cbace7b57e54106cc96d598d29ac16aabe48153313302cb629b7307baae0ae5e74f68e58564615ccfde0d03603381e1a233e0ed1c")
        when:
        def act = signer.verifyMessageSignature(message, signature, address)
        then:
        act

        when:
        act = signer.verifyMessageSignature(message, signature, addressWrong)
        then:
        !act
    }

    def "Verify message signature 2"() {
        setup:
        def message = "test-test-test 2"
        def address = Address.from("0x9d8A62f656a8d1615C1294fd71e9CFb3E4855A4F")
        def addressWrong = Address.from("0x1c5E6f6F6C7866EF146B0c0220D857D12a9058F0")
        def signature = HexData.from("0x86f13303ffc5c05b3bf500f7f6f8bce9074721ea792e41c9f3624318ee08eebc6c1e4c3f091b2c9611361d462af3103c64a6873918c1aacaf2171bd36615f9f61c")
        when:
        def act = signer.verifyMessageSignature(message, signature, address)
        then:
        act

        when:
        act = signer.verifyMessageSignature(message, signature, addressWrong)
        then:
        !act
    }

}
