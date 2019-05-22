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

import io.infinitape.etherjar.domain.Address
import io.infinitape.etherjar.hex.HexData
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

import java.nio.ByteBuffer

class RlpWriterSpec extends Specification {

    def "Official examples - bytes - dog"() {
        when:
        def wrt = new RlpWriter()
        wrt.write("dog")
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "83646f67"
    }

    def "Official examples - list - cat, dog"() {
        when:
        def wrt = new RlpWriter()
        def act = wrt.startList()
            .write("cat")
            .write("dog")
            .closeList()
            .toByteArray()
        then:
        Hex.encodeHexString(act) == "c88363617483646f67"
    }

    def "Official examples - bytes - empty string"() {
        when:
        def wrt = new RlpWriter()
        wrt.write((String)null)
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "80"

        when:
        wrt = new RlpWriter()
        wrt.write("")
        act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "80"
    }

    def "Official examples - bytes - integer 0"() {
        when:
        def wrt = new RlpWriter()
        wrt.write(0)
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "80"

        when:
        wrt = new RlpWriter()
        wrt.write(0L)
        act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "80"
    }

    def "Official examples - bytes - integer 15"() {
        when:
        def wrt = new RlpWriter()
        wrt.write(15)
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "0f"

        when:
        wrt = new RlpWriter()
        wrt.write(15L)
        act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "0f"
    }

    def "Official examples - bytes - integer 1024"() {
        when:
        def wrt = new RlpWriter()
        wrt.write(1024)
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "820400"

        when:
        wrt = new RlpWriter()
        wrt.write(1024L)
        act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act)  == "820400"
    }

    def "Official examples - tree"() {
        when:
        def wrt = new RlpWriter()
        // [ [], [[]], [ [], [[]] ] ]
        def act = wrt.startList()
            .startList().closeList() // []
            .startList().startList().closeList().closeList() // [[]]
            .startList() // [
                .startList().closeList() // []
                .startList().startList().closeList().closeList() // [[]]
            .closeList() // ]
        .closeList().toByteArray()

        then:
        Hex.encodeHexString(act)  == "c7c0c1c0c3c0c1c0"
    }

    def "Official examples - lorem ipsum"() {
        when:
        def wrt = new RlpWriter()
        wrt.write("Lorem ipsum dolor sit amet, consectetur adipisicing elit")
        def act = wrt.toByteArray()
        then:
        Hex.encodeHexString(act) == "b838" + "4c 6f 72 65 6d 20 69 70 73 75 6d 20 64 6f 6c 6f 72 20 73 69 74 20 61 6d 65 74 2c 20 63 6f 6e 73 65 63 74 65 74 75 72 20 61 64 69 70 69 73 69 63 69 6e 67 20 65 6c 69 74".replaceAll(" ", "")
    }

    def "Write transaction 0x19442f"() {
        when:
        def wrt = new RlpWriter()
        def act = wrt.startList()
                .write(15524) //nonce
                .write(new BigInteger("59b9b95f0", 16)) //gas price
                .write(0x03d090) // gas
                .write(Address.from("0x8b3b3b624c3c0397d3da8fd861512393d51dcbac").bytes) // to
                .write(0) // value
                .write(HexData.from("0x667a2f58").bytes) //data
                .write(28) // v
                .write(new BigInteger("d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64", 16)) //r
                .write(new BigInteger("39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b", 16)) //s
            .closeList()
            .toByteArray()

        then:
        Hex.encodeHexString(act) == "f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"
    }

    def "Write transaction 0x19442f - not chained call"() {
        when:
        def wrt = new RlpWriter()
        wrt.startList()
        wrt.write(15524) //nonce
        wrt.write(new BigInteger("59b9b95f0", 16)) //gas price
        wrt.write(0x03d090) // gas
        wrt.write(Address.from("0x8b3b3b624c3c0397d3da8fd861512393d51dcbac").bytes) // to
        wrt.write(0) // value
        wrt.write(HexData.from("0x667a2f58").bytes) //data
        wrt.write(28) // v
        wrt.write(new BigInteger("d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64", 16)) //r
        wrt.write(new BigInteger("39837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b", 16)) //s
        wrt.closeList()
        def act = wrt.toByteArray()

        then:
        Hex.encodeHexString(act) == "f86b823ca485059b9b95f08303d090948b3b3b624c3c0397d3da8fd861512393d51dcbac8084667a2f581ca0d7ddf1368fa81f6092ec15734000f911501af11876ef908a418f015030503a64a039837b1d2ee9c8ee011f44407927b540df893884eef98f67b164c8cafb82061b"
    }
}
