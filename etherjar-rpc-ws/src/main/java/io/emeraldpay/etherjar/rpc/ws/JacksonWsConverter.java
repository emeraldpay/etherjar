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
package io.emeraldpay.etherjar.rpc.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.emeraldpay.etherjar.rpc.JacksonRpcConverter;
import io.emeraldpay.etherjar.rpc.RpcResponseException;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

/**
 * Jackson based parsed for subscription JSONs
 *
 * @author Igor Artamonov
 */
@NullMarked
public class JacksonWsConverter extends JacksonRpcConverter {

    public SubscriptionJson readSubscription(String content) throws RpcResponseException {
        ObjectMapper objectMapper = getObjectMapper();
        SubscriptionJson responseJson;
        try {
            responseJson = objectMapper.readerFor(SubscriptionJson.class).readValue(content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RpcResponseException("Invalid response from RPC endpoint", e);
        }
        return responseJson;
    }
}
