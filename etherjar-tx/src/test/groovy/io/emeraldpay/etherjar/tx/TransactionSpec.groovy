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
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.Hex32
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Func
import nl.jqno.equalsverifier.Warning
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class TransactionSpec extends Specification {

    TransactionDecoder decoder = new TransactionDecoder()

    def "Unsigned hash"() {
        setup:
        def tx = decoder.decode(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def hash = tx.hash()
        then:
        Hex.encodeHexString(hash) == "383caae49692ae021fb2189933518ca58fd04d88e99b41a4d18f5ae5fb5f52aa"
    }

    def "Get transaction id"() {
        setup:
        def tx = decoder.decode(
                Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        )
        when:
        def txid = tx.transactionId()
        then:
        txid.toString() == "0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31"
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


    def "EqualVerify"() {
        expect:
        EqualsVerifier.forClass(Transaction)
            .withPrefabValues(
                Address,
                Address.extract(Hex32.extendFrom(1)), Address.extract(Hex32.extendFrom(2))
            )
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .suppress(Warning.STRICT_HASHCODE)
            .verify()
    }
}
