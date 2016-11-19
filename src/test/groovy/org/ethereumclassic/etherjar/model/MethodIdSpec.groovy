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
}
