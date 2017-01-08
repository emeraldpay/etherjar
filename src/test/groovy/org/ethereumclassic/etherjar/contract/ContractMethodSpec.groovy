package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.contract.type.Type
import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.HexData
import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

/**
 * @author Igor Artamonov
 */
class ContractMethodSpec extends Specification {

    @Shared ContractMethod method

    def setup() {
        def t1 = [
                getCanonicalName: { 'fixed128x128[2]' },
                isDynamic: { false },
                getFixedSize: { Hex32.SIZE_BYTES * 2 },
                encode: { HexData.combine(
                        Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                        Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000'),
                ) },
        ] as Type

        def t2 = [
                getCanonicalName: { 'address' },
                isDynamic: { false },
                getFixedSize: { Hex32.SIZE_BYTES },
                encode: { Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000000') },
        ] as Type

        method = new ContractMethod.Builder()
                .withName('bar').withInputTypes(t1).withOutputTypes(t2).build()

        assert method.id == MethodId.fromSignature('bar', 'fixed128x128[2]')
        assert method.name == 'bar'
        assert !method.constant
        assert method.inputTypes == [t1] as ContractParametersTypes
        assert method.outputTypes == [t2] as ContractParametersTypes
    }

    def "should check method signature validity"() {
        expect:
        ContractMethod.isAbiValid valid_sign

        where:
        _ | valid_sign
        _ | 'baz()'
        _ | 'baz(uint32)'
        _ | 'baz(uint32,bool)'
        _ | 'baz(uint32):(bool)'
        _ | '_bar(fixed128x128[2])'
        _ | '_bar(fixed128x128[2]):(address)'
        _ | 'f123(uint256,uint32[],bytes10,bytes)'
    }

    def "should check method signature invalidity"() {
        expect:
        !ContractMethod.isAbiValid(invalid_sign)

        where:
        _ | invalid_sign
        _ | 'baz(uint32, bool)'
        _ | 'barfixed128x128[2])'
        _ | 'bar(fixed128x128[2]'
        _ | 'bar(fixed128x128[2]:(bool)'
        _ | 'bar(fixed128x128[2]):bool)'
        _ | 'bar(fixed128x128[2]):(bool'
        _ | 'bar(fixed128x128[2])(bool)'
        _ | '1f(uint256,uint32[],bytes10,bytes)'
    }

    def "should copy contract method"() {
        def parser = Stub(Function) {
            apply(_) >>> method.inputTypes.types.collect { Optional.of it }
        }

        def obj = ContractMethod.fromAbi({ -> [parser] }, method.toAbi())

        expect:
        obj == method
    }

    def "should catch wrong ABI method signature"() {
        when:
        ContractMethod.fromAbi({ -> [] }, abi)

        then:
        thrown IllegalArgumentException

        where:
        _ | abi
        _ | 'baz(a, b)'
        _ | 'bara,b)'
        _ | 'bar(a,b'
    }

    def "should catch null ABIs"() {
        when:
        ContractMethod.fromAbi({ -> [] }, abi)

        then:
        thrown NullPointerException

        where:
        _ | abi
        _ | null
    }

    def "should rebuild similar contract method"() {
        def obj = new ContractMethod.Builder().withName('bar')
                .withInputTypes(method.inputTypes)
                .withOutputTypes(method.outputTypes)
                .build()

        expect:
        obj == method
    }

    def "should build constant contract method"() {
        def obj = new ContractMethod.Builder()
                .withName('bar').asConstant()
                .withInputTypes(method.inputTypes)
                .withOutputTypes(method.outputTypes)
                .build()

        expect:
        obj.id == method.id
        obj.constant
        obj.outputTypes == method.outputTypes
        obj.inputTypes == method.inputTypes
    }

    def "should throw illegal state exception for empty builder"() {
        when:
        new ContractMethod.Builder().build()

        then:
        thrown IllegalStateException
    }

    def "should create correctly without constant flag"() {
        when:
        def obj = new ContractMethod(method.name)

        then:
        !obj.constant
    }

    def "should create correctly without input and output types"() {
        when:
        def obj = new ContractMethod(method.name, method.constant, ContractParametersTypes.EMPTY)

        then:
        !obj.constant
        obj.inputTypes.isEmpty()
        obj.outputTypes.isEmpty()
    }

    def "should encode contract method call"() {
        def args = [[BigDecimal.valueOf(2.125), BigDecimal.valueOf(8.5)]]

        def data = HexData.combine(
                Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000'))

        when:
        def enc = method.encodeCall(args as Object[])

        then:
        enc.size == MethodId.SIZE_BYTES + Hex32.SIZE_BYTES * 2
        enc.extract(MethodId.SIZE_BYTES).toHex() == '0xab55044d'
        enc.extract(Hex32.SIZE_BYTES * 2, MethodId.SIZE_BYTES) == data
    }

    def "should throw exception for encode call with wrong parameters number"() {
        when:
        method.encodeCall params

        then:
        thrown IllegalArgumentException

        where:
        _ | params
        _ | [] as Object[]
        _ | [1, 2, 3] as Object[]
    }

    def "should be converted to ABI string representation"() {
        expect:
        obj.toAbi() == str

        where:
        obj                                                 | str
        method                                              | 'bar(fixed128x128[2]):(address)'
        new ContractMethod.Builder().withName('bar')
                .withInputTypes(method.inputTypes).build()  | 'bar(fixed128x128[2])'
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first   | second
        method  | new ContractMethod.Builder().withName('bar').withInputTypes(method.inputTypes).build()
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first   | second
        method  | method
        method  | new ContractMethod.Builder().withName('bar').withInputTypes(method.inputTypes).build()
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
        expect:
        method as String == 'bar(fixed128x128[2]):(address)'
    }
}
