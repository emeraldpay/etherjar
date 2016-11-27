package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.contract.type.Type
import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.MethodId
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

        method = new ContractMethod(MethodId.fromAbi('bar', 'fixed128x128', 'fixed128x128'), t1 ,t2)
    }

    def "should check method signature validity"() {
        expect:
        ContractMethod.Builder.isSignatureValid valid_sign

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
        !ContractMethod.Builder.isSignatureValid(invalid_sign)

        where:
        _ | invalid_sign
        _ | 'baz(uint32,,bool)'
        _ | 'baz(uint32,bool,)'
        _ | 'baz(uint32, bool)'
        _ | 'bar(fixed128x128[2]'
        _ | '1f(uint256,uint32[],bytes10,bytes)'
    }

    def "should rebuild similar contract method"() {
        def obj = new ContractMethod.Builder()
                .withName('bar').expects(method.getInputTypes()).build()

        expect:
        obj == method
    }

    def "should throw illegal state exception for empty builder"() {
        when:
        new ContractMethod.Builder().build()

        then:
        thrown IllegalStateException
    }

    def "should be steady for external modifications"() {
        def coll = new ArrayList(method.inputTypes)
        def obj = new ContractMethod(MethodId.fromAbi('bar', 'fixed128x128[2]'), coll)

        when:
        coll.clear()

        then:
        obj.inputTypes.size() == method.inputTypes.size()
    }

    def "should be created correctly"() {
        expect:
        method.id == MethodId.fromAbi('bar', 'fixed128x128', 'fixed128x128')
        !method.constant
        method.inputTypes.size() == 2
        method.outputTypes.isEmpty()
    }

    def "should check the returned input types collection for immutability"() {
        def coll = method.inputTypes

        when:
        coll.clear()

        then:
        thrown UnsupportedOperationException
    }

    def "should check the returned output types collection for immutability"() {
        def coll = method.outputTypes

        when:
        coll.clear()

        then:
        thrown UnsupportedOperationException
    }

    def "should encode call"() {
        when:
        def hex = method.encodeCall([1, 2] as Object[]).toHex()

        then:
        hex.startsWith(method.getId().toHex())
        hex.contains '00000000000000000000000000000002200000000000000000000000000000000000000000000000000000000000000880000000000000000000000000000000'
    }

    def "should encode call with wrong parameters number"() {
        when:
        method.encodeCall params

        then:
        thrown IllegalArgumentException

        where:
        _ | params
        _ | [] as Object[]
        _ | [1] as Object[]
        _ | [1, 2, 3] as Object[]
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        new ContractMethod(method.id)   | new ContractMethod(method.id)
        method                          | new ContractMethod.Builder().withName('bar').expects(method.getInputTypes()).build()
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                           | second
        method                          | method
        new ContractMethod(method.id)   | new ContractMethod(method.id)
        method                          | new ContractMethod.Builder().withName('bar').expects(method.getInputTypes()).build()
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first       | second
        method      | null
        method      | new ContractMethod(MethodId.fromAbi('bar', 'fixed128x128[2]'))
    }

    def "should be converted to a string representation"() {
        def str = method as String

        expect:
        str ==~ /ContractMethod\{.+}/
        str.contains "id=${method.id}"
        str.contains "isConstant=${method.constant}"
        str.contains "expects=${method.inputTypes}"
        str.contains "returns=${method.outputTypes}"
    }
}
