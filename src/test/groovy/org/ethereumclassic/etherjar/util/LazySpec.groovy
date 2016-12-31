package org.ethereumclassic.etherjar.util

import spock.lang.Specification

import java.util.function.Supplier

class LazySpec extends Specification {

    def "should memoize a supplier value"() {
        def suppl = Mock(Supplier)

        when:
        def lazy = Lazy.wrap suppl

        then:
        0 * suppl.get()

        when:
        def val1 = lazy.get()
        def val2 = lazy.get()
        def val3 = lazy.get()

        then:
        1 * suppl.get() >> 123

        and:
        val1 == 123
        val1.is val2
        val2.is val3
    }

    def "should detect null supplier"() {
        when:
        Lazy.wrap null

        then:
        thrown NullPointerException
    }

    def "should detect null supplier value"() {
        def suppl = { null } as Supplier

        when:
        Lazy.wrap(suppl).get()

        then:
        thrown NullPointerException
    }
}
