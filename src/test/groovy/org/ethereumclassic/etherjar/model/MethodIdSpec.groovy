package org.ethereumclassic.etherjar.model

import spock.lang.Specification

class MethodIdSpec extends Specification {
    def "check invalid bytes"() {
        when:
        new MethodId(invalid_bytes as byte[])
        then:
        thrown(IllegalArgumentException)
        where:
        _ | invalid_bytes
        _ | [0x00]
        _ | []
        _ | [0x00, 0x01, 0x00, 0x00, 0xFF]
    }

    def "check from invalid bytes"() {
        when:
        MethodId.from(invalid_bytes as byte[])
        then:
        thrown(IllegalArgumentException)
        where:
        _ | invalid_bytes
        _ | null
        _ | [0x00]
        _ | []
        _ | [0x00, 0x01, 0x00, 0x00, 0xFF]
    }

    def "check from invalid string"() {
        when:
        MethodId.from(invalid_string as String)
        then:
        thrown(IllegalArgumentException)
        where:
        _ | invalid_string
        _ | null
        _ | "123"
        _ | "-850932852093457982375"
    }

    def "check from valid bytes"() {
        when:
        MethodId id = MethodId.from(valid_bytes as byte[])
        then:
        id.getBytes() == valid_bytes
        where:
        _ | valid_bytes
        _ | [(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00]
        _ | [(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x0F]
        _ | [(byte)0xFF, (byte)0xaa, (byte)0xcc, (byte)0x1c]
        _ | [(byte)0x13, (byte)0x3b, (byte)0xf2, (byte)0x2F]
    }

    def "check from valid string"() {
        when:
        MethodId id = MethodId.from(valid_string as String)
        then:
        id.toString()equalsIgnoreCase(valid_string)
        where:
        _ | valid_string
        _ | "0x11223344"
        _ | "0x00000000"
        _ | "0xABCDEFFF"
        _ | "0x12345678"
    }
}
