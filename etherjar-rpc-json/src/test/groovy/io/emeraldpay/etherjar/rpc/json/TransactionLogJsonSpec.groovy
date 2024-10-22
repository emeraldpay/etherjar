package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.TransactionId
import io.emeraldpay.etherjar.hex.Hex32
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class TransactionLogJsonSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = jacksonRpcConverter.getObjectMapper()

    def "Equals with topics values"() {
        setup:
        // val1 and val2 topics are both lists, but different implementations
        // must be equal anyway

        def val1 = new TransactionLogJson().tap {
            it.setTopics(new ArrayList([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2),
                Hex32.extendFrom(3)
            ]))
        }

        def val2 = new TransactionLogJson().tap {
            it.setTopics(new Vector().tap {
                it.addAll([
                    Hex32.extendFrom(1),
                    Hex32.extendFrom(2),
                    Hex32.extendFrom(3)
                ])
            })
        }

        def val3 = new TransactionLogJson().tap {
            it.setTopics([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2),
                Hex32.extendFrom(4)
            ])
        }

        def val4 = new TransactionLogJson().tap {
            it.setTopics([
                Hex32.extendFrom(1),
                Hex32.extendFrom(2)
            ])
        }

        when:
        def equals11 = val1.equals(val1)
        def equals12 = val1.equals(val2)
        def equals13 = val1.equals(val3)
        def equals14 = val1.equals(val4)

        def equals21 = val2.equals(val1)
        def equals22 = val2.equals(val2)
        def equals23 = val2.equals(val3)
        def equals24 = val2.equals(val4)

        def equals31 = val3.equals(val1)
        def equals32 = val3.equals(val2)
        def equals33 = val3.equals(val3)
        def equals34 = val3.equals(val4)

        then:
        equals11
        equals12
        !equals13
        !equals14

        equals21
        equals22
        !equals23
        !equals24

        !equals31
        !equals32
        equals33
        !equals34
    }

    def "Should not generate extra hash field"() {
        setup:

        def tx = new TransactionLogJson()
        tx.transactionHash = TransactionId.from("0xc765d3a679254d60a519d83f33059f882875ad67d4114fb3de4c87a446fd0e52")
        tx.setTopics([
            Hex32.from("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
            Hex32.from("0x00000000000000000000000009b77fda4f5d9016d2cbe1d9fd12d465df8f96d5"),
            Hex32.from("0x000000000000000000000000ef8801eaf234ff82801821ffe2d78d60a0237f97"),
        ])
        tx.data = HexData.from("0x0000000000000000000000000000000000000000000000000000000012113a25")
        tx.address = Address.from("0xdAC17F958D2ee523a2206206994597C13D831ec7")

        when:
        def json = objectMapper.writeValueAsString(tx)

        then:
        json.contains("\"transactionHash\":")
        !json.contains("\"hash\":")
        json.findAll("0xc765d3a679254d60a519d83f33059f882875ad67d4114fb3de4c87a446fd0e52").size() == 1
    }
}
