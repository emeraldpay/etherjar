package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

import java.util.function.Function

class TypeSpec extends Specification {

    def "should find an appropriate type"() {
        def type = Stub(Type)

        def mock = Mock(Function) {
            0 * apply(_ as String)
        }

        Type.Repository repo = { -> [{ Optional.of type } as Function, mock] }

        when:
        def opt = repo.search "abc123"

        then:
        opt.present
        opt.get() == type
    }

    def "should not find a type"() {
        Type.Repository repo = { -> [{ Optional.empty() } as Function] }

        when:
        def opt = repo.search "abc321"

        then:
        !opt.present
    }
}
