/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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
package io.infinitape.etherjar.rpc

import spock.lang.Specification

class NetCommandsSpec extends Specification {

    def version() {
        when:
        def call = Commands.net().version()

        then:
        call.method == "net_version"
        call.params == []
        call.jsonType == String
        call.resultType == Integer
    }

    def listening() {
        when:
        def call = Commands.net().listening()

        then:
        call.method == "net_listening"
        call.params == []
        call.jsonType == Boolean
        call.resultType == Boolean
    }

    def peerCount() {
        when:
        def call = Commands.net().peerCount()

        then:
        call.method == "net_peerCount"
        call.params == []
        call.jsonType == String
        call.resultType == Integer
    }

}
