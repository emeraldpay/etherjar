package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Shared
import spock.lang.Specification

class ContractSpec extends Specification {

    @Shared Contract contract

    def setup() {
        contract = new Contract(Address.EMPTY,
                new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])),
                new ContractMethod(new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[])),
                new ContractMethod(new MethodId([0xff, 0xAA, 0x2B, 0x3C] as byte[])))
    }

    def "should be created using builder"() {
        setup:
        def other = new Contract.Builder()
                .at(contract.address).withMethods(contract.methods).build()

        expect:
        contract == other
    }

    def "should be steady for external modifications"() {
        setup:
        def coll = [ new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])) ]
        def other = new Contract(contract.address, coll)

        when:
        coll.clear()

        then:
        other.methods.size() == 1
    }

    def "should find a contract method by a signature id"() {
        when:
        def opt = contract.findMethod id

        then:
        opt.present

        and:
        opt.get().id == id

        where:
        _ | id
        _ | new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])
        _ | new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[])
        _ | new MethodId([0xff, 0xAA, 0x2B, 0x3C] as byte[])
    }

    def "should catch a non-existent contract method signatures id"() {
        expect:
        !contract.findMethod(id).present

        where:
        _ | id
        _ | new MethodId([0xff, 0xff, 0xff, 0xff] as byte[])
        _ | new MethodId([0x11, 0x11, 0x11, 0x11] as byte[])
    }

    def "should catch null method signature ids"() {
        when:
        contract.findMethod null

        then:
        thrown NullPointerException
    }

    def "should get the contract methods collection"() {
        when:
        def coll = contract.methods

        then:
        coll.size() == 3

        and:
        coll.stream().anyMatch { it.id == id }

        where:
        _ | id
        _ | new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])
        _ | new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[])
        _ | new MethodId([0xff, 0xAA, 0x2B, 0x3C] as byte[])
    }

    def "should check the contract methods collection for immutability"() {
        setup:
        def coll = contract.methods

        when:
        coll.clear()

        then:
        thrown(UnsupportedOperationException)
    }

    def "should calculate consistent hashcode"() {
        expect:
        fisrt.hashCode() == second.hashCode()

        where:
        fisrt    | second
        contract | new Contract(contract.address, contract.methods)
    }

    def "should be equal"() {
        expect:
        fisrt == second

        where:
        fisrt    | second
        contract | new Contract(contract.address, contract.methods)
    }

    def "should be converted to a string representation"() {
        setup:
        def str = contract.toString()

        expect:
        str.startsWith "Contract!"

        and:
        str.contains "address=0x0000000000000000000000000000000000000000"

        and:
        str.contains "methods=" + contract.methods
    }
}
