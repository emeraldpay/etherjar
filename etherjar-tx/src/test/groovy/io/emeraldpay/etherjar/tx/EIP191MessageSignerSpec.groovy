/*
 * Copyright (c) 2025 EmeraldPay Ltd, All Rights Reserved.
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
import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class EIP191MessageSignerSpec extends Specification {

    EIP191MessageSigner signer = new EIP191MessageSigner(new Signer(1))

    def "Sign message 1"() {
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

    def "Sign message with byte array"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        def message = "test-test-test".getBytes()

        when:
        def signature = signer.signMessage(message, pk)

        then:
        signature != null
        signature.r != null
        signature.s != null
        signature.v != 0
    }

    def "Verify message signature with byte array"() {
        setup:
        def message = "test-test-test".getBytes()
        def address = Address.from("0x9d8A62f656a8d1615C1294fd71e9CFb3E4855A4F")
        def signature = HexData.from("0xc26a3a1922d97e573db507e82cbace7b57e54106cc96d598d29ac16aabe48153313302cb629b7307baae0ae5e74f68e58564615ccfde0d03603381e1a233e0ed1c")
        
        when:
        def act = signer.verifyMessageSignature(message, signature, address)
        
        then:
        act
    }
}