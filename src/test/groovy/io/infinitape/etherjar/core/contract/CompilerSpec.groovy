/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.infinitape.etherjar.core.contract

import io.infinitape.etherjar.rpc.JacksonEthRpcConverterSpec
import spock.lang.Ignore
import spock.lang.Specification

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
        InputStream contract = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("contract/SimpleToken.sol")

        Compiler compiler = Compiler.newBuilder()
                .withSolc('./node_modules/solc/solcjs')
                .build()
        when:
        def act = compiler.compile(contract)
        then:
        act.success
        act.count == 3
        act.names.sort() == ["l_SimpleToken", "l_ERC20", "l_StandardToken"].sort()
        act.getContract("SimpleToken").compiled != null
        act.getContract("SimpleToken").abi.contains("\"name\":\"transferFrom\"")
    }

    def "Fail with invalid path to solc"() {
        setup:
        InputStream contract = JacksonEthRpcConverterSpec.classLoader.getResourceAsStream("contract/SimpleToken.sol")
        Compiler compiler = Compiler.newBuilder()
                .withSolc('./fake_solcjs')
                .build()
        when:
        def act = compiler.compile(contract)
        then:
        !act.success
        act.errors.size() > 0
        act.count == 0
    }

    @Ignore("solc doesn't give any hint")
    def "Faile to compile invalid contract"() {
        setup:
        String contract = """
        contract SimpleContract {
            uint651256 x
        }
        """
        Compiler compiler = Compiler.newBuilder()
                .withSolc('./node_modules/solc/solcjs')
                .build()
        when:
        def act = compiler.compile(contract)
        then:
        !act.success
        act.count == 0
    }
}
