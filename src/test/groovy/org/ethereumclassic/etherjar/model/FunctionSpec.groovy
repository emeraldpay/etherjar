package org.ethereumclassic.etherjar.model

import spock.lang.Specification

class FunctionSpec extends Specification {

    def "Parse function"() {
        expect:
        Function.from(addr).toString() == addr
        Function.from(addr).bytes == bytes
        where:
        addr                                                    | bytes
        '0xfffffffff3984f569b4c7ff5143499d94abe2ff201020304'      | [-1, -1, -1, -1, -13, -104, 79, 86, -101, 76, 127, -11, 20, 52, -103, -39, 74, -66, 47,-14, 1, 2, 3, 4] as byte[]
        '0x000000000000000000000000000000000000000000000000'    | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] as byte[]
        '0xffffffffffffffffffffffffffffffffffffffffffffffff'    | [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1] as byte[]
    }

    def "Ignore Invalid function"() {
        when:
        Function.from('0x0')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from(null as String)
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from(null as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0xfake')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe12345678')
        then:
        thrown(IllegalArgumentException)

        when:
        Function.from('0x0000000000015b23c7e20b0ea5ebd84c39dcbe607012345678')
        then:
        thrown(IllegalArgumentException)
    }
}
