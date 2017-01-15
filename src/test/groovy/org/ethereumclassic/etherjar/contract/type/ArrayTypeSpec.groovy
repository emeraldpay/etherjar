package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.HexData
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

class ArrayTypeSpec extends Specification {

    @Shared Type<Boolean> wrappedType

    def setup() {
        wrappedType = [
                getCanonicalName: { 'ABC' },
                isDynamic: { false },
                getFixedSize: { 64 },
                encode: { Boolean bool ->
                    HexData.from('0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001')
                },
                decode: { true },
        ] as Type
    }

    def "should parse string representation"() {
        def type = Stub Type
        def parser = Mock Function

        when:
        ArrayType.from({ -> [parser] }, input)

        then:
        1 * parser.apply(inter) >> Optional.of(type)
        0 * parser.apply(_)

        where:
        input           | inter
        '_[]'           | '_'
        'abc[123]'      | 'abc'
        '_[][123]'      | '_[]'
        '_[123][]'      | '_[123]'
        '_[1][2][3]'    | '_[1][2]'
    }

    def "should detect null string representation"() {
        when:
        ArrayType.from({ -> [] }, null)

        then:
        thrown NullPointerException
    }

    def "should ignore wrong string representation"() {
        when:
        def opt = ArrayType.from({ -> [] }, input)

        then:
        !opt.present

        where:
        _ | input
        _ | ''
        _ | 'int16'
        _ | 'int16['
    }

    def "should detect wrong inputs in string representation"() {
        when:
        ArrayType.from({ -> [] }, input)

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'int16]'
        _ | 'int16[0]'
        _ | 'int16[-1]'
        _ | 'int16[abc]'
        _ | 'int16[][0]'
        _ | 'int16[][-3]'
    }

    def "should detect unknown array wrapped type"() {
        when:
        ArrayType.from({ -> [] }, input)

        then:
        thrown IllegalArgumentException

        where:
        _ | input
        _ | 'int16[]'
        _ | 'uint16[3]'
    }

    def "should create a fixed-size static instance"() {
        def obj = [wrappedType, 12] as ArrayType

        expect:
        obj.wrappedType == wrappedType
        obj.length.present
        obj.length.asInt == 12
        obj.static
    }

    def "should create a non-fixed-size dynamic instance"() {
        def obj = [wrappedType] as ArrayType

        expect:
        obj.wrappedType == wrappedType
        !obj.length.present
        obj.dynamic
    }

    def "should detect dynamic wrapped types"() {
        when:
        new ArrayType({ true } as Type)

        then:
        thrown IllegalArgumentException
    }

    def "should return a canonical string representation" () {
        expect:
        type.canonicalName == str

        where:
        type                                                                | str
        [wrappedType] as ArrayType                                          | 'ABC[]'
        [wrappedType, 12] as ArrayType                                      | 'ABC[12]'
        [[wrappedType, 21] as ArrayType] as ArrayType                       | 'ABC[21][]'
        [[[wrappedType, 12] as ArrayType, 123] as ArrayType] as ArrayType   | 'ABC[12][123][]'
    }

    def "should encode & decode array values"() {
        def parser = { Optional.of wrappedType } as Function

        def obj = ArrayType.from({ -> [parser] }, str).get()

        when:
        def data = obj.encode(arr as Object[])
        def res = obj.decode data

        then:
        data == hex
        Arrays.equals(res, arr as Object[])

        where:
        str     | arr                   | hex
        '_[1]'  | [true]                | wrappedType.encode(true)
        '_[3]'  | [true, true, true]    | HexData.combine([wrappedType.encode(true)] * 3)
        '_[]'   | [true, true]          | Type.encodeLength(2).concat([wrappedType.encode(true)] * 2)
        '_[]'   | []                    | Type.encodeLength(0)
    }

    def "should catch wrong array length to encode"() {
        def obj = [wrappedType, 12] as ArrayType

        when:
        obj.encode(new Boolean[length])

        then:
        thrown IllegalArgumentException

        where:
        _ | length
        _ | 1
        _ | 8
        _ | 21
    }

    def "should catch wrong data to decode"() {
        def parser = { Optional.of wrappedType } as Function

        def obj = ArrayType.from({ -> [parser] }, str).get()

        when:
        obj.decode hex

        then:
        thrown IllegalArgumentException

        where:
        str     | hex
        '_[]'   | Type.encodeLength(0).concat(wrappedType.encode(true))
        '_[]'   | Type.encodeLength(2).concat(wrappedType.encode(true))
        '_[1]'   | Type.encodeLength(1).concat([wrappedType.encode(true)] * 2)
        '_[3]'   | Type.encodeLength(3)
    }

    def "should catch empty data to decode"() {
        def obj = [wrappedType] as ArrayType

        when:
        obj.decode(HexData.EMPTY)

        then:
        thrown IllegalArgumentException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        [wrappedType] as ArrayType      | [wrappedType] as ArrayType
        [wrappedType, 12] as ArrayType  | [wrappedType, 12] as ArrayType
    }

    def "should be equal"() {
        def obj = [wrappedType, 12] as ArrayType

        expect:
        obj == obj

        and:
        obj == [wrappedType, 12] as ArrayType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first                           | second
        [wrappedType, 12] as ArrayType  | null
        [wrappedType, 12] as ArrayType  | [wrappedType] as ArrayType
        [wrappedType, 12] as ArrayType  | [wrappedType, 8] as ArrayType
        [wrappedType, 12] as ArrayType  | new UIntType()
        [wrappedType, 12] as ArrayType  | "ABC"
    }

    def "should be converted to a string representation with fixed length"() {
        def obj = [wrappedType, 12] as ArrayType

        expect:
        obj as String == 'ABC[12]'
    }

    def "should be converted to a string representation without fixed length"() {
        def obj = [wrappedType] as ArrayType

        expect:
        obj as String == 'ABC[]'
    }
}
