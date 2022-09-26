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
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class TransactionDecoderSpec extends Specification {

    TransactionDecoder decoder = new TransactionDecoder()

    def "Parse 0x19442f"() {
        // id: 0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31
        setup:
        def tx = Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.nonce == 15524
        act.gasPrice.toHex() == "0x59b9b95f0"
        act.gas == 0x03d090
        act.to.toHex() == "0x8b3b3b624c3c0397d3da8fd861512393d51dcbac"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x667a2f58"
        act.signature.getType() == SignatureType.LEGACY
        act.signature instanceof Signature
        !(act.signature instanceof SignatureEIP155)
        act.signature != null
        act.signature.v == 28
        act.signature.r.toString(16) == "d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64"
        act.signature.s.toString(16) == "39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"

        act.signature.recoverAddress().toHex() == "0xed059bc543141c8c93031d545079b3da0233b27f"
        act.transactionId().toHex() == "0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31"
    }

    def "Parse goerli 0x6d4d85"() {
        // 0x6d4d85482c59b6fe2f416996c802ceae2e30b9fe6bc27fe5c72d2fa9b1b2e28b
        setup:
        def tx = Hex.decodeHex("f8cb82afdc843b9aca008303d090947ef66b77759e12caf3ddb3e4aff524e577c59d8d80b864e9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb08641ba01e8a3bacc31fc91ade73278d0267b70d38b53623ab0a28d1e20e133286f8a85ca02f2e0ac2c4e9e4410804fa62c9cba70e18eed8b93ec2a2ad5762279de8288b63")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.nonce == 45020
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI)
        act.gas == 250000
        act.to.toHex() == "0x7ef66b77759e12caf3ddb3e4aff524e577c59d8d"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0xe9c6c1760000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000043c636a65ea4943acfacb227680b6ba20c477ba24ef87049a4a5b3958385e215bb0864"
        act.signature != null
        act.signature.getType() == SignatureType.LEGACY
        act.signature.recoverAddress() == Address.from("0x79047abf3af2a1061b108d71d6dc7bdb06474790")
        act.transactionId().toHex() == "0x6d4d85482c59b6fe2f416996c802ceae2e30b9fe6bc27fe5c72d2fa9b1b2e28b"
    }

    def "Parse goerli 0x8d8367"() {
        // 0x8d8367acc4c8f17fa6b9e8a856833b3406b200f96a5fa98018128411b1f0c6d1
        setup:
        def tx = Hex.decodeHex("f86c8227b2843b9aca008275309413ac1a2c6d1a4efc492a40d8f9d4e9f14b7c726887b1a2bc2ec50000001ca077313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23a079c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.nonce == 10162
        act.gasPrice == Wei.ofUnits(1, Wei.Unit.GWEI)
        act.gas == 30000
        act.to.toHex() == "0x13ac1a2c6d1a4efc492a40d8f9d4e9f14b7c7268"
        act.value == Wei.ofEthers(0.05)
        act.data.toHex() == "0x00"
        act.signature != null
        act.signature.getType() == SignatureType.LEGACY
        act.signature.v == 28
        act.signature.r.toString(16) == "77313351aaa29a277e3cf015c354542e042b00c4757e1ac70fdbc9b1d0341c23"
        act.signature.s.toString(16) == "79c62b0c278676c590afd0f8bcfc4654b5babb99a5883baefc539acd55ee0365"

        act.signature.recoverAddress().toHex() == "0x8ced5ad0d8da4ec211c17355ed3dbfec4cf0e5b9"
        act.transactionId().toHex() == "0x8d8367acc4c8f17fa6b9e8a856833b3406b200f96a5fa98018128411b1f0c6d1"
    }

    def "Parse tx with Access List"() {
        setup:
        def tx = Hex.decodeHex("01f8cb01808504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f85bf85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a0000000000000000000000000000000000000000000000000000000000000000701a038c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255a03acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.ACCESS_LIST
        act instanceof TransactionWithAccess
        act.nonce == 0
        act.gasPrice == Wei.ofUnits(20, Wei.Unit.GWEI)
        act.gas == 150_000
        act.to.toHex() == "0x3535353535353535353535353535353535353535"
        act.value == Wei.ofEthers(1)
        act.data.toHex() == "0x"
        act.signature != null
        with((TransactionWithAccess)act) {
            chainId == 1
            accessList.size() == 1
            accessList[0] == new TransactionWithAccess.Access(
                Address.from("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"),
                Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000003"),
                Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000007")
            )
        }
        act.signature.getType() == SignatureType.EIP2930
        act.signature instanceof SignatureEIP2930
        with((SignatureEIP2930)act.signature) {
            YParity == 1
            r.toString(16) == "38c8eb279a4b6c4b806258389e1b5906b28418e3eff9e0fc81173f54fa37a255"
            s.toString(16) == "3acaa2b6d5e4edb561b918b4cb49cf1dbae9972ca90df7af6364598353a2c125"
        }
        act.signature.recoverAddress().toHex() == "0x9d8a62f656a8d1615c1294fd71e9cfb3e4855a4f"
    }

    def "Parse tx with Access List 2"() {
        setup:
        def tx = Hex.decodeHex("01f8e201018504a817c800830249f0943535353535353535353535353535353535353535880de0b6b3a764000080f872f85994de0b295669a9fd93d5f28d9ec85e40f4cb697baef842a00000000000000000000000000000000000000000000000000000000000000003a00000000000000000000000000000000000000000000000000000000000000007d694bb9bc244d798123fde783fcc1c72d3bb8c189413c080a0b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7aa0431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.ACCESS_LIST
        act instanceof TransactionWithAccess
        act.nonce == 1
        act.gasPrice == Wei.ofUnits(20, Wei.Unit.GWEI)
        act.gas == 150_000
        act.to.toHex() == "0x3535353535353535353535353535353535353535"
        act.value == Wei.ofEthers(1)
        act.data.toHex() == "0x"
        act.signature != null
        with((TransactionWithAccess)act) {
            chainId == 1
            accessList.size() == 2
            accessList[0] == new TransactionWithAccess.Access(
                Address.from("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"),
                Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000003"),
                Hex32.from("0x0000000000000000000000000000000000000000000000000000000000000007")
            )
            accessList[1] == new TransactionWithAccess.Access(
                Address.from("0xbb9bc244d798123fde783fcc1c72d3bb8c189413")
            )
        }
        act.signature.getType() == SignatureType.EIP2930
        act.signature instanceof SignatureEIP2930
        with((SignatureEIP2930)act.signature) {
            YParity == 0
            r.toString(16) == "b935047bf9b8464afec5bda917281610b2aaabd8de4b01d2eba6e876c934ca7a"
            s.toString(16) == "431b406eb13aefca05a0320c3595700b9375df6fac8cc8ec5603ac2e42af4894"
        }
        act.signature.recoverAddress().toHex() == "0x9d8a62f656a8d1615c1294fd71e9cfb3e4855a4f"
    }

    def "Parse tx with gas priority same as max - 0x26acb4b"() {
        // EIP-1559 tx
        // 0x26acb4b776574c2610c82d0a846d54a993a16f0cf5018a32c7860e0e60dd8255
        setup:
        def tx = Hex.decodeHex("02f87401038509524eafc38509524eafc382520894e993226e3ebd2852c9ee9efab6a0e3260be0cb0688359768e80ed7c9ee80c001a06f1aafac255225b837f5dcfdfd7e2180c53acf23d44edf67cebb3cf62b872dcfa001eb68b217fd9c007ab3b51996b5d2d05f0c335e674700c39617ed155fdde6df")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.GAS_PRIORITY
        act instanceof TransactionWithGasPriority
        with((TransactionWithGasPriority)act) {
            nonce == 3
            maxGasPrice == new Wei(40035594179)
            priorityGasPrice == new Wei(40035594179)
            gas == 21_000
            to.toHex() == "0xe993226e3ebd2852c9ee9efab6a0e3260be0cb06"
            value == Wei.from("0x359768e80ed7c9ee")
            data.toHex() == "0x"
            signature != null
            chainId == 1
            accessList.size() == 0
            signature.getType() == SignatureType.EIP2930
            signature instanceof SignatureEIP2930
            with((SignatureEIP2930)signature) {
                YParity == 1
                r.toString(16) == "6f1aafac255225b837f5dcfdfd7e2180c53acf23d44edf67cebb3cf62b872dcf"
                s.toString(16) == "1eb68b217fd9c007ab3b51996b5d2d05f0c335e674700c39617ed155fdde6df"
            }
            act.signature.recoverAddress().toHex() == "0xfac40888ed4b06e7b832e0a6460d7fa2065d1a28"
        }
        act.transactionId().toHex() == "0x26acb4b776574c2610c82d0a846d54a993a16f0cf5018a32c7860e0e60dd8255"
    }

    def "Parse tx with gas priority - 0xe2c9ad"() {
        // EIP-1559 tx
        // 0xe2c9ad4b92dfdea74203f83c503b769525ada75b9a53745f70113f23c077162c
        setup:
        def tx = Hex.decodeHex("02f8b101819684ee6b280085134062da9b82c79d947bebd226154e865954a87650faefa8f485d3608180b844095ea7b300000000000000000000000003f7724180aa6b939894b5ca4314783b0b36b329ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc001a0d978ed98e78dd480b2aec86d1521962a8fe4009e44fb19f45b70d8005e602182a0347c933f78131995c1abd07c1d0be67d8f04c2cf99cd79510657e97ead8c1a9f")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.GAS_PRIORITY
        act instanceof TransactionWithGasPriority
        with((TransactionWithGasPriority)act) {
            nonce == 150
            maxGasPrice == new Wei(82684598939)
            priorityGasPrice == new Wei(4000000000)
            gas == 51_101
            to.toHex() == "0x7bebd226154e865954a87650faefa8f485d36081"
            value == Wei.ZERO
            data.toHex() == "0x095ea7b300000000000000000000000003f7724180aa6b939894b5ca4314783b0b36b329ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
            signature != null
            chainId == 1
            accessList.size() == 0
            signature.getType() == SignatureType.EIP2930
            signature instanceof SignatureEIP2930
            with((SignatureEIP2930)signature) {
                YParity == 1
                r.toString(16) == "d978ed98e78dd480b2aec86d1521962a8fe4009e44fb19f45b70d8005e602182"
                s.toString(16) == "347c933f78131995c1abd07c1d0be67d8f04c2cf99cd79510657e97ead8c1a9f"
            }
            act.signature.recoverAddress().toHex() == "0xcf85118573955817f86795fc68feed6937d61064"
        }
        act.transactionId().toHex() == "0xe2c9ad4b92dfdea74203f83c503b769525ada75b9a53745f70113f23c077162c"
    }

    def "Parse tx with high V"() {
        // Matic TX 0x6fe439fa7b6f3b4883aa48f85018405e3ae61de3ad72aec614db69bebbd522b5
        setup:
        def tx = Hex.decodeHex("f86c01844190ab0082947094cf281b9d76894627e54234604ef26d35f33860c887482a88e5d2489080820135a0813bbf0d2e686a6c82ce5726d8ec11ba1df0d5b401bf271d7a08ada9cad008dda0759ce1bbc912667e56dbf2ccd35ca06843265393430daa7437c447dfe3ad7dc1")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.signature != null
        act.signature.getType() == SignatureType.EIP155
        act.signature instanceof SignatureEIP155
        with((SignatureEIP155)act.signature) {
            v == 0x135
            r.toString(16) == "813bbf0d2e686a6c82ce5726d8ec11ba1df0d5b401bf271d7a08ada9cad008dd"
            s.toString(16) == "759ce1bbc912667e56dbf2ccd35ca06843265393430daa7437c447dfe3ad7dc1"

            recoverAddress().toHex() == "0x7a3972dce76a4089898e12e0606eba38766f3106"
        }
        act.transactionId().toHex() == "0x6fe439fa7b6f3b4883aa48f85018405e3ae61de3ad72aec614db69bebbd522b5"
    }

    def "Parse with large chainId - arbitrum 0x4a97c3"() {
        // 0x4a97c3e7ccff3c12fd7872cb0e1eb66861937dde8128f27f5d52f993b67b4d4a
        setup:
        def tx = Hex.decodeHex("f9018f8263aa840bebc8cd830c3500945d8e854f0fe0d55c4dbc32d5f3ec0734f2da002680b9012405615a750000000000000000000000000000000000000000000000001b5c8a19bb24c800000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000bbfc327f00000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000002000000000000000000000000ff970a61a04b1ca14834a43f5de4533ebddb5cc800000000000000000000000082af49447d8a07e3bd95bd0d56f35241523fbab1000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000001f483014986a0fa891149bf52113d95ab98a8e39bbd97b7c48c2a440f6e46418ac4ee2686606ea062b6f1c3fd2cada8badb19968c9c33790ca6df643468e8fde6ed4833ef2467ad")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.nonce == 0x63aa
        act.gasPrice == Wei.from("0xbebc8cd") // 200001741
        act.gas == 0xc3500
        act.to.toHex() == "0x5d8e854f0fe0d55c4dbc32d5f3ec0734f2da0026"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x05615a750000000000000000000000000000000000000000000000001b5c8a19bb24c800000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000bbfc327f00000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000002000000000000000000000000ff970a61a04b1ca14834a43f5de4533ebddb5cc800000000000000000000000082af49447d8a07e3bd95bd0d56f35241523fbab1000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000001f4"
        act.signature != null
        act.signature.getType() == SignatureType.EIP155
        with((SignatureEIP155)act.signature) {
            chainId == 42161
            recoverAddress() == Address.from("0xe61ca074cd20f94dcc3b74e7df02ba5a4987f1fa")
        }
        act.transactionId().toHex() == "0x4a97c3e7ccff3c12fd7872cb0e1eb66861937dde8128f27f5d52f993b67b4d4a"
    }

    def "Parse with large chainId - arbitrum 0x8a6a7e"() {
        // 0x8a6a7e1adf9c900b64dc284152f419a63acfcd94c1d881449d1912f4d150de47
        setup:
        def tx = Hex.decodeHex("f8af82078d8502540be400830c350094888f4408ab925bd9511ea8440121186519fb496380b84490b98a11000000000000000000000000bde60cdd192734665f21d3c2e2712bc65cef45d7000000000000000000000000000000000000000000000028a857425466f8000083014985a0d21ea4286eadd0ae9be4636e0ea029d4c894ddc2a7b714f846c24ff2a0b6fa59a040f358a0ce0c5ca75ebe011a21ccf2c873255222fdfba31aff785ff62e7a36ce")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.getType() == TransactionType.STANDARD
        act.nonce == 0x78d
        act.gasPrice == Wei.from("0x2540be400")
        act.gas == 0xc3500
        act.to.toHex() == "0x888f4408ab925bd9511ea8440121186519fb4963"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x90b98a11000000000000000000000000bde60cdd192734665f21d3c2e2712bc65cef45d7000000000000000000000000000000000000000000000028a857425466f80000"
        act.signature != null
        act.signature.getType() == SignatureType.EIP155
        with((SignatureEIP155)act.signature) {
            chainId == 42161
            getV() == 0x14985
            r.toString(16) == "d21ea4286eadd0ae9be4636e0ea029d4c894ddc2a7b714f846c24ff2a0b6fa59"
            s.toString(16) == "40f358a0ce0c5ca75ebe011a21ccf2c873255222fdfba31aff785ff62e7a36ce"
            recoverAddress() == Address.from("0x8a14f2ec98cc1d4a144903550308d838ee6ec3c9")
        }
        act.transactionId().toHex() == "0x8a6a7e1adf9c900b64dc284152f419a63acfcd94c1d881449d1912f4d150de47"
    }

    def "Parse with large chainId - ethw 0x190340"() {
        // 0x190340bd96279d4f337a16b319d76c517315ba7c04331781787344a579ab1543
        setup:
        def tx = Hex.decodeHex("f86e808502540be40082520894fd87622db43ca9e135180b32c2e4ee8e445f9af8880ddff7b537fce00080824e45a0bc44ec7d7dfe10a46b51e48b8681ba43aa6a6f3dbc08a14e71225014709c181ea01dbcedda6890a31e425ce7c315666984a47de6954343cfaef0c8c394958f17b1")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.transactionId().toHex() == "0x190340bd96279d4f337a16b319d76c517315ba7c04331781787344a579ab1543"
        act.getType() == TransactionType.STANDARD
        act.nonce == 0x0
        act.gasPrice == Wei.from("0x2540be400")
        act.gas == 0x5208
        act.to.toHex() == "0xfd87622db43ca9e135180b32c2e4ee8e445f9af8"
        act.value.toHex() == "0xddff7b537fce000"
        act.data.toHex() == "0x"
        act.signature != null
        act.signature.getType() == SignatureType.EIP155
        with((SignatureEIP155)act.signature) {
            chainId == 10001
            recoverAddress() == Address.from("0xec4eae283f68056e43dc8bbf8ba6f6464db65ab0")
        }
    }

    def "Parse with large chainId type 2 - ethw 0xda4d4d type 2"() {
        // 0xda4d4d6f87437d05f7988e4bdfa5d8fa2136a3084cbe4a3e3a832b18beff021a
        setup:
        def tx = Hex.decodeHex("02f8b18227110c8477359400847735940082c7c7942ad7868ca212135c6119fd7ad1ce51cfc570289280b844095ea7b30000000000000000000000004f381d5ff61ad1d0ec355fed2ac4000ea1e67854ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc080a06db5c4c3c07bc4f992599edc62537e772b0ba7bf8e6cb2995876868cc577cb2aa0624bef1817d59d1fb653a4972111f6585378057228e4e223d94a72d9593b456e")
        when:
        def act = decoder.decode(tx)
        act.signature.message = act.hash()

        then:
        act.transactionId().toHex() == "0xda4d4d6f87437d05f7988e4bdfa5d8fa2136a3084cbe4a3e3a832b18beff021a"
        act.getType() == TransactionType.GAS_PRIORITY
        act.nonce == 0xc
        act.gasPrice == Wei.from("0x77359400")
        act.gas == 0xc7c7
        act.to.toHex() == "0x2ad7868ca212135c6119fd7ad1ce51cfc5702892"
        act.value.toHex() == "0x0"
        act.data.toHex() == "0x095ea7b30000000000000000000000004f381d5ff61ad1d0ec355fed2ac4000ea1e67854ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        act.signature != null
        act.signature.getType() == SignatureType.EIP2930
        with((SignatureEIP2930)act.signature) {
            chainId == 10001
            recoverAddress() == Address.from("0x72e96f7033c40cacc2fd554836846c2258f39909")
        }
    }
}
