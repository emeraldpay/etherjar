package io.emeraldpay.etherjar.abi

import io.emeraldpay.etherjar.hex.HexData
import spock.lang.Specification

class DynamicArrayPartTypeSpec extends Specification {

    def "Parse first part of data"() {
        setup:
        def data = HexData.from("0x" +
            "0000000000000000000000000000000000000000000000000000000000000002" +
            "00000000000000000000000000000000000000000000000000000000000000f1" +
            "00000000000000000000000000000000000000000000000000000000000000f2" +
            "0000000000000000000000000000000000000000000000000000000000000001" +
            "00000000000000000000000000000000000000000000000000000000000000f3"
        )
        when:
        def act = new DynamicArrayPartType(new IntType()).decode(data)
        then:
        act.size() == 2
        act[0] == BigInteger.valueOf(0xf1)
        act[1] == BigInteger.valueOf(0xf2)
    }
}
