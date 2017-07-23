package io.infinitape.etherjar.util

import spock.lang.Specification

class HexEncodingSpec extends Specification {

    def "should hex encode & decode integer values"() {
        expect:
        HexEncoding.toNakedHex(val) == naked
        HexEncoding.toFullHex(val) == full

        and:
        HexEncoding.fromHex(naked) == val
        HexEncoding.fromHex(full) == val

        where:
        val                 | naked             | full
        0G                  | '0'               | '0x00'
        4180G               | '1054'            | '0x1054'
        1659284G            | '195194'          | '0x195194'
        81985529216486895G  | '123456789abcdef' | '0x0123456789abcdef'
    }

    def "should detect wrong hex-encoding strings"() {
        when:
        HexEncoding.fromHex hex

        then:
        thrown NumberFormatException

        where:
        _ | hex
        _ | ''
        _ | '_'
        _ | 'x'
        _ | '0x'
        _ | 'xyz'
    }
}
