package io.infinitape.etherjar.contract

import io.infinitape.etherjar.contract.type.Type
import io.infinitape.etherjar.core.Hex32
import io.infinitape.etherjar.core.HexData
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

class ContractParametersTypesSpec extends Specification {

    @Shared ContractParametersTypes arr

    def setup() {
        def t1 = [
                getCanonicalName: { 'a' },
                isDynamic: { false },
                getFixedSize: { Hex32.SIZE_BYTES },
        ] as Type

        def t2 = [
                getCanonicalName: { 'bb' },
                isDynamic: { false },
                getFixedSize: { Hex32.SIZE_BYTES * 2 },
        ] as Type

        def t3 = [
                getCanonicalName: { 'ccc' },
                isDynamic: { true },
                getFixedSize: { Hex32.SIZE_BYTES },
        ] as Type

        arr = [t1, t2, t3] as ContractParametersTypes

        assert !arr.empty
        assert arr.types.size() == 3
    }

    def "should check parameters types signature validity"() {
        expect:
        ContractParametersTypes.isAbiValid valid_sign

        where:
        _ | valid_sign
        _ | ''
        _ | 'uint32'
        _ | 'uint32,bool'
        _ | 'fixed128x128[2]'
        _ | 'uint256,uint32[],bytes10,bytes'
    }

    def "should check parameters types signature invalidity"() {
        expect:
        !ContractParametersTypes.isAbiValid(invalid_sign)

        where:
        _ | invalid_sign
        _ | 'uint32, bool'
        _ | 'fixed128x128[2],'
    }

    def "should create parameters from ABI"() {
        def str = arr.toAbi()

        def parser = Stub(Function) {
            apply(_) >>> arr.types.collect { Optional.of it }
        }

        when:
        def obj = ContractParametersTypes.fromAbi({ -> [parser] }, str)

        then:
        obj == arr
    }

    def "should catch wrong parameters types signature"() {
        when:
        ContractParametersTypes.fromAbi({ -> [] }, abi)

        then:
        thrown IllegalArgumentException

        where:
        _ | abi
        _ | 'a, b'
        _ | 'abc,'
    }

    def "should catch not-exist ABI types"() {
        when:
        ContractParametersTypes.fromAbi({ -> [] }, abi)

        then:
        thrown IllegalArgumentException

        where:
        _ | abi
        _ | 'a,b'
        _ | 'abc'
    }

    def "should create empty parameters from empty ABI"() {
        when:
        def obj = ContractParametersTypes.fromAbi({ -> [] }, '')

        then:
        !obj.types
        !obj.fixedSize
    }

    def "should catch null ABI"() {
        when:
        ContractParametersTypes.fromAbi({ -> [] }, null)

        then:
        thrown NullPointerException
    }

    def "should be steady for external modifications"() {
        def coll = arr.types as ArrayList

        def obj = coll as ContractParametersTypes

        when:
        coll.clear()

        then:
        obj.types == arr.types
    }

    def "should return immutable types collection"() {
        def coll = arr.types

        when:
        coll.clear()

        then:
        thrown UnsupportedOperationException
    }

    def "should encode & decode 'fixed[2]' with the argument [2.125, 8.5]"() {
        def data = HexData.combine(
                Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000'),
                Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000'))

        def val = [2.125, 8.5] as BigDecimal[]

        def type = Stub(Type) {
            isStatic() >> true
            getCanonicalName() >> 'fixed128x128[2]'
            getFixedSize() >> Hex32.SIZE_BYTES * 2
            encode(val) >> data
            decode(data) >> val
        }

        def obj = [type] as ContractParametersTypes

        when:
        def enc = obj.encode([val])
        def dec = obj.decode enc

        then:
        enc == data

        and:
        dec == [val]
    }

    def "should encode & decode 'baz(uint32,bool)' with the parameters 69 and true"() {
        def data1 = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000045'
        def data2 = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000001'

        def val1 = 69
        def val2 = Boolean.TRUE

        def type1 = Stub(Type) {
            isStatic() >> true
            getCanonicalName() >> 'uint32'
            getFixedSize() >> Hex32.SIZE_BYTES
            encode(val1) >> data1
            decode(data1) >> val1
        }

        def type2 = Stub(Type) {
            isStatic() >> true
            getCanonicalName() >> 'bool'
            getFixedSize() >> Hex32.SIZE_BYTES
            encode(val2) >> data2
            decode(data2) >> val2
        }

        def obj = [type1, type2] as ContractParametersTypes

        when:
        def enc = obj.encode val1, val2
        def dec = obj.decode enc

        then:
        enc == HexData.combine(data1, data2)

        and:
        dec == [val1, val2]
    }

    def "should encode & decode 'sam(bytes,bool,uint[])' with the arguments 'dave', true and [1,2,3]"() {
        def data1 = HexData.combine(
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000004'),
                Hex32.from('0x6461766500000000000000000000000000000000000000000000000000000000'))

        def data2 = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000001'

        def data3 = HexData.combine(
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000003'),
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000001'),
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000002'),
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000003'))

        def off1 = Hex32.from '0x0000000000000000000000000000000000000000000000000000000000000060'
        def off2 = Hex32.from '0x00000000000000000000000000000000000000000000000000000000000000a0'

        def val1 = 'dave'
        def val2 = Boolean.TRUE
        def val3 = [1, 2, 3] as BigInteger[]

        def type1 = Stub(Type) {
            isDynamic() >> true
            getCanonicalName() >> 'bytes'
            getFixedSize() >> Hex32.SIZE_BYTES
            encode(val1) >> data1
            decode(data1) >> val1
        }

        def type2 = Stub(Type) {
            isStatic() >> true
            getCanonicalName() >> 'bool'
            getFixedSize() >> Hex32.SIZE_BYTES
            encode(val2) >> data2
            decode(data2) >> val2
        }

        def type3 = Stub(Type) {
            isDynamic() >> true
            getCanonicalName() >> 'uint256[]'
            getFixedSize() >> Hex32.SIZE_BYTES
            encode(val3) >> data3
            decode(data3) >> val3
        }

        def obj = [type1, type2, type3] as ContractParametersTypes

        when:
        def enc = obj.encode val1, val2, val3
        def dec = obj.decode enc

        then:
        enc == HexData.combine(/* head */off1, data2, off2, /* tail */data1, data3)

        and:
        dec == [val1, val2, val3]
    }

    def "should encode & decode empty parameters types"() {
        when:
        def hex = ContractParametersTypes.EMPTY.encode()
        def args = ContractParametersTypes.EMPTY.decode hex

        then:
        hex == HexData.EMPTY
        !args
    }

    def "should throw exception for encode call with wrong parameters number"() {
        when:
        arr.encode params

        then:
        thrown IllegalArgumentException

        where:
        _ | params
        _ | [] as Object[]
        _ | [1] as Object[]
        _ | [1, 2] as Object[]
        _ | [1, 2, 3, 4] as Object[]
    }

    def "should throw exception when insufficient data length to decode"() {
        when:
        arr.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | data
        _ | HexData.EMPTY
        _ | HexData.from('0x0123456789abcdef')
    }

    def "should throw exception when illegal tail bytes offset to decode"() {
        def data = Hex32.from hex

        def type = [
                getCanonicalName: { 'ccc' },
                isDynamic: { true },
                getFixedSize: { Hex32.SIZE_BYTES },
        ] as Type

        def obj = [type] as ContractParametersTypes

        when:
        obj.decode data

        then:
        thrown IllegalArgumentException

        where:
        _ | hex
        _ | '0x0000000000000000000000000000000000000000000000000000000000000018'
        _ | '0x0000000000000000000000000000000000000000000000000000000000000040'
    }

    def "should throw exception when wrong tail part of data to decode"() {
        def data = HexData.combine(
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000030'),
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000000'))

        def type = [
                getCanonicalName: { 'ccc' },
                isDynamic: { true },
                getFixedSize: { Hex32.SIZE_BYTES },
                decode: { null }
        ] as Type

        def obj = [type] as ContractParametersTypes

        when:
        obj.decode data

        then:
        thrown IllegalStateException
    }

    def "should be converted to parameters types canonical names"() {
        when:
        def arr = arr.toCanonicalNames()

        then:
        Arrays.equals(arr, ['a', 'bb', 'ccc'] as String[])
    }

    def "should be converted to ABI string representation"() {
        expect:
        arr.toAbi() == 'a,bb,ccc'
    }

    def "should be converted to empty ABI string representation"() {
        expect:
        !new ContractParametersTypes().toAbi()
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first   | second
        arr     | arr.types as ContractParametersTypes
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first   | second
        arr     | arr
        arr     | arr.types as ContractParametersTypes
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first   | second
        arr     | null
        arr     | arr.types[0..1] as ContractParametersTypes
        arr     | HexData.EMPTY
    }

    def "should be converted to a string representation"() {
        expect:
        arr as String == 'a,bb,ccc'
    }
}
