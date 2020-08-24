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

package io.infinitape.etherjar.rpc.json

import spock.lang.Specification

/**
 *
 * @author Igor Artamonov
 */
class RequestJsonSpec extends Specification {

    def "Request can have int id"() {
        when:
        def req = new RequestJson("eth_none", [], 101)
        then:
        req.id == 101
        req.id != "101"
    }

    def "Request can have long id"() {
        when:
        def req = new RequestJson("eth_none", [], 1057264140543346L)
        then:
        req.id == 1057264140543346
        req.id != "101"
    }

    def "Request can have string id"() {
        when:
        def req = new RequestJson("eth_none", [], "1a5f")
        then:
        req.id == "1a5f"
    }

    def "Request can't have double id"() {
        when:
        def req = new RequestJson("eth_none", [], 1.23)
        then:
        thrown(IllegalArgumentException)
    }

}
