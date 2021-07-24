package io.emeraldpay.etherjar.tx

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.hex.Hex32
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import spock.lang.Specification

class TransactionWithAccessSpec extends Specification {

    def "EqualVerify"() {
        expect:
        EqualsVerifier.forClass(TransactionWithAccess)
            .withPrefabValues(
                Address,
                Address.extract(Hex32.extendFrom(1)), Address.extract(Hex32.extendFrom(2))
            )
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .suppress(Warning.STRICT_HASHCODE)
            .verify()
    }
}
