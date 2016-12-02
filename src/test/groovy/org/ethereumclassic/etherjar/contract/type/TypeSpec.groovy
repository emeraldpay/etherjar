package org.ethereumclassic.etherjar.contract.type

import spock.lang.Specification

class TypeSpec extends Specification {

    def "should find an appropriate type"() {
        def res = Stub(Type)

        def type1 = Stub(Type) {
            parse("abc123") >> Optional.of(res)
        }

        def type2 = Mock(Type) {
            0 * parse(_ as String)
        }

        Type.Repository repo = { -> [type1, type2] }

        when:
        def opt = repo.search "abc123"

        then:
        opt.present
        opt.get() == res
    }

    def "should not find a type"() {
        def type = Stub(Type) {
            parse(_ as String) >> Optional.empty()
        }

        Type.Repository repo = { -> [type] }

        when:
        def opt = repo.search "abc321"

        then:
        !opt.present
    }
}
