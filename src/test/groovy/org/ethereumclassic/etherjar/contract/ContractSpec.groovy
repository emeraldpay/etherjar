package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Specification

class ContractSpec extends Specification {
    def "check valid method search"() {
        when:
        Contract contract = new Contract(valid_methods as Collection<ContractMethod>)
        then:
        for (ContractMethod method: valid_methods)
            contract.getMethod(method.getId()) == method
        where:
        _ | valid_methods
        _ | [new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])),
             new ContractMethod(new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[])),
             new ContractMethod(new MethodId([0xff, 0xAA, 0x2B, 0x3C] as byte[]))]
        _ | []
    }

    def "check invalid method search"() {
        when:
        Contract contract = new Contract([new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])),
                                          new ContractMethod(new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[]))])
        then:
            contract.getMethod(invalid_methodId as MethodId) == null
        where:
        _ | invalid_methodId
        _ | new MethodId([0xff, 0xff, 0xff, 0xff] as byte[])
        _ | new MethodId([0x11, 0x11, 0x11, 0x11] as byte[])
    }

    def "check null method id"() {
        when:
        Contract contract = new Contract([new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])),
                                          new ContractMethod(new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[]))])
        contract.getMethod(null)
        then:
        thrown(IllegalArgumentException)

    }

    def "check get methods"() {
        when:
        Contract contract = new Contract(valid_methods as Collection<ContractMethod>)
        then:
        contract.getMethods().containsAll(valid_methods)
        true
        where:
        _ | valid_methods
        _ | [new ContractMethod(new MethodId([0xff, 0x11, 0x22, 0x33] as byte[])),
             new ContractMethod(new MethodId([0xff, 0x00, 0xCC, 0x44] as byte[])),
             new ContractMethod(new MethodId([0xff, 0xAA, 0x2B, 0x3C] as byte[]))]
        _ | []
    }
}
