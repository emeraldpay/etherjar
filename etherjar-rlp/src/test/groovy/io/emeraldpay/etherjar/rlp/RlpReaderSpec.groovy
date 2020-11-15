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
package io.emeraldpay.etherjar.rlp

import io.emeraldpay.etherjar.domain.Address
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

import java.nio.ByteBuffer

class RlpReaderSpec extends Specification {

    def "Official examples - bytes - dog"() {
        when:
        // [0x83, 'd', 'o', 'g']
        def act = new RlpReader(Hex.decodeHex("83646f67"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        new String(act.next()) == 'dog'
        !act.hasNext()
        act.consumed

        when:
        // [0x83, 'd', 'o', 'g']
        act = new RlpReader(Hex.decodeHex("83646f67"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextString() == 'dog'
        !act.hasNext()
        act.consumed
    }

    def "Official examples - bytes - empty string"() {
        when:
        def act = new RlpReader(Hex.decodeHex("80"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.next() == new byte[0]
        !act.hasNext()
        act.consumed

        when:
        act = new RlpReader(Hex.decodeHex("80"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextString() == ''
        !act.hasNext()
        act.consumed
    }

    def "Official examples - bytes - integer 0"() {
        when:
        def act = new RlpReader(Hex.decodeHex("00"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextLong() == 0
        !act.hasNext()
        act.consumed

        when:
        act = new RlpReader(Hex.decodeHex("00"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextInt() == 0
        !act.hasNext()
        act.consumed
    }

    def "Official examples - bytes - integer 15"() {
        when:
        def act = new RlpReader(Hex.decodeHex("0f"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextLong() == 15
        !act.hasNext()
        act.consumed

        when:
        act = new RlpReader(Hex.decodeHex("0f"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextInt() == 15
        !act.hasNext()
        act.consumed
    }

    def "Official examples - bytes - integer 1024"() {
        when:
        def act = new RlpReader(Hex.decodeHex("820400"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextLong() == 1024
        !act.hasNext()
        act.consumed

        when:
        act = new RlpReader(Hex.decodeHex("820400"))
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextInt() == 1024
        !act.hasNext()
        act.consumed
    }

    def "Official examples - bytes - lorem ipsum"() {
        when:
        byte[] str = "Lorem ipsum dolor sit amet, consectetur adipisicing elit".getBytes()
        def buf = ByteBuffer.allocate(2 + str.length).put(Hex.decodeHex("b838")).put(str)
        def act = new RlpReader(buf.array())
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextString() == "Lorem ipsum dolor sit amet, consectetur adipisicing elit"
        !act.hasNext()
        act.consumed
    }

    def "Official examples - list - cat, dog"() {
        when:
        def act = new RlpReader(Hex.decodeHex("c88363617483646f67"))
        then:
        act.hasNext()
        act.type == RlpType.LIST

        when:
        act = act.nextList()

        then:
        act.hasNext()
        act.nextString() == "cat"
        act.hasNext()
        act.nextString() == "dog"
        !act.hasNext()
    }

    def "Official examples - list"() {
        when:
        // list = [ [], [[]], [ [], [[]] ] ]
        // list 1 = []
        // list 2 = [[]]
        // list 21 = []
        // list 3 = [ [], [[]] ]
        // list 31 = []
        // list 32 = [[]]
        // list 321 = []
        def act = new RlpReader(Hex.decodeHex("c7c0c1c0c3c0c1c0"))
        then:
        act.hasNext()
        act.type == RlpType.LIST

        when:
        def list = act.nextList()
        then:
        list.hasNext()
        list.type == RlpType.LIST

        when:
        def list1  = list.nextList()

        then:
        !list1.hasNext()
        list1.consumed
        //but has seconds
        list.hasNext()
        list.type == RlpType.LIST

        when:
        def list2 = list.nextList()
        then:
        list2.hasNext()
        list2.type == RlpType.LIST

        when:
        def list21 = list2.nextList()

        then:
        !list21.hasNext()
        list21.consumed
        list2.consumed
        list.hasNext()
        list.type == RlpType.LIST

        when:
        def list3 = list.nextList()

        then:
        list3.hasNext()
        list3.type == RlpType.LIST

        when:
        def list31 = list3.nextList()

        then:
        !list31.hasNext()
        list3.hasNext()
        list3.type == RlpType.LIST

        when:
        def list32 = list3.nextList()

        then:
        list32.hasNext()
        list32.type == RlpType.LIST

        when:
        def list321 = list32.nextList()

        then:
        !list321.hasNext()
        !list32.hasNext()
        !list3.hasNext()
        !list.hasNext()
    }

    def "Read transaction 0x19442f"() {
        setup:
        def tx = Hex.decodeHex("f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b")
        when:
        def rdr = new RlpReader(tx)

        then:
        rdr.hasNext()
        rdr.type == RlpType.LIST

        when:
        rdr = rdr.nextList()

        then:
        //nonce
        rdr.type == RlpType.BYTES
        rdr.nextLong() == 15524
        //gasprice
        rdr.type == RlpType.BYTES
        rdr.nextBigInt().toString(16) == "59b9b95f0"
        //gas
        rdr.nextLong() == 0x03d090
        rdr.type == RlpType.BYTES
        //to
        rdr.type == RlpType.BYTES
        Address.from(rdr.next()).toHex() == "0x8b3b3b624c3c0397d3da8fd861512393d51dcbac"
        //value
        rdr.type == RlpType.BYTES
        rdr.nextBigInt().toString() == "0"
        //data
        rdr.type == RlpType.BYTES
        rdr.nextBigInt().toString(16) == "667a2f58"
        //v
        rdr.hasNext()
        rdr.type == RlpType.BYTES
        rdr.nextInt() == 28
        //r
        rdr.hasNext()
        rdr.type == RlpType.BYTES
        rdr.nextBigInt().toString(16) == "d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64"
        //s
        rdr.hasNext()
        rdr.type == RlpType.BYTES
        rdr.nextBigInt().toString(16) == "39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"

        !rdr.hasNext()
        rdr.consumed
    }

}
