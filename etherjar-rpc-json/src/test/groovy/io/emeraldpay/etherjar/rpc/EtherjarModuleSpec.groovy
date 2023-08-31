package io.emeraldpay.etherjar.rpc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonMappingException
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

    def "Can decode Wei value"() {
        setup:
        def json = '{"wei":"0x499602d2","another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.wei == new Wei(1234567890)
    }

    def "Can decode Wei null value"() {
        setup:
        def json = '{"wei": null,"another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.wei == null
    }

    def "Fails to decode Wei invalid string value"() {
        setup:
        def json = '{"wei": "foobar","another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Fails to decode Wei invalid type value"() {
        setup:
        def json = '{"wei":123,"another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Can encode HexData value"() {
        setup:
        def obj = new TestObject(hexData: HexData.from("0x1234"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"hexData":"0x1234","another":1234567890}'
    }

    def "Can decode HexData value"() {
        setup:
        def json = '{"hexData":"0x499602d2","another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.hexData == HexData.from("0x499602d2")
    }

    def "Can decode HexData null value"() {
        setup:
        def json = '{"hexData": null,"another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.hexData == null
    }

    def "Can decode HexData empty value"() {
        setup:
        def json = '{"hexData": "0x","another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.hexData == null
    }

    def "Fails to decode HexData invalid string value"() {
        setup:
        def json = '{"hexData": "foobar","another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Fails to decode HexData invalid type value"() {
        setup:
        def json = '{"hexData":123,"another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Can encode BlockHash value"() {
        setup:
        def obj = new TestObject(blockHash: BlockHash.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"blockHash":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca","another":1234567890}'
    }

    def "Can decode BlockHash value"() {
        setup:
        def json = '{"blockHash":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.blockHash == BlockHash.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca")
    }

    def "Can encode TransactionId value"() {
        setup:
        def obj = new TestObject(transactionId: TransactionId.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"transactionId":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca","another":1234567890}'
    }

    def "Can decode TransactionId value"() {
        setup:
        def json = '{"transactionId":"0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca"}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.transactionId == TransactionId.from("0x9b7bd9e6d7cc36047cd3ed9ec3f3164179ca75f2db7be48e8d0f87be56f806ca")
    }

    def "Can encode HexQuantity value"() {
        setup:
        def obj = new TestObject(hexQuantity: HexQuantity.from(123), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"hexQuantity":"0x7b","another":1234567890}'
    }

    def "Can decode HexQuantity value"() {
        setup:
        def json = '{"hexQuantity":"0x499602d2","another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.hexQuantity == HexQuantity.from("0x499602d2")
    }

    def "Can decode HexQuantity null value"() {
        setup:
        def json = '{"hexQuantity": null,"another":1234567890}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.hexQuantity == null
    }

    def "Fails to decode HexQuantity invalid string value"() {
        setup:
        def json = '{"hexQuantity": "foobar","another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Fails to decode HexQuantity invalid type value"() {
        setup:
        def json = '{"hexQuantity":123,"another":1234567890}'
        when:
        objectMapper.readValue(json, TestObject.class)
        then:
        thrown(JsonMappingException)
    }

    def "Can encode Address value"() {
        setup:
        def obj = new TestObject(address: Address.from("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"address":"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48","another":1234567890}'
    }

    def "Can decode Address value"() {
        setup:
        def json = '{"address":"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.address == Address.from("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48")
    }

    def "Can encode MethodId value"() {
        setup:
        def obj = new TestObject(methodId: MethodId.from("0x12345678"), another: 1234567890)
        when:
        def json = objectMapper.writeValueAsString(obj)
        then:
        json == '{"methodId":"0x12345678","another":1234567890}'
    }

    def "Can decode MethodId value"() {
        setup:
        def json = '{"methodId":"0x12345678"}'
        when:
        def obj = objectMapper.readValue(json, TestObject.class)
        then:
        obj.methodId == MethodId.from("0x12345678")
    }

    @MapConstructor(noArg = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class TestObject {
        Wei wei
        HexData hexData
        BlockHash blockHash
        TransactionId transactionId
        HexQuantity hexQuantity
        Address address
        MethodId methodId

        Long another

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof TestObject)) return false

            TestObject that = (TestObject) o

            if (address != that.address) return false
            if (another != that.another) return false
            if (blockHash != that.blockHash) return false
            if (hexData != that.hexData) return false
            if (hexQuantity != that.hexQuantity) return false
            if (methodId != that.methodId) return false
            if (transactionId != that.transactionId) return false
            if (wei != that.wei) return false

            return true
        }

        int hashCode() {
            int result
            result = (wei != null ? wei.hashCode() : 0)
            result = 31 * result + (hexData != null ? hexData.hashCode() : 0)
            result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0)
            result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0)
            result = 31 * result + (hexQuantity != null ? hexQuantity.hashCode() : 0)
            result = 31 * result + (address != null ? address.hashCode() : 0)
            result = 31 * result + (methodId != null ? methodId.hashCode() : 0)
            result = 31 * result + (another != null ? another.hashCode() : 0)
            return result
        }
    }

}
