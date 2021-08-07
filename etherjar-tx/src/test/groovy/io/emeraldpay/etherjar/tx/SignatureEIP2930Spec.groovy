package io.emeraldpay.etherjar.tx

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import spock.lang.Specification

class SignatureEIP2930Spec extends Specification {

    def "correct type"() {
        when:
        Signature signature = new SignatureEIP2930()
        then:
        signature.getType() == SignatureType.EIP2930
    }

    def "EqualVerify"() {
        expect:
        EqualsVerifier.forClass(SignatureEIP2930)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .suppress(Warning.STRICT_HASHCODE)
            .verify()
    }

}
