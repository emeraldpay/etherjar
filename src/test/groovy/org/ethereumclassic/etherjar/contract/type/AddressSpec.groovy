package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Specification


class AddressSpec extends Specification {
    def "check name"() {
        when:
        Address adress = new Address()

        then:
        adress.getName() == "address"
    }

    def "check fixed size"() {
        when:
        Address adress = new Address();

        then:
        adress.getBytesFixedSize() == 20
    }

    def "check is dynamic"() {
        when:
        Address adress = new Address()

        then:
        adress.isDynamic() == false;
    }

    def "check encode"() {
        setup:
        Address address = new Address()
        BigInteger par = new BigInteger(str, 16);

        expect:
        address.encode(par)[0].toString() == encoded

        where:
        _ | str                                         | encoded
        _ | "+112233445566778899aabbccddeeff1122334455" | "0x000000000000000000000000112233445566778899aabbccddeeff1122334455"
        _ | "+ff000000000000000000000000000000000000ff" | "0x000000000000000000000000ff000000000000000000000000000000000000ff"
    }

    def "check decode"() {
        setup:
        Address adress = new Address()
        Hex32[] par = [new Hex32(array as byte[])]
        BigInteger decoded = new BigInteger(array as byte[])

        expect:
        adress.decode(par) == decoded

        where:
        _ | array
        _ | [0x11, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
             0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
             0xff, 0xff, 0xff, 0xff, 0x00, 0x00, 0x00, 0x00,
             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]
        _ | [0x11, 0xff, 0xff, 0x00, 0x00, 0xff, 0xff, 0xff,
             0xff, 0xff, 0x00, 0x00, 0x33, 0xff, 0xff, 0xff,
             0xff, 0xff, 0xff, 0xff, 0x00, 0x00, 0x00, 0x00,
             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]
    }
}