package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification

/**
 *
 * @author Igor Artamonov
 */
class MethodTypeSpec extends Specification {

    def "check method signature validity"() {
        expect:
        MethodType.Builder.isSignatureValid(valid_sign as String)
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
        !MethodType.Builder.isSignatureValid(invalid_sign as String)
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
        def m = new MethodType.Builder().fromFullName(method).build()
        exp == m.id.toHex()
        where:
        exp             | method
        '0xcdcd77c0'    | 'baz(uint32,bool)'
        '0xab55044d'    | 'bar(fixed128x128[2])'
        '0x8be65246'    | 'f(uint256,uint32[],bytes10,bytes)'
    }

    def "encode call"() {
        setup:
        def m = new MethodType.Builder().fromFullName('bar(fixed128x128[2])').build()
        def params = [
                Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000')
        ] as Hex32[]
        when:
        def act = m.encodeCall(params)
        then:
        act.toHex() == '0xab55044d00000000000000000000000000000002200000000000000000000000000000000000000000000000000000000000000880000000000000000000000000000000'
    }

}
