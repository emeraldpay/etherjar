package org.ethereumclassic.etherjar.rpc

import org.ethereumclassic.etherjar.rpc.json.TraceItemJson
import spock.lang.Specification

import java.text.SimpleDateFormat

/**
 *
 * @author Igor Artamonov
 */
class JacksonRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }

    def "converts trace list"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0xdc6c6d.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 3
        act[0].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[0].subtraces == 2L
        act[0].traceAddress == []
        act[1].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[1].subtraces == 0L
        act[1].traceAddress == [0L]
        act[2].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[2].subtraces == 0L
        act[2].traceAddress == [1L]
    }
}
