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

import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class SignatureEIP155Spec extends Specification {

    def "valid recId for ETC"() {
        SignatureEIP155 signature = new SignatureEIP155(61)
        when:
        signature.setV(0x9d)
        def recid = signature.getRecId()

        then:
        recid == 0

        when:
        signature.setV(0x9e)
        recid = signature.getRecId()

        then:
        recid == 1

        when:
        signature.setV(0x1b)
        recid = signature.getRecId()

        then:
        recid == 0

        when:
        signature.setV(0x1c)
        recid = signature.getRecId()

        then:
        recid == 1
    }

    def "extracts chain id for ETC"() {
        expect:
        SignatureEIP155.extractChainId(0x9d) == 61
        SignatureEIP155.extractChainId(0x9e) == 61
    }

    def "extracts chain id for ETH"() {
        expect:
        SignatureEIP155.extractChainId(0x26) == 1
        SignatureEIP155.extractChainId(0x27) == 2
    }

    def "extract addresses from tx in 7100000 in ETC"() {
        expect:
        Signature signature = new SignatureEIP155(
                61,
                Hex.decodeHex(message),
                v,
                new BigInteger(r, 16),
                new BigInteger(s, 16)
        )

        signature.recoverAddress().toHex() == address

        where:

        address | message | v | r | s
        "0x9eab4b0fc468a7f5d46228bf5a76cb52370d068d" | "ee83527ea74d7b08cca67de5f2adfe7bf371c5e6dbcf7e851db83cfa27e50afb" | 0x9d | "271658e49edd3495771f43734d29fac87cac9e740bf4c60a5847a6606ea8b38e" | "209874432a43edb6376afe704940b78521058fdbc7c85e55c6f5b2280c2b4942"
        "0x9eab4b0fc468a7f5d46228bf5a76cb52370d068d" | "f62d6fe04f09b01a1e9090d772fb789a8f73fe508d6dc205c0d33810d420e441" | 0x9e | "5f29a5cd9959cb7c18518b10fde774d441e48617fecda2b6bfc9199892e1f2b7" | "556149ff3dfc5031feaffc8a57e9eecc6de1a0371997f58bfd2136fbbdea91c5"
        "0x9eab4b0fc468a7f5d46228bf5a76cb52370d068d" | "f483a0dfe2c9863c2efe42d6be637e08c044f3d09b16088df41d0f4a9979bf66" | 0x9d | "27bfaca3696507f8cdf674494ec2663d50348427489955bc28aba04cd650b393" | "3ecada232648309dd2bf7aac1759e88bd2494e47b854950eecde4270076a2430"
    }


    def "extract addresses from tx in 7100000 in ETH"() {
        expect:
        Signature signature = new SignatureEIP155(
                1,
                Hex.decodeHex(message),
                v,
                new BigInteger(r, 16),
                new BigInteger(s, 16)
        )

        signature.recoverAddress().toHex() == address

        where:

        address | message | v | r | s
        "0x52bc44d5378309ee2abf1539bf71de1b7d7be3b5" | "455916da827a0f1a68e90b846e8408472cc73eab4204553beba2c299544e6f74" | 0x26 | "cc124793dfde7cffe7a1fcbfa20c417141ca2aa4a92cb53ff6ae6a7f7ed4eb5a" | "682efafaeb065673f4fd7e9ed989c1762b8d4a470055eeff1824ca3fd64db8ee"
        "0x52bc44d5378309ee2abf1539bf71de1b7d7be3b5" | "928255af54c2d8339a16917757f75d93918f74bdab2f25e40430a853f79a3423" | 0x26 | "b3f13916a129bf0d6834534d9219475cf2e80b0707afd3ccf1fcb06d90c9a1d3" | "1a18178efe212d12bf5dc33f9f75778ac4d526fe918352d6626c4fca1efdf72c"
        "0x52bc44d5378309ee2abf1539bf71de1b7d7be3b5" | "c9ac20c3116943489a2be2c2a14d117ba1c6dd441285fe872ebd90d0f71dcfa3" | 0x25 | "bbe86585313eb25981e14fd4e7148c02b3b36081f554cffa3e7e47009995a31e" | "1287e151a0995e9aae7abaf163bd032b2ec6cad8db72f09853b1cb5a25932231"
        "0x564286362092d8e7936f0549571a803b203aaced" | "b75a5cb333ca776d8aea44f2c82d19243ead096bf5f64669613e9aba20eade15" | 0x26 | "a755b0b141fc114d5c357dc0e19408b5aa6800d0ada71632a43b37244f5ce9c2" | "b81505d3317b49c550fda63efb5bf8778829cc2fcb3c1eb45db3ba823b6700"
     }

}

