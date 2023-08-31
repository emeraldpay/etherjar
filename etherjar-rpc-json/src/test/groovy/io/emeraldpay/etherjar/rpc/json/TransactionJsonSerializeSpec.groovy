package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class TransactionJsonSerializeSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = jacksonRpcConverter.createJsonMapper()

    def "Write-Read 0xb8e7e1"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0xb8e7e1.json")
        when:
        def base = jacksonRpcConverter.fromJson(json, TransactionJson)
        def serialized = objectMapper.writeValueAsString(base)
        def act = objectMapper.readValue(serialized, TransactionJson)
        then:
        act == base
    }

    def "Write 0xb8e7e1"() {
        setup:
        InputStream json = this.class.classLoader.getResourceAsStream("tx/0xb8e7e1.json")
        when:
        def base = jacksonRpcConverter.fromJson(json, TransactionJson)
        def serialized = objectMapper.writeValueAsString(base)

        then:
        serialized.contains("\"hash\":\"0xb8e7e1e41e0b11ed6524a869a89c5dc471371f6a79982a95293caa6705331c8c\"")
    }
}
