package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.contract.type.Type
import org.ethereumclassic.etherjar.model.Hex32
import org.ethereumclassic.etherjar.model.MethodId
import spock.lang.Specification

/**
 * @author Igor Artamonov
 */
class ContractMethodSpec extends Specification {

    def "encode call"() {
        setup:
        def t1 = [
                isDynamic: { false },
                getBytesFixedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [Hex32.from('0x0000000000000000000000000000000220000000000000000000000000000000')] as Hex32[] }
        ] as Type;
        def t2 = [
                isDynamic: { false },
                getBytesFixedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [Hex32.from('0x0000000000000000000000000000000880000000000000000000000000000000')] as Hex32[] }
        ] as Type;
        def m = new ContractMethod(MethodId.fromSignature('bar(fixed128x128[2])'), t1 ,t2)
        when:
        def act = m.encodeCall([1, 2] as Object[])
        then:
        act.toHex() == '0xab55044d00000000000000000000000000000002200000000000000000000000000000000000000000000000000000000000000880000000000000000000000000000000'
    }

    def "encode call with wrong parameters number"() {
        setup:
        def t = [
                isDynamic: { false },
                getBytesFixedSize: { Hex32.SIZE_BYTES },
                encode: { Object obj ->
                    [Hex32.from('0x0000000000000000000000000000000000000000000000000000000000000000')] as Hex32[] }
        ] as Type;
        def m = new ContractMethod(MethodId.fromSignature('bar(fixed128x128[2])'), t)
        when:
        m.encodeCall(params)
        then:
        thrown(IllegalArgumentException)
        where:
        _ | params
        _ | [] as Object[]
        _ | [1, 2] as Object[]
    }
}
