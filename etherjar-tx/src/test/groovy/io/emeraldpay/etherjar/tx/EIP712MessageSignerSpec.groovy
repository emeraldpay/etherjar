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
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class EIP712MessageSignerSpec extends Specification {

    EIP712MessageSigner signer = new EIP712MessageSigner(new Signer(1))

    def "Sign typed data - basic example"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        // Define the domain
        def domain = new EIP712MessageSigner.EIP712Domain(
            "Ether Mail",
            "1",
            1,
            Address.from("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"),
            null
        )

        // Define the types
        def types = [
            "EIP712Domain": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("version", "string"),
                new EIP712MessageSigner.TypedDataField("chainId", "uint256"),
                new EIP712MessageSigner.TypedDataField("verifyingContract", "address")
            ],
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        // Define the message
        def message = [
            "from": [
                "name": "Cow",
                "wallet": Address.from("0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826")
            ],
            "to": [
                "name": "Bob",
                "wallet": Address.from("0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB")
            ],
            "contents": "Hello, Bob!"
        ]

        def typedData = new EIP712MessageSigner.TypedData(types, "Mail", domain, message)

        when:
        def signature = signer.signTypedData(typedData, pk)

        then:
        signature != null
        signature.r != null
        signature.s != null
        signature.v != 0
    }

    def "Sign typed data - encoded signature"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")

        def domain = new EIP712MessageSigner.EIP712Domain(
            "Ether Mail",
            "1",
            1,
            Address.from("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"),
            null
        )

        def types = [
            "EIP712Domain": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("version", "string"),
                new EIP712MessageSigner.TypedDataField("chainId", "uint256"),
                new EIP712MessageSigner.TypedDataField("verifyingContract", "address")
            ],
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        def message = [
            "from": [
                "name": "Cow",
                "wallet": Address.from("0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826")
            ],
            "to": [
                "name": "Bob",
                "wallet": Address.from("0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB")
            ],
            "contents": "Hello, Bob!"
        ]

        def typedData = new EIP712MessageSigner.TypedData(types, "Mail", domain, message)

        when:
        def encodedSignature = signer.signTypedDataEncoded(typedData, pk)

        then:
        encodedSignature != null
        encodedSignature.getSize() >= 65 // 32 + 32 + 1 bytes minimum
    }

    def "Verify typed data signature"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        def signerAddress = Address.from("0x9d8A62f656a8d1615C1294fd71e9CFb3E4855A4F")
        def wrongAddress = Address.from("0x1c5E6f6F6C7866EF146B0c0220D857D12a9058F0")

        def domain = new EIP712MessageSigner.EIP712Domain(
            "Ether Mail",
            "1",
            1,
            Address.from("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"),
            null
        )

        def types = [
            "EIP712Domain": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("version", "string"),
                new EIP712MessageSigner.TypedDataField("chainId", "uint256"),
                new EIP712MessageSigner.TypedDataField("verifyingContract", "address")
            ],
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        def message = [
            "from": [
                "name": "Cow",
                "wallet": Address.from("0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826")
            ],
            "to": [
                "name": "Bob",
                "wallet": Address.from("0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB")
            ],
            "contents": "Hello, Bob!"
        ]

        def typedData = new EIP712MessageSigner.TypedData(types, "Mail", domain, message)
        def encodedSignature = signer.signTypedDataEncoded(typedData, pk)

        when:
        def valid = signer.verifyTypedDataSignature(typedData, encodedSignature, signerAddress)

        then:
        valid

        when:
        def invalid = signer.verifyTypedDataSignature(typedData, encodedSignature, wrongAddress)

        then:
        !invalid
    }

    def "Test encodeType"() {
        setup:
        def types = [
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        when:
        def encoded = signer.encodeType("Mail", types)

        then:
        encoded == "Mail(Person from,Person to,string contents)Person(string name,address wallet)"
    }

    def "Test hashType"() {
        setup:
        def types = [
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        when:
        def hash = signer.hashType("Mail", types)

        then:
        hash != null
        hash.length == 32
    }

    def "Test simple types encoding"() {
        setup:
        def types = [
            "SimpleData": [
                new EIP712MessageSigner.TypedDataField("value", "uint256"),
                new EIP712MessageSigner.TypedDataField("flag", "bool"),
                new EIP712MessageSigner.TypedDataField("addr", "address"),
                new EIP712MessageSigner.TypedDataField("text", "string")
            ]
        ]

        when:
        def uintValue = signer.encodeValue("uint256", BigInteger.valueOf(42), types)
        def boolValue = signer.encodeValue("bool", true, types)
        def addressValue = signer.encodeValue("address", Address.from("0x1234567890123456789012345678901234567890"), types)
        def stringValue = signer.encodeValue("string", "Hello World", types)

        then:
        uintValue.length == 32
        boolValue.length == 32
        addressValue.length == 32
        stringValue.length == 32

        boolValue[31] == (byte) 1
    }

    def "Test domain with salt"() {
        setup:
        PrivateKey pk = PrivateKey.create("0x4646464646464646464646464646464646464646464646464646464646464646")
        byte[] salt = new byte[32]
        Arrays.fill(salt, (byte) 0x42)

        def domain = new EIP712MessageSigner.EIP712Domain(
            "Test Domain",
            "1.0",
            1,
            Address.from("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"),
            salt
        )

        def types = [
            "EIP712Domain": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("version", "string"),
                new EIP712MessageSigner.TypedDataField("chainId", "uint256"),
                new EIP712MessageSigner.TypedDataField("verifyingContract", "address"),
                new EIP712MessageSigner.TypedDataField("salt", "bytes32")
            ],
            "SimpleMessage": [
                new EIP712MessageSigner.TypedDataField("message", "string")
            ]
        ]

        def message = [
            "message": "Hello World"
        ]

        def typedData = new EIP712MessageSigner.TypedData(types, "SimpleMessage", domain, message)

        when:
        def signature = signer.signTypedData(typedData, pk)

        then:
        signature != null
        signature.r != null
        signature.s != null
        signature.v != 0
    }

    def "Test official EIP-712 example"() {
        setup:
        /**
         * This test uses the exact same test data as the official EIP-712 specification example
         * to ensure our implementation produces identical signatures.
         *
         * @see <a href="https://github.com/ethereum/EIPs/blob/master/assets/eip-712/Example.js">Official EIP-712 Example</a>
         */
        // Generate private key from keccak256("cow")
        def cowHash = signer.keccak256("cow".getBytes())
        PrivateKey pk = PrivateKey.create(cowHash)

        // Official example domain
        def domain = new EIP712MessageSigner.EIP712Domain(
            "Ether Mail",
            "1",
            1,
            Address.from("0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"),
            null
        )

        // Official example types
        def types = [
            "EIP712Domain": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("version", "string"),
                new EIP712MessageSigner.TypedDataField("chainId", "uint256"),
                new EIP712MessageSigner.TypedDataField("verifyingContract", "address")
            ],
            "Person": [
                new EIP712MessageSigner.TypedDataField("name", "string"),
                new EIP712MessageSigner.TypedDataField("wallet", "address")
            ],
            "Mail": [
                new EIP712MessageSigner.TypedDataField("from", "Person"),
                new EIP712MessageSigner.TypedDataField("to", "Person"),
                new EIP712MessageSigner.TypedDataField("contents", "string")
            ]
        ]

        // Official example message
        def message = [
            "from": [
                "name": "Cow",
                "wallet": Address.from("0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826")
            ],
            "to": [
                "name": "Bob",
                "wallet": Address.from("0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB")
            ],
            "contents": "Hello, Bob!"
        ]

        def typedData = new EIP712MessageSigner.TypedData(types, "Mail", domain, message)

        when:
        def hashes = signer.hashTypedData(typedData)

        then:
        hashes.domainSeparator == Hex32.from("0xf2cee375fa42b42143804025fc449deafd50cc031ca257e0b194a650a912090f")
        hashes.messageHash == Hex32.from("0xc52c0ee5d84264471806290a3f2c4cecfc5490626bf912d01f240d7a274b371e")

        when:
        def signature = signer.signTypedData(typedData, pk)

        then:
        signature != null
        // Expected signature from the official example
        signature.r.toString(16) == "4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d"
        signature.s.toString(16) == "7299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b91562"
        signature.v == 28

        // Also test the encoded signature and verification
        def encodedSignature = signer.signTypedDataEncoded(typedData, pk)
        encodedSignature.toHex() == "0x4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d07299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b915621c"

        // Verify the signature
        def signerAddress = Address.from("0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826")
        signer.verifyTypedDataSignature(typedData, encodedSignature, signerAddress)
    }

    def "Test Ledger example"() {
        /// Makes sure it matches the Ledger example at https://github.com/emeraldpay/emerald-hwkey
        setup:
        def signerAddress = Address.from("0x3d66483b4Cad3518861029Ff86a387eBc4705172")
        PrivateKey pk = PrivateKey.create("0x2df3f68d81eaa12e73cbae773ad626b527253c5630203426814069cdab58bbce")
        def dataHashes = new EIP712MessageSigner.TypedDataHashes(
            Hex32.from("0x550ec1f194f472d2c84fbf85bcb230090c5f382f5ed7df45a1071a8e2c0a5177"),
            Hex32.from("0x8391eb27569cbd00047175269d5198ac32accfa30488afb2e86aa8b39883e897"),
        )

        when:
        def signature = signer.signTypedData(dataHashes, pk)
        def encodedSignature = signer.encodeSignature(signature)

        then:
        encodedSignature == HexData.from("0x34ad89a26f346e7c488bfb9a1f4078b415a6fe4f0fdca025653b9b833545fb7c4f07ce6e0cfc93f1d5dcae29704955b3b7d6dcdbc8509933a0940b5242c2761c1b")

        when:
        def valid = signer.verifyTypedDataSignature(dataHashes, encodedSignature, signerAddress)

        then:
        valid
    }
}
