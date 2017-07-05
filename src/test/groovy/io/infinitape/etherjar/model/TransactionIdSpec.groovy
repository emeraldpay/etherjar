package io.infinitape.etherjar.model

import spock.lang.Specification

/**
 *
 * @since
 * @author Igor Artamonov
 */
class TransactionIdSpec extends Specification {

    def "Parse tx id"() {
        expect:
        TransactionId.from(hex).toString() == hex.toLowerCase()
        where:
        hex << [
            '0x99d94ccf4f1ad255ba6538ad53c31cf3a9c49065c9b5822533b0abb5af171d82',
            '0xb8b54c779d2eb83b14bd56875c063064937593871658ae559596a25ea5bc0f91',
            '0xf4457d9466b7a445198ca95781032ff46eebeae71578b9f97c8df1caa7ef9b85',
            '0x0f4f762709c13a6d5253c794f77c2a467384023874418ca1df4cd80ffe651236',
            '0xa009852beaafe46df94f28116491f3f63a1c03567b0a85e97494c2fd95a5ac45',
            '0x0000000000000000000000000000000000000000000000000000000000000000',
            '0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
            '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
        ]
    }

    def "Fail for invalid value"() {
        when:
        TransactionId.from([0, 1, 2] as byte[])
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0x')
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0x0')
        then:
        thrown(IllegalArgumentException)

        when:
        TransactionId.from('0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff')
        then:
        thrown(IllegalArgumentException)

    }
}
