package org.ethereumclassic.etherjar.model

import spock.lang.Specification

class MethodIdSpec extends Specification {

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
}
