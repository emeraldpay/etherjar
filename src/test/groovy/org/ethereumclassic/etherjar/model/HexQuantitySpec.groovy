package org.ethereumclassic.etherjar.model

import spock.lang.Specification

/**
 *
 * @author Igor Artamonov
 */
class HexQuantitySpec extends Specification {

    def "Equal"() {
        setup:
        def x = HexQuantity.from('0x01054')
        def y = HexQuantity.from('0x01054')
        when:
        def act = x.equals(y)
        then:
        act == true
    }

    def "Not Equal"() {
        setup:
        def x = HexQuantity.from('0x01054')
        def y = HexQuantity.from('0x01055')
        when:
        def act = x.equals(y)
        then:
        act == false
    }

    def "Equal is reflexive"() {
        setup:
        def x = HexQuantity.from('0x0123456789abcdef')
        when:
        def act = x.equals(x)
        then:
        act == true
    }

    def "Equal is symmetric"() {
        setup:
        def x = HexQuantity.from('0x0195194')
        def y = Wei.from('0x0195194')
        when:
        def act1 = x.equals(y)
        def act2 = y.equals(x)
        then:
        act1 == true
        act2 == true
    }

    def "Equal is transitive"() {
        setup:
        def x = HexQuantity.from('0x0195194')
        def y = Wei.from('0x0195194')
        def z = HexQuantity.from('0x0195194')
        when:
        def act1 = x.equals(y)
        def act2 = y.equals(x)
        def act3 = x.equals(z)
        then:
        act1 == true
        act2 == true
        act3 == true
    }
}
