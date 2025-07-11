/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
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
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class SignatureSpec extends Specification {

    def "extract pubkey from 0x19442f"() {
        setup:
        Signature signature = new Signature()
        signature.message = Hex.decodeHex("383caae49692ae021fb2189933518ca58fd04d88e99b41a4d18f5ae5fb5f52aa")
        signature.v = 28
        signature.r = new BigInteger("d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64", 16)
        signature.s = new BigInteger("39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b", 16)

        when:
        def addr = signature.recoverAddress()

        then:
        addr.toHex() == "0xed059bc543141c8c93031d545079b3da0233b27f"
    }

    def "extract addresses from tx in 1920000"() {
        expect:
        Signature signature = new Signature(
            Hex.decodeHex(message),
            v,
            new BigInteger(r, 16),
            new BigInteger(s, 16)
        )

        signature.recoverAddress().toHex() == address

        where:

        address | message | v | r | s
        "0x6ebeb2af2e734fbba2b58c5b922628af442527ce" | "65b1b2531608e9d9715d2bbdaff33d1a53696f59c092e40dfc236600b9984d7d" | 27 | "8d94a55c7ac7adbfa2285ef7f4b0c955ae1a02647452cd4ead03ee6f449675c6" | "67149821b74208176d78fc4dffbe37c8b64eecfd47532406b9727c4ae8eb7c9a"
        "0xee62a6740b3069781fc0ed138e94dcaa89f8eb05" | "043346c6f9636456e874e0872937d852098383ad7529ad822157acd5b1ab6e0d" | 28 | "6d31e3d59bfea97a34103d8ce767a8fe7a79b8e2f30af1e918df53f9e78e69ab" | "98e5b80e1cc436421aa54eb17e96b08fe80d28a2fbd46451b56f2bca7a321e7"
        "0x57ec8ef62a9af59b9fbbc6d7dba05516558f5018" | "cf6c9ff75cdb0493ab8c63012d2e91ae5fa197729bd982f3a4f63edf1eb3f729" | 27 | "fdbbc462a8a60ac3d8b13ee236b45af9b7991cf4f0f556d3af46aa5aeca242ab" | "5de5dc03fdcb6cf6d14609dbe6f5ba4300b8ff917c7d190325d9ea2144a7a2fb"
        "0x80a103beced8a6854a7a82ac2d48cdab0eb21cc0" | "13031c845812fd5a7ad55038efc5f410dfd90331a369fba92b50d311d487ab5c" | 27 | "bafb9f71cef873b9e0395b9ed89aac4f2a752e2a4b88ba3c9b6c1fea254eae73" | "1cef688f6718932f7705d9c1f0dd5a8aad9ddb196b826775f6e5703fdb997706"
    }

    def "fromEncoded extracts signature from 65 bytes"() {
        setup:
        def encodedHex = "0x4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d07299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b915621c"
        def encoded = HexData.from(encodedHex)
        
        when:
        def signatureFromHex = Signature.fromEncoded(encoded)
        def signatureFromBytes = Signature.fromEncoded(encoded.getBytes())
        
        then:
        signatureFromHex.r.toString(16) == "4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d"
        signatureFromHex.s.toString(16) == "7299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b91562"
        signatureFromHex.v == 28
        signatureFromHex.message == null
        
        signatureFromBytes.r.toString(16) == "4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d"
        signatureFromBytes.s.toString(16) == "7299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b91562"
        signatureFromBytes.v == 28
        signatureFromBytes.message == null
    }

    def "EqualVerify"() {
        expect:
        EqualsVerifier.forClass(Signature)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .suppress(Warning.STRICT_HASHCODE)
            .verify()
    }
}
