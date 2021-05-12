package io.emeraldpay.etherjar.rpc.json

import io.emeraldpay.etherjar.hex.Hex32
import spock.lang.Specification

class TransactionLogJsonSpec extends Specification {

    def "Equals with topics values"() {
        setup:
        // val1 and val2 topics are both lists, but different implementations
        // must be equal anyway

        def val1 = new TransactionLogJson().tap {
            it.setTopics(new ArrayList([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2),
                Hex32.extendFrom(3)
            ]))
        }

        def val2 = new TransactionLogJson().tap {
            it.setTopics(new Vector().tap {it.addAll([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2),
                Hex32.extendFrom(3)
            ])})
        }

        def val3 = new TransactionLogJson().tap {
            it.setTopics([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2),
                Hex32.extendFrom(4)
            ])
        }

        def val4 = new TransactionLogJson().tap {
            it.setTopics([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2)
            ])
        }

        when:
        def equals11 = val1.equals(val1)
        def equals12 = val1.equals(val2)
        def equals13 = val1.equals(val3)
        def equals14 = val1.equals(val4)

        def equals21 = val2.equals(val1)
        def equals22 = val2.equals(val2)
        def equals23 = val2.equals(val3)
        def equals24 = val2.equals(val4)

        def equals31 = val3.equals(val1)
        def equals32 = val3.equals(val2)
        def equals33 = val3.equals(val3)
        def equals34 = val3.equals(val4)

        then:
        equals11
        equals12
        !equals13
        !equals14

        equals21
        equals22
        !equals23
        !equals24

        !equals31
        !equals32
        equals33
        !equals34
    }
}
