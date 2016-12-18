package org.ethereumclassic.etherjar.contract.type

import org.ethereumclassic.etherjar.model.Hex32
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Function

class ArrayTypeSpec extends Specification {

    @Shared Type<Boolean> wrappedType1

    @Shared Type<String> wrappedType2

    @Shared ArrayType<Boolean> arrayType1

    @Shared ArrayType<String> arrayType2

    def setup() {
        def list1 = [
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000001'),
        ]

        def list2 = [
                Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000003'),
                Hex32.from('0x7878780000000000000000000000000000000000000000000000000000000001'),
        ]

        wrappedType1 = [
                getCanonicalName: { 'ABC' },
                isDynamic: { false },
                getFixedSize: { Hex32.SIZE_BYTES as long },
                encode: { Boolean bool -> list1 },
                decode: { if (it != list1) { throw new IllegalArgumentException() }; true },
        ] as Type

        wrappedType2 = [
                getCanonicalName: { 'CBA' },
                isDynamic: { true },
                getFixedSize: { Hex32.SIZE_BYTES as long },
                encode: { String str -> list2 },
                decode: { if (it != list2) { throw new IllegalArgumentException() }; 'xxx' },
        ] as Type

        arrayType1 = [wrappedType1, 12] as ArrayType
        arrayType2 = [wrappedType2, 21] as ArrayType
    }

    def "should parse string representation"() {
        def type = Stub(Type)
        def parser = Mock(Function)

        when:
        ArrayType.from({ -> [parser] }, input)

        then:
        1 * parser.apply(inter) >> Optional.of(type)
        0 * parser.apply(_)

        where:
        input           | inter     | output
        '_[]'           | '_'       | 'ABC[]'
        'abc[123]'      | 'abc'     | 'ABC[123]'
        '_[][123]'      | '_[]'     | 'ABC[123]'
        '_[123][]'      | '_[123]'  | 'ABC[]'
        '_[1][2][3]'    | '_[1][2]' | 'ABC[3]'
    }

    def "should detect null string representation"() {
        def parser = Mock(Function) {
            0 * apply(_)
        }

        when:
        ArrayType.from({ -> [parser] }, null)

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
        _ | 'int16[-1]'
        _ | 'int16[abc]'
        _ | 'int16[][-3]'
    }

    def "should create a fixed-size instance with fixed length"() {
        expect:
        arrayType1.wrappedType == wrappedType1
        arrayType1.fixedLength.present
        arrayType1.fixedLength.asLong == 12
        arrayType1.static
    }

    def "should create a non-fixed-size instance without fixed length"() {
        def obj = [wrappedType1] as ArrayType

        expect:
        obj.wrappedType == wrappedType1
        !obj.fixedLength.present
        obj.dynamic
    }

    def "should create a non-fixed-size instance with fixed length"() {
        expect:
        arrayType2.wrappedType == wrappedType2
        arrayType2.fixedLength.present
        arrayType2.fixedLength.asLong == 21
        arrayType2.dynamic
    }

    def "should return a canonical string representation" () {
        expect:
        type.canonicalName == str

        where:
        type                                            | str
        arrayType1                                      | 'ABC[12]'
        arrayType2                                      | 'CBA[21]'
        [wrappedType1] as ArrayType                     | 'ABC[]'
        [wrappedType2] as ArrayType                     | 'CBA[]'
        [arrayType1] as ArrayType                       | 'ABC[12][]'
        [arrayType2, 123] as ArrayType                  | 'CBA[21][123]'
        [[arrayType2, 123] as ArrayType] as ArrayType   | 'CBA[21][123][]'
    }

    def "should encode & decode array values"() {
        def parser1 = {
            it == 'type1' ? Optional.of(wrappedType1) : Optional.empty()
        } as Function

        def parser2 = {
            it == 'type2' ? Optional.of(wrappedType2) : Optional.empty()
        } as Function

        def obj = ArrayType.from({ -> [parser1, parser2] }, str).get()

        when:
        def data = obj.encode(arr as Object[])
        def res = obj.decode data

        then:
        data == hex
        Arrays.equals(res, arr as Object[])

        where:
        str         | arr                   | hex
        'type1[1]'  | [true]                | wrappedType1.encode(true)
        'type1[3]'  | [true, true, true]    | wrappedType1.encode(true) + wrappedType1.encode(true) + wrappedType1.encode(true)
        'type1[]'   | [true, true]          | [Type.encodeLength(2)] + wrappedType1.encode(true) + wrappedType1.encode(true)
        'type2[1]'  | ['xxx']               | [Type.encodeLength(Hex32.SIZE_BYTES)] + wrappedType2.encode('xxx')
        'type2[2]'  | ['xxx', 'xxx']        | [Type.encodeLength(2 * Hex32.SIZE_BYTES), Type.encodeLength((2 + wrappedType2.encode('A').size()) * Hex32.SIZE_BYTES)] + wrappedType2.encode('A') + wrappedType2.encode('B')
        'type2[]'   | ['xxx']               | [Type.encodeLength(1)] + [Type.encodeLength(Hex32.SIZE_BYTES)] + wrappedType2.encode('1')
    }

    def "should catch empty array to encode"() {
        when:
        arrayType1.encode([] as Boolean[])

        then:
        thrown IllegalArgumentException
    }

    def "should catch wrong array length to encode"() {
        when:
        arrayType1.encode(new Boolean[length])

        then:
        thrown IllegalArgumentException

        where:
        _ | length
        _ | 1
        _ | 8
        _ | 21
    }

    def "should catch empty data to decode"() {
        when:
        arrayType1.decode([] as Hex32[])

        then:
        thrown IllegalArgumentException
    }

    def "should catch wrong data to decode"() {
        def parser1 = {
            it == 'type1' ? Optional.of(wrappedType1) : Optional.empty()
        } as Function

        def parser2 = {
            it == 'type2' ? Optional.of(wrappedType2) : Optional.empty()
        } as Function

        def obj = ArrayType.from({ -> [parser1, parser2] }, str).get()

        when:
        obj.decode(hex as Hex32[])

        then:
        thrown IllegalArgumentException

        where:
        str         | hex
        'type1[1]'  | wrappedType1.encode(true) + wrappedType1.encode(true)
        'type1[3]'  | wrappedType1.encode(true) + wrappedType1.encode(true)
        'type1[]'   | [Type.encodeLength(2)] + wrappedType1.encode(true)
        'type1[]'   | [Type.encodeLength(1)] + wrappedType1.encode(true) + wrappedType1.encode(true)
        'type2[1]'  | [Type.encodeLength(Hex32.SIZE_BYTES)]
        'type2[1]'  | [Type.encodeLength(2 * Hex32.SIZE_BYTES)] + wrappedType2.encode('xxx')
        'type2[1]'  | [Type.encodeLength(Hex32.SIZE_BYTES)] + wrappedType2.encode('xxx') + wrappedType2.encode('xxx')
        'type2[2]'  | [Type.encodeLength(2 * Hex32.SIZE_BYTES), Type.encodeLength((2 + wrappedType2.encode('A').size()) * Hex32.SIZE_BYTES)] + wrappedType2.encode('A')
        'type2[2]'  | [Type.encodeLength(2 * Hex32.SIZE_BYTES), Type.encodeLength((2 + wrappedType2.encode('A').size()) * Hex32.SIZE_BYTES)] + wrappedType2.encode('A') + wrappedType2.encode('B') + wrappedType2.encode('C')
        'type2[2]'  | [Type.encodeLength((2 + wrappedType2.encode('A').size()) * Hex32.SIZE_BYTES), Type.encodeLength(2 * Hex32.SIZE_BYTES)] + wrappedType2.encode('A') + wrappedType2.encode('B')
        'type2[2]'  | [Type.encodeLength(Hex32.SIZE_BYTES), Type.encodeLength((2 + wrappedType2.encode('A').size()) * Hex32.SIZE_BYTES)] + wrappedType2.encode('A') + wrappedType2.encode('B')
        'type2[]'   | [Type.encodeLength(1)] + [Type.encodeLength(Hex32.SIZE_BYTES)]
        'type2[]'   | [Type.encodeLength(1)] + [Type.encodeLength(Hex32.SIZE_BYTES)] + wrappedType2.encode('1') + wrappedType2.encode('2')
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        [wrappedType1] as ArrayType     | [wrappedType1] as ArrayType
        [wrappedType2] as ArrayType     | [wrappedType2] as ArrayType
        [wrappedType1, 12] as ArrayType | [wrappedType1, 12] as ArrayType
        [wrappedType2, 21] as ArrayType | [wrappedType2, 21] as ArrayType
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first                                       | second
        arrayType1                                  | arrayType1
        arrayType2                                  | arrayType2
        arrayType1                                  | [wrappedType1, 12] as ArrayType
        arrayType2                                  | [wrappedType2, 21] as ArrayType
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first                                       | second
        arrayType1                                  | arrayType2
        arrayType2                                  | arrayType1
        arrayType1                                  | [wrappedType1, 8] as ArrayType
        arrayType2                                  | [wrappedType2, 12] as ArrayType
        arrayType1                                  | new UIntType()
        arrayType2                                  | "ABC"
    }

    def "should be converted to a string representation with fixed length"() {
        when:
        def str = arrayType1 as String

        then:
        str ==~ /ArrayType\{.+}/
        str.contains "type=${wrappedType1}"
        str.contains 'length=12'
    }

    def "should be converted to a string representation without fixed length"() {
        def obj = [wrappedType1] as ArrayType

        when:
        def str = obj as String

        then:
        str ==~ /ArrayType\{.+}/
        str.contains "type=${wrappedType1}"
        str.contains 'length=-1'
    }
}
