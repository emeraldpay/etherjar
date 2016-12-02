package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.contract.type.BoolType
import org.ethereumclassic.etherjar.contract.type.Type
import org.ethereumclassic.etherjar.model.Address
import org.ethereumclassic.etherjar.model.Hex32
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
                getCanonicalName: { 'fixed128x128' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000') ] as Hex32[] }
        ] as Type

        def t2 = [
                getCanonicalName: { 'fixed128x128' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES },
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

    def "should copy contract method"() {
        def parser = Stub(Function) {
            apply('fixed128x128') >> Optional.of(method.inputTypes[0])
        }

        Type.Repository repo = { -> [parser] }

        def obj = ContractMethod.Builder.fromAbi(repo, method.toAbi()).build()

        expect:
        obj == method
    }

    def "should catch null ABIs"() {
        def repo = Stub(Type.Repository)

        when:
        ContractMethod.Builder.fromAbi(repo, abi)

        then:
        thrown NullPointerException

        where:
        _ | abi
        _ | null
    }

    def "should catch invalid ABIs"() {
        def parser = Stub(Function) {
            apply('int32') >> Optional.of('')
            apply(_ as String) >> Optional.empty()
        }

        Type.Repository repo = { -> [parser] }

        when:
        ContractMethod.Builder.fromAbi(repo, abi)

        then:
        thrown IllegalArgumentException

        where:
        _ | abi
        _ | ''
        _ | 'bar'
        _ | 'bar(uint32)'
        _ | 'bar(int32,uint32)'
        _ | '1bar(int32,int32)'
    }

    def "should rebuild similar contract method"() {
        def obj = new ContractMethod.Builder().withName('bar')
                .expects(method.getInputTypes() as Type[])
                .returns(method.outputTypes as Type[])
                .build()

        expect:
        obj == method
    }

    def "should build constant contract method"() {
        def obj = new ContractMethod.Builder()
                .withName('bar').asConstant()
                .expects(method.getInputTypes() as Type[])
                .returns(method.outputTypes as Type[])
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

    def "should be steady for external modifications"() {
        def coll = new ArrayList(method.inputTypes)
        def obj = new ContractMethod('bar', coll)

        when:
        coll.clear()

        then:
        obj.inputTypes.size() == method.inputTypes.size()
    }

    def "should be created correctly"() {
        expect:
        method.id == MethodId.fromSignature('bar', 'fixed128x128', 'fixed128x128')
        !method.constant
        method.inputTypes.size() == 2
        method.outputTypes.isEmpty()
    }

    def "should create correctly without constant flag"() {
        when:
        def obj = new ContractMethod(method.name)

        then:
        !obj.constant
    }

    def "should create correctly without input and output types"() {
        when:
        def obj = new ContractMethod(method.name, method.constant)

        then:
        !obj.constant
        obj.inputTypes.isEmpty()
        obj.outputTypes.isEmpty()
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

    def "should encode call 'bar(fixed[2])' with the with the argument [2.125, 8.5]"() {
        def type = [
                getCanonicalName: { 'fixed128x128[2]' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES * 2 },
                encode: { Object obj -> [
                        Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                        Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000')
                ] as Hex32[] }
        ] as Type

        def obj = new ContractMethod('bar', type)

        def args = [[BigDecimal.valueOf(2.125), BigDecimal.valueOf(8.5)]]

        when:
        def hex = obj.encodeCall(args as Object[]).toHex()
        def arr = hex.substring(MethodId.SIZE_HEX).split "(?<=\\G.{${Hex32.SIZE_BYTES << 1}})"

        then:
        obj.toAbi() == 'bar(fixed128x128[2])'
        hex.startsWith '0xab55044d'

        arr.length == 2
        arr[0] == '0000000000000000000000000000000220000000000000000000000000000000'
        arr[1] == '0000000000000000000000000000000880000000000000000000000000000000'
    }

    def "should encode call 'baz(uint32,bool)' with the parameters 69 and true"() {
        def type1 = [
                getCanonicalName: { 'uint32' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000045') ] as Hex32[] }
        ] as Type

        def type2 = [
                getCanonicalName: { 'bool' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000001') ] as Hex32[] }
        ] as Type

        def obj = new ContractMethod('baz', type1, type2)

        def args = [BigInteger.valueOf(69), BoolType.TRUE]

        when:
        def hex = obj.encodeCall(args as Object[]).toHex()
        def arr = hex.substring(MethodId.SIZE_HEX).split "(?<=\\G.{${Hex32.SIZE_BYTES << 1}})"

        then:
        obj.toAbi() == 'baz(uint32,bool)'
        hex.startsWith '0xcdcd77c0'

        arr.length == 2
        arr[0] == '0000000000000000000000000000000000000000000000000000000000000045'
        arr[1] == '0000000000000000000000000000000000000000000000000000000000000001'
    }

    def "should encode call 'sam(bytes,bool,uint[])' with the arguments 'dave', true and [1,2,3]"() {
        def type1 = [
                getCanonicalName: { 'bytes' },
                isDynamic: { true },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj -> [
                        Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000004'),
                        Hex32.from('0x6461766500000000000000000000000000000000000000000000000000000000')
                ] as Hex32[] }
        ] as Type

        def type2 = [
                getCanonicalName: { 'bool' },
                isDynamic: { false },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [ Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000001') ] as Hex32[] }
        ] as Type

        def type3 = [
                getCanonicalName: { 'uint256[]' },
                isDynamic: { true },
                getEncodedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj -> [
                        Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000003'),
                        Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000001'),
                        Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000002'),
                        Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000003')
                ] as Hex32[] }
        ] as Type

        def obj = new ContractMethod('sam', type1, type2, type3)

        def args = ['dave', BoolType.TRUE, [BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(3)]]

        when:
        def hex = obj.encodeCall(args as Object[]).toHex()
        def arr = hex.substring(MethodId.SIZE_HEX).split "(?<=\\G.{${Hex32.SIZE_BYTES << 1}})"

        then:
        obj.toAbi() == 'sam(bytes,bool,uint256[])'
        hex.startsWith '0xa5643bf2'

        arr.length == 9
        arr[0] == '0000000000000000000000000000000000000000000000000000000000000060'
        arr[1] == '0000000000000000000000000000000000000000000000000000000000000001'
        arr[2] == '00000000000000000000000000000000000000000000000000000000000000a0'
        arr[3] == '0000000000000000000000000000000000000000000000000000000000000004'
        arr[4] == '6461766500000000000000000000000000000000000000000000000000000000'
        arr[5] == '0000000000000000000000000000000000000000000000000000000000000003'
        arr[6] == '0000000000000000000000000000000000000000000000000000000000000001'
        arr[7] == '0000000000000000000000000000000000000000000000000000000000000002'
        arr[8] == '0000000000000000000000000000000000000000000000000000000000000003'
    }

    def "should throw exception for encode call with wrong parameters number"() {
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
