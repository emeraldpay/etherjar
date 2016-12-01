package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.contract.type.Type
import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Igor Artamonov
 */
class ContractMethodSpec extends Specification {

    @Shared ContractMethod method

    def setup() {
        def t1 = [
                getName: { 'fixed128x128' },
                isDynamic: { false },
                getBytesFixedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000') ] as Hex32[] }
        ] as Type

        def t2 = [
                getName: { 'fixed128x128' },
                isDynamic: { false },
                getBytesFixedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000') ] as Hex32[] }
        ] as Type

        method = new ContractMethod('bar', t1 ,t2)
    }

    def "should check method signature validity"() {
        expect:
        ContractMethod.Builder.isAbiValid valid_sign

        where:
        _ | valid_sign
        _ | 'baz()'
        _ | 'baz(uint32)'
        _ | 'baz(uint32,bool)'
        _ | 'bar(fixed128x128[2])'
        _ | 'f123(uint256,uint32[],bytes10,bytes)'
    }

    def "should check method signature invalidity"() {
        expect:
        !ContractMethod.Builder.isAbiValid(invalid_sign)

        where:
        _ | invalid_sign
        _ | 'baz(uint32, bool)'
        _ | 'bar(fixed128x128[2]'
        _ | '1f(uint256,uint32[],bytes10,bytes)'
    }

    @Ignore
    def "should copy contract method"() {
        def obj = ContractMethod.Builder.fromAbi(method.toAbi()).build()

        expect:
        obj == method
    }

    def "should catch null ABIs"() {
        when:
        ContractMethod.Builder.fromAbi abi

        then:
        thrown NullPointerException

        where:
        _ | abi
        _ | null
    }

    def "should catch invalid ABIs"() {
        when:
        ContractMethod.Builder.fromAbi abi

        then:
        thrown(IllegalArgumentException)

        where:
        _ | params
        _ | [] as Object[]
        _ | [1] as Object[]
        _ | [1, 2, 3] as Object[]
    }

    def "should be converted to ABI string representation"() {
        def str = method.toAbi()

        expect:
        str == 'bar(fixed128x128,fixed128x128)'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first   | second
        method  | new ContractMethod.Builder().withName('bar').expects(method.inputTypes).build()
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first   | second
        method  | method
        method  | new ContractMethod.Builder().withName('bar').expects(method.inputTypes).build()
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first   | second
        method  | null
        method  | new ContractMethod('baz', method.inputTypes)
        method  | new Contract(Address.EMPTY)
    }

    def "should be converted to a string representation"() {
        def str = method as String

        expect:
        str ==~ /ContractMethod\{.+}/
        str.contains "id=${method.id}"
        str.contains "name=${method.name}"
        str.contains "isConstant=${method.constant}"
        str.contains "expects=${method.inputTypes}"
        str.contains "returns=${method.outputTypes}"
    }
}
