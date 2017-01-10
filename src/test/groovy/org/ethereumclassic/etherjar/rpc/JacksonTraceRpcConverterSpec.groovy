package org.ethereumclassic.etherjar.rpc

import org.ethereumclassic.etherjar.rpc.json.TraceItemJson
import spock.lang.Specification

import java.text.SimpleDateFormat

/**
 *
 * @author Igor Artamonov
 */
class JacksonTraceRpcConverterSpec extends Specification {

    JacksonRpcConverter jacksonRpcConverter = new JacksonRpcConverter()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")

    def setup() {
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
    }


    def "Transfer"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x16cb69.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 1
        act[0].type == TraceItemJson.TraceType.CALL
        act[0].action.from?.toHex() == '0x026d9acb43f44b258ebcfae786f32ae8376f6f00'
        act[0].action.gas?.value?.intValue() == 69000
        act[0].action.input == null
        act[0].action.to?.toHex() == '0x506c79b086e7c0e1d654b92e806d7a177fcbbd6e'
        act[0].action.value?.toString() == '0.2547 ether'
        act[0].action.callType == TraceItemJson.CallType.CALL
        act[0].blockHash.toHex() == '0x6d4ba9516a1e92b95ebbd8e7d024c6277b8607c1bc94abc1a53c5c5f65c7bf7c'
        act[0].blockNumber.intValue() == 2279999
        act[0].result.gasUsed.value.intValue() == 0
        act[0].result.output == null
        act[0].subtraces == 0L
        act[0].traceAddress == []
        act[0].transactionHash.toHex() == '0x16cb6998a1bf41e7cd64d9946f58e5a718bb1f79d7ffcf9902798ff8642d6593'
        act[0].transactionPosition == 0L
    }

    def "Call a contract"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x19442f.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 2

        act[0].action.from?.toHex() == '0xed059bc543141c8c93031d545079b3da0233b27f'
        act[0].action.gas?.value?.intValue() == 228728
        act[0].action.input.toHex() == '0x667a2f58'
        act[0].action.to?.toHex() == '0x8b3b3b624c3c0397d3da8fd861512393d51dcbac'
        act[0].action.value?.toString() == '0.0000 ether'
        act[0].action.callType == TraceItemJson.CallType.CALL
        act[0].blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act[0].blockNumber.intValue() == 1720231
        act[0].result.gasUsed.value.intValue() == 216235
        act[0].result.output.toHex() == '0x0000000000000000000000000000000000000000000000000000000000000001'
        act[0].subtraces == 1L
        act[0].traceAddress == []
        act[0].transactionHash.toHex() == '0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'
        act[0].transactionPosition == 4L

        act[1].type == TraceItemJson.TraceType.CREATE
        act[1].action.from?.toHex() == '0x8b3b3b624c3c0397d3da8fd861512393d51dcbac'
        act[1].action.gas?.value?.intValue() == 195789
        act[1].action.init.toHex() == '0x606060405260405160208061028783395060806040525160008054600160a060020a03199081163317909155600180549091168217905550610242806100456000396000f3606060405236156100405760e060020a600035046335faa416811461004d57806355a373d61461006e5780638da5cb5b14610080578063b69ef8a814610092575b6101026000610107610096565b61017760008054819033600160a060020a039081169116146101c857610002565b610190600154600160a060020a031681565b610190600054600160a060020a031681565b6101ad5b6000600160009054906101000a9004600160a060020a0316600160a060020a03166370a08231306040518260e060020a0281526004018082600160a060020a031681526020019150506020604051808303816000876161da5a03f1156100025750506040515191505090565b005b50565b604080516001546000805460e060020a63a9059cbb028452600160a060020a03908116600485015260248401869052935194955092169263a9059cbb926044808401936020939290839003909101908290876161da5a03f115610002575050604051511515905061010457610002565b6040805192835260208301919091528051918290030190f35b60408051600160a060020a03929092168252519081900360200190f35b60408051918252519081900360200190f35b600091505b9091565b6101d0610096565b600154600080546040805160e060020a63a9059cbb028152600160a060020a03928316600482015260248101869052905194955092169263a9059cbb9260448181019360209392839003909101908290876161da5a03f115610002575050604051511590506101bf57600191506101c456000000000000000000000000bb9bc244d798123fde783fcc1c72d3bb8c189413'
        act[1].action.value?.toString() == '0.0000 ether'
        act[1].blockHash.toHex() == '0x2c83b485b4e9211e2296b4cafd4f19f7dcb16c24430c187d37e6d93f8fd4a802'
        act[1].blockNumber.intValue() == 1720231
        act[1].result.address.toHex() == '0x127b00542399f6b092042658b21b998b13811d6c'
        act[1].result.code.toHex() == '0x606060405236156100405760e060020a600035046335faa416811461004d57806355a373d61461006e5780638da5cb5b14610080578063b69ef8a814610092575b6101026000610107610096565b61017760008054819033600160a060020a039081169116146101c857610002565b610190600154600160a060020a031681565b610190600054600160a060020a031681565b6101ad5b6000600160009054906101000a9004600160a060020a0316600160a060020a03166370a08231306040518260e060020a0281526004018082600160a060020a031681526020019150506020604051808303816000876161da5a03f1156100025750506040515191505090565b005b50565b604080516001546000805460e060020a63a9059cbb028452600160a060020a03908116600485015260248401869052935194955092169263a9059cbb926044808401936020939290839003909101908290876161da5a03f115610002575050604051511515905061010457610002565b6040805192835260208301919091528051918290030190f35b60408051600160a060020a03929092168252519081900360200190f35b60408051918252519081900360200190f35b600091505b9091565b6101d0610096565b600154600080546040805160e060020a63a9059cbb028152600160a060020a03928316600482015260248101869052905194955092169263a9059cbb9260448181019360209392839003909101908290876161da5a03f115610002575050604051511590506101bf57600191506101c456'
        act[1].result.gasUsed.value.intValue() == 155966
        act[1].error == null
        act[1].subtraces == 0L
        act[1].traceAddress == [0L]
        act[1].transactionHash.toHex() == '0x19442fe5e9e4f4819b7090298f1f108f2a1cca1f2167a413c771d6574fa34a31'
        act[1].transactionPosition == 4L
    }

    def "Failed call"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0xdc6c6d.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 3

        act[0].type == TraceItemJson.TraceType.CALL
        act[0].action.from?.toHex() == '0x0efe510e11c338c4d0284d058dcc8d3194b0882e'
        act[0].action.gas?.value?.intValue() == 137699
        act[0].action.input.toHex() == '0x3ccfd60b'
        act[0].action.to?.toHex() == '0xbf4ed7b27f1d666546e30d74d50d173d20bca754'
        act[0].action.value?.toString() == '0.0000 ether'
        act[0].blockHash.toHex() == '0x6cac615aa753c6f425c6b611cb14773f869b23cc5eeb8d686105cbc1da737a27'
        act[0].blockNumber.intValue() == 2141493
        act[0].error != null
        act[0].subtraces == 2L
        act[0].traceAddress == []
        act[0].transactionHash.toHex() == '0xdc6c6d169946767dc3448848c1dd82e6286ac939aadeac8450ab959cac7da54d'
        act[0].transactionPosition == 1L
    }

    def "Shanghai Go Home Attack"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x5c1969-eth.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 1

        act[0].type == TraceItemJson.TraceType.CALL
        act[0].action.from?.toHex() == '0x1d0fee96aa9750f87894b034d25e17edca8b76a3'
        act[0].action.gas?.value?.intValue() == 171346
        act[0].action.input.toHex() == '0x913fdfbd00000000000000000000000000000000000000000000000000000000000000550000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000300000000000000000000000000000000000000000000000000000000000000104661687274206e61636820486175736500000000000000000000000000000000'
        act[0].action.to?.toHex() == '0xb284e6a25d0972f9a92fec45d2075067db2d49b0'
        act[0].action.value?.toString() == '0.0000 ether'
        act[0].blockHash.toHex() == '0x9852a25198a980b28999db234404a99ebf38bd9531b330bf6d7cf4cfe0f904ea'
        act[0].blockNumber.intValue() == 2283416
        act[0].result.gasUsed.value.toInteger() == 0x0116b2
        act[0].subtraces == 0L
        act[0].traceAddress == []
        act[0].transactionHash.toHex() == '0x5c19695f50a30abbadfeef201d695d9c95c254534019e2f6a7a590e9ef246e82'
        act[0].transactionPosition == 2L
    }

    def "The Dao Hack"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x847149.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 236
        act[0].subtraces == 2L
        act[51].traceAddress.size() == 20
        act[51].traceAddress == [1, 3, 0, 1, 3, 0, 1, 3, 0, 1, 3, 0, 1, 3, 0, 1, 3, 0, 1, 0] as List<Long>
        act[123].traceAddress.size() == 47
    }

    def "Contract suicide"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0xb9c321.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 2
        act[0].type == TraceItemJson.TraceType.CALL
        act[1].type == TraceItemJson.TraceType.SUICIDE
        act[1].action.address?.toHex() == '0x7d4c7c61f98d653d5b49b695a01839791e002393'
        act[1].action.balance?.toHex() == '0x1aa9a6b21eb820000'
        act[1].action.refundAddress?.toHex() == '0xd9446e2e0c9216a393df8e46c70ae8bbcce87e3c'
        act[1].result == null
    }

    def "Create plus Suicide Attack"() {
        setup:
        InputStream json = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("trace/0x02e508.json")
        when:
        def act = jacksonRpcConverter.fromJsonList(json, TraceItemJson.class)
        then:
        act.size() == 2642
        act[0].type == TraceItemJson.TraceType.CALL
        act[1].type == TraceItemJson.TraceType.CREATE
        act[2].type == TraceItemJson.TraceType.CALL
        act[3].type == TraceItemJson.TraceType.SUICIDE
        act.every { it.blockHash.toHex() == '0xcccca35475a616036977373053068d32e9f1b7dd111b66cb1791ceb845307dd8' }
        act.every { it.blockNumber == 2423047L}
        act.every { it.transactionHash.toHex() == '0x02e5080477b605c6b83acbd93548f4fdb9205353bc6bad4a7172d951300de4cb'}
    }
}
