package io.emeraldpay.etherjar.rpc.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.etherjar.hex.HexData
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter
import spock.lang.Specification


class ExecutionResultJsonSpec extends Specification {

    ObjectMapper objectMapper = new JacksonRpcConverter().getObjectMapper()

    def "Parse getReservers call 1"() {
        InputStream json = BlockJsonSpec.classLoader.getResourceAsStream("traceCall/1.json")

        when:
        def act = objectMapper.readValue(json, ExecutionResultJson.class)

        then:
        act instanceof ExecutionResultJson
        act.gas == 23568
        !act.failed
        act.returnValue.toHex() == "0x0000000000000000000000000000000000000000000000000004679fe3cb38df00000000000000000000000000000000000000000233a5ccbc54a51e1661c0120000000000000000000000000000000000000000000000000000000067bd66d3"
        act.structLogs.size() == 112
        with(act.structLogs[0]) {
            pc == 0
            op == "PUSH1"
            gas == 49978936
            gasCost == 3
            depth == 1
            stack.size() == 0
        }
        with(act.structLogs[2]) {
            pc == 4
            op == "MSTORE"
            gas == 49978930
            gasCost == 12
            depth == 1
            stack == [
                HexData.from("0x80"),
                HexData.from("0x40")
            ]
        }
        with(act.structLogs[58]) {
            pc == 3475
            op == "SLOAD"
            gas == 49978707
            gasCost == 2100
            depth == 1
            stack == [
                HexData.from("0x902f1ac"),
                HexData.from("0x2de"),
                HexData.from("0x8")
            ]
            storage.size() == 1
            storage.entrySet()
                .collect { it.key.toHex() + "=" + it.value.toHex() } == [
                    "0x0000000000000000000000000000000000000000000000000000000000000008=0x67bd66d300000233a5ccbc54a51e1661c0120000000000000004679fe3cb38df"
                ]
        }


    }
}
