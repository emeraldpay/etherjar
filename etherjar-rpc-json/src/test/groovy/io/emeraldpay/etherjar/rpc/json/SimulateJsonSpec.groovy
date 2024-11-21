package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.Wei
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification

class SimulateJsonSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    ObjectMapper objectMapper = jacksonRpcConverter.getObjectMapper()

    SimulateJson request1 = new SimulateJson().tap {
        blockStateCalls = [
            new SimulateJson.BlockStateCalls().tap {
                blockOverrides = new BlockOverridesJson().tap {
                    baseFeePerGas = new Wei(9)
                }
                overrideState(
                    Address.from("0xc000000000000000000000000000000000000000"),
                    new StateOverrideJson().tap {
                        balance = new Wei(0x4a817c800)
                    }
                )
                appendCall(new TransactionCallJson().tap {
                    from = Address.from("0xc000000000000000000000000000000000000000")
                    to = Address.from("0xc000000000000000000000000000000000000001")
                    maxFeePerGas = new Wei(0xf)
                    value = new Wei(0x1)
                })
                appendCall(new TransactionCallJson().tap {
                    from = Address.from("0xc000000000000000000000000000000000000000")
                    to = Address.from("0xc000000000000000000000000000000000000002")
                    maxFeePerGas = new Wei(0xf)
                    value = new Wei(0x1)
                })
            }
        ]
        validation = true
        traceTransfers = true
    }

    def "Encodes request"() {
        setup:
        String expected = BlockJsonSpec.classLoader.getResourceAsStream("simulate/simulate-1.json")
            .text
            .replaceAll("\\s+", "")

        when:
        def act = objectMapper.writeValueAsString(request1)

        then:
        act == expected
    }

    def "Decodes request"() {
        setup:
        String json = BlockJsonSpec.classLoader.getResourceAsStream("simulate/simulate-1.json").text

        when:
        def act = objectMapper.readValue(json, SimulateJson)

        then:
        act == request1
    }
}
