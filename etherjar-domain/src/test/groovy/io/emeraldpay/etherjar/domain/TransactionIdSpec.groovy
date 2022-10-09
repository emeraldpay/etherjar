/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.domain

import spock.lang.Specification

class TransactionIdSpec extends Specification {

    def "Parse tx id"() {
        expect:
        TransactionId.from(hex).toString() == hex.toLowerCase()
        where:
        hex << [
            '0x99d94ccf4f1ad255ba6538ad53c31cf3a9c49065c9b5822533b0abb5af171d82',
            '0xb8b54c779d2eb83b14bd56875c063064937593871658ae559596a25ea5bc0f91',
            '0xf4457d9466b7a445198ca95781032ff46eebeae71578b9f97c8df1caa7ef9b85',
            '0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236',
            '0xa009852beaafe46df94f28116491f3f63a1c03567b0a85e97494c2fd95a5ac45',
            '0x0000000000000000000000000000000000000000000000000000000000000000',
            '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
            '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
        ]
    }

    def "Fail for invalid value"() {
        when:
        TransactionId.from([0, 1, 2] as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0x')
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0x0')
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff')
        then:
        thrown(IllegalArgumentException)
    }

    def "Equals"() {
        when:
        def act = TransactionId.from("0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236")
            .equals(TransactionId.from("0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236"))
        then:
        act

        when:
        act = TransactionId.empty().equals(TransactionId.empty())
        then:
        act

        when:
        act = TransactionId.empty().equals(TransactionId.from("0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236"))
        then:
        !act

        when:
        act = TransactionId.from("0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236").equals(TransactionId.empty())
        then:
        !act

        when:
        act = TransactionId.from("0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236")
            .equals(TransactionId.from("0x77c2a467384023874418ca1df4cd80ffe6512360f4f762709c13a6d5253c794f"))
        then:
        !act
    }

    def "Order"() {
        when:
        def tx_99 = TransactionId.from('0x99d94ccf4f1ad255ba6538ad53c31cf3a9c49065c9b5822533b0abb5af171d82')
        def tx_b8 = TransactionId.from('0xb8b54c779d2eb83b14bd56875c063064937593871658ae559596a25ea5bc0f91')
        def tx_f4 = TransactionId.from('0xf4457d9466b7a445198ca95781032ff46eebeae71578b9f97c8df1caa7ef9b85')
        def tx_0f = TransactionId.from('0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236')
        def tx_a0 = TransactionId.from('0xa009852beaafe46df94f28116491f3f63a1c03567b0a85e97494c2fd95a5ac45')
        def tx_00 = TransactionId.from('0x0000000000000000000000000000000000000000000000000000000000000000')
        def tx_ff = TransactionId.from('0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff')

        then:
        tx_00 < tx_ff

        tx_00 < tx_99

        tx_99 < tx_b8
        tx_b8 < tx_f4
        tx_f4 > tx_0f
        tx_a0 > tx_00
        tx_a0 > tx_99
        tx_ff > tx_f4
    }

    def "Sort"() {
        when:
        def txes = [
            '0x99d94ccf4f1ad255ba6538ad53c31cf3a9c49065c9b5822533b0abb5af171d82',
            '0xb8b54c779d2eb83b14bd56875c063064937593871658ae559596a25ea5bc0f91',
            '0xf4457d9466b7a445198ca95781032ff46eebeae71578b9f97c8df1caa7ef9b85',
            '0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236',
            '0xa009852beaafe46df94f28116491f3f63a1c03567b0a85e97494c2fd95a5ac45',
            '0x0000000000000000000000000000000000000000000000000000000000000000',
            '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
            '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
        ].collect { TransactionId.from(it) }
        Collections.sort(txes)

        then:
        txes.collect { it.toHex() } == [
            '0x0000000000000000000000000000000000000000000000000000000000000000',
            '0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236',
            '0x99d94ccf4f1ad255ba6538ad53c31cf3a9c49065c9b5822533b0abb5af171d82',
            '0xa009852beaafe46df94f28116491f3f63a1c03567b0a85e97494c2fd95a5ac45',
            '0xb8b54c779d2eb83b14bd56875c063064937593871658ae559596a25ea5bc0f91',
            '0xf4457d9466b7a445198ca95781032ff46eebeae71578b9f97c8df1caa7ef9b85',
            '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
            '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        ]
    }
}
