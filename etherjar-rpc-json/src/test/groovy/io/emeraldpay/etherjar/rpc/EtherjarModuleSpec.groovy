package io.emeraldpay.etherjar.rpc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.MapConstructor
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.BlockHash
import io.emeraldpay.etherjar.domain.MethodId
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.hex.HexQuantity
import spock.lang.Specification

class EtherjarModuleSpec extends Specification {

    def objectMapper = new ObjectMapper().tap {
        it.registerModule(new EtherjarModule())
    }

    def "Can encode Wei value"() {
        setup:
        def obj = new TestObject([wei: new Wei(1234567890), another: 1234567890])
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"wei":"0x499602d2","another":1234567890}'
    }

    def "Can encode HexData value"() {
        setup:
        def obj = new TestObject(hexData: HexData.from("0x1234"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"hexData":"0x1234","another":1234567890}'
    }

    def "Can encode BlockHash value"() {
        setup:
        def obj = new TestObject(blockHash: BlockHash.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"blockHash":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca","another":1234567890}'
    }

    def "Can encode TransactionId value"() {
        setup:
        def obj = new TestObject(transactionId: TransactionId.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"transactionId":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca","another":1234567890}'
    }

    def "Can encode HexQuantity value"() {
        setup:
        def obj = new TestObject(hexQuantity: HexQuantity.from(123), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"hexQuantity":"0x7b","another":1234567890}'
    }

    def "Can encode Address value"() {
        setup:
        def obj = new TestObject(address: Address.from("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"address":"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48","another":1234567890}'
    }

    def "Can encode MethodId value"() {
        setup:
        def obj = new TestObject(methodId: MethodId.from("0x12345678"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"methodId":"0x12345678","another":1234567890}'
    }

    @MapConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TestObject {
        Wei wei
        HexData hexData
        BlockHash blockHash
        TransactionId transactionId
        HexQuantity hexQuantity
        Address address
        MethodId methodId

        Long another
    }

}
