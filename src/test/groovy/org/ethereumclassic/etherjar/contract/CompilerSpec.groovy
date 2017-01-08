package org.ethereumclassic.etherjar.contract

import spock.lang.Specification

/**
 *
 *
 * @author Igor Artamonov
 */
class CompilerSpec extends Specification {

    //
    // Run following before running tests:
    // npm install solc
    //
    Compiler compiler = new Compiler('./node_modules/solc/solcjs')

    def "Basic compile"() {
        setup:
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
        def act = compiler.compile(contract, false)
        then:
        act.success
        act.compiled != null
        act.abi != null
        act.abi.contains("\"name\":\"doit\"")
    }
}
