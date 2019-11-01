package io.infinitape.etherjar.rpc

import io.infinitape.etherjar.domain.Address
import io.infinitape.etherjar.domain.Wei
import spock.lang.Specification

class RpcCallResponseSpec extends Specification {

    def "Cast to same object"() {
        setup:
        def val = new RpcCallResponse(Commands.net().version(), 1)
        when:
        def act = val.cast(Integer)
        then:
        act.value == 1
    }

    def "Cast to base object"() {
        setup:
        def val = new RpcCallResponse(Commands.net().version(), 1)
        when:
        def act = val.cast(Number)
        then:
        act.value == 1

        when:
        val = new RpcCallResponse(Commands.eth().getBalance(Address.EMPTY, 100), Wei.ZERO)
        act = val.cast(Serializable)
        then:
        act.value == Wei.ZERO
    }

    def "Null cast to anything"() {
        setup:
        def val = new RpcCallResponse(Commands.net().version(), (Integer) null)
        when:
        def act = val.cast(Void)
        then:
        act.value == null
    }

    def "Doesn't cast to different class"() {
        setup:
        def val = new RpcCallResponse(Commands.net().version(), 1)
        when:
        val.cast(Boolean)
        then:
        thrown(ClassCastException)
    }
}
