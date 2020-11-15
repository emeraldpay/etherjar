/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.solidity

import io.emeraldpay.etherjar.domain.Address
import io.emeraldpay.etherjar.domain.MethodId
import spock.lang.Shared
import spock.lang.Specification

class ContractSpec extends Specification {

    @Shared Contract contract

    def setup() {
        def methods = []

        methods << new ContractMethod('bar')
        methods << new ContractMethod('baz')
        methods << new ContractMethod('sam')

        contract = new Contract(Address.EMPTY, methods as ContractMethod[])

        assert contract.address == Address.EMPTY
        assert contract.methods == methods
    }

    def "should build an empty contract by default"() {
        def obj = new Contract.Builder().build()

        expect:
        obj == [Address.EMPTY] as Contract
    }

    def "should rebuild similar contract"() {
        def obj = new Contract.Builder().withAddress(contract.address)
                .withMethods(contract.methods as ContractMethod[]).build()

        expect:
        obj == contract
    }

    def "should be steady for external modifications"() {
        def coll = [] + contract.methods
        def obj = [contract.address, coll] as Contract

        when:
        coll.clear()

        then:
        obj.methods.size() == 3
    }

    def "should return the contract methods collection"() {
        when:
        def coll = contract.methods

        then:
        coll.size() == 3
        coll.stream().anyMatch { it.id == id }

        where:
        id << contract.methods*.id
    }

    def "should return immutable methods collection"() {
        def coll = contract.methods

        when:
        coll.clear()

        then:
        thrown UnsupportedOperationException
    }

    def "should find a contract method by a signature id"() {
        when:
        def opt = contract.findMethod id

        then:
        opt.get().id == id

        where:
        id << contract.methods*.id
    }

    def "should catch a non-existent contract method signatures id"() {
        when:
        def opt = contract.findMethod id

        then:
        !opt.present

        where:
        _ | id
        _ | new MethodId([0xff, 0xff, 0xff, 0xff] as byte[])
        _ | new MethodId([0x11, 0x11, 0x11, 0x11] as byte[])
    }

    def "should catch null method signature ids"() {
        when:
        contract.findMethod null

        then:
        thrown NullPointerException
    }

    def "should calculate consistent hashcode"() {
        expect:
        first.hashCode() == second.hashCode()

        where:
        first                           | second
        contract                        | new Contract(contract.address, contract.methods)
        new Contract(contract.address)  | new Contract(contract.address)
    }

    def "should be equal"() {
        expect:
        first == second

        where:
        first       | second
        contract    | contract
        contract    | new Contract(contract.address)
        contract    | new Contract(contract.address, contract.methods)
    }

    def "should not be equal"() {
        expect:
        first != second

        where:
        first       | second
        contract    | null
        contract    | new ContractMethod('bar')
        contract    | new Contract(Address.from("0x0000000000015b23c7e20b0ea5ebd84c39dcbe60"))
    }

    def "should be converted to a string representation"() {
        def str = contract as String

        expect:
        str ==~ /Contract\{.+}/
        str.contains 'address=0x0000000000000000000000000000000000000000'
        str.contains "methods=$contract.methods"
    }
}
