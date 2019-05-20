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
package io.infinitape.etherjar.rlp

import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

import java.nio.ByteBuffer

class RlpReaderSpec extends Specification {

    // https://github.com/ethereum/wiki/wiki/RLP#rlp-decoding
    def "Official examples - bytes"() {
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

        when:
        act = new RlpReader(Hex.decodeHex("80"))
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

        when:
        act = new RlpReader(Hex.decodeHex("00"))
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


        when:
        act = new RlpReader(Hex.decodeHex("0f"))
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

        when:
        act = new RlpReader(Hex.decodeHex("820400"))
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

        when:
        byte[] str = "Lorem ipsum dolor sit amet, consectetur adipisicing elit".getBytes()
        def buf = ByteBuffer.allocate(2 + str.length).put(Hex.decodeHex("b838")).put(str)
        act = new RlpReader(buf.array())
        then:
        act.hasNext()
        act.getType() == RlpType.BYTES
        act.nextString() == "Lorem ipsum dolor sit amet, consectetur adipisicing elit"
        !act.hasNext()
        act.consumed
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

}
