package org.ethereumclassic.etherjar.contract

import org.ethereumclassic.etherjar.rpc.JacksonEthRpcConverterSpec
import spock.lang.Specification

/**
 *
 *
 * @author Igor Artamonov
 */
class CompilerSpec extends Specification {

    //
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //
    // Run following before running tests:
    // npm install solc
    //
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //

    def "Basic compile"() {
        setup:
        Compiler compiler = Compiler.newBuilder()
                .withSolc('./node_modules/solc/solcjs')
                .optimize(false)
                .build()
        String contract = """
        pragma solidity ^0.4.4;

        contract SimpleContract {
            uint256 x;
            
            function doit() returns (uint256) {
                return 515142;
            }
        }
        """
        when:
        def act = compiler.compile(contract)
        then:
        act.success
        act.count == 1
        act.contracts[0].compiled != null
        act.contracts[0].abi != null
        act.contracts[0].abi.contains("\"name\":\"doit\"")
    }

    def "Multicontract compiler"() {
        setup:
        InputStream contract = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("contract/SimpleToken.sol")
        Compiler compiler = Compiler.newBuilder()
                .withSolc('./node_modules/solc/solcjs')
                .build()
        when:
        def act = compiler.compile(contract)
        then:
        act.success
        act.count == 3
        act.names.sort() == ["SimpleToken", "ERC20", "StandardToken"].sort()
        act.getContract("SimpleToken").compiled != null
        act.getContract("SimpleToken").abi.contains("\"name\":\"transferFrom\"")
    }
}
