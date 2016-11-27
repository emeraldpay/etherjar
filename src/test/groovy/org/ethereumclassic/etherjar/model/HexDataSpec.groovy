package org.ethereumclassic.etherjar.model

import spock.lang.Specification

/**
 * @author Igor Artamonov
 */
class HexDataSpec extends Specification {

    def "Parse hex"() {
        expect:
        HexData.from(hex).bytes == bytes
        HexData.from(hex).toString() == hex.toLowerCase()
        where:
        hex         | bytes
        '0xff'      | [-1] as byte[]
        '0x00'      | [0] as byte[]
        '0x01'      | [1] as byte[]
        '0x0f'      | [15] as byte[]
        '0x0001'    | [0, 1] as byte[]
        '0xff01'    | [-1, 1] as byte[]
        '0x000001'  | [0, 0, 1] as byte[]
        '0x000000'  | [0, 0, 0] as byte[]
        '0xffffff'  | [-1, -1, -1] as byte[]
        '0xABcD'    | [-85, -51] as byte[]
    }

    def "Throw on invalid value"() {
        when:
        HexData.from('')
        then:
        thrown(IllegalArgumentException)

        when:
        new HexData(null as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        HexData.from(null as String)
        then:
        thrown(IllegalArgumentException)

        when:
        HexData.from('0xfake')
        then:
        thrown(IllegalArgumentException)
    }

    def "combine hex data"() {
        setup:
        def params = [
                Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000')
        ] as HexData[]
        when:
        def data = HexData.from(params)
        then:
        data.toHex() == '0x00000000000000000000000000000002200000000000000000000000000000000000000000000000000000000000000880000000000000000000000000000000'
    }

    def "Equal"() {
        setup:
        def x = HexData.from('0x0123456789abcdef')
        when:
        def act = x.equals(HexData.from('0x0123456789abcdef'))
        then:
        act == true

        when:
        act = x.equals(HexData.from('0x0123456789abcdee'))
        then:
        act == false

        when:
        x = HexData.from('0x0')
        act = x.equals(HexData.from('0x0'))
        then:
        act == true
    }

    def "Equal is reflexive"() {
        setup:
        def x = HexData.from('0x0123456789abcdef')
        when:
        def act = x.equals(x)
        then:
        act == true
    }

    def "Equal is symmetric"() {
        setup:
        def x = HexData.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946')
        def y = BlockHash.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946')
        when:
        def act1 = x.equals(y)
        def act2 = y.equals(x)
        then:
        act1 == true
        act2 == true
    }

    def "Equal is transitive"() {
        setup:
        def x = HexData.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946')
        def y = TransactionId.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946')
        def z = BlockHash.from('0x604f7bef716ded3aeea97946652940c0c075bcbb2e6745af042ab1c1ad988946')
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
