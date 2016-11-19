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

    def "check method signature validity"() {
        expect:
        MethodId.isSignatureValid(valid_sign as String)
        where:
        _ | valid_sign
        _ | 'baz()'
        _ | 'baz(uint32)'
        _ | 'baz(uint32,bool)'
        _ | 'bar(fixed128x128[2])'
        _ | 'f123(uint256,uint32[],bytes10,bytes)'
    }

    def "check method signature invalidity"() {
        expect:
        !MethodId.isSignatureValid(invalid_sign as String)
        where:
        _ | invalid_sign
        _ | 'baz(uint32,,bool)'
        _ | 'baz(uint32,bool,)'
        _ | 'baz(uint32, bool)'
        _ | 'bar(fixed128x128[2]'
        _ | '1f(uint256,uint32[],bytes10,bytes)'
    }

    def "prepare method id"() {
        expect:
        id == MethodId.fromSignature(method).toHex()
        where:
        id           | method
        '0xcdcd77c0' | 'baz(uint32,bool)'
        '0xab55044d' | 'bar(fixed128x128[2])'
        '0x8be65246' | 'f(uint256,uint32[],bytes10,bytes)'
    }
}
