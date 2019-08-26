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
package io.infinitape.etherjar.rpc.ws;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * @author Igor Artamonov
 */
public class SubscriptionJsonDeserializer extends JsonDeserializer<SubscriptionJson> {

    @Override
    public SubscriptionJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        if (node.has("method")) {
            String method = node.get("method").asText();
            if (!"eth_subscription".equals(method)) {
                throw new IOException("Invalid method: " + method);
            }
            JsonNode params = node.get("params");
            SubscriptionJson resp = new SubscriptionJson();
            resp.setSubscription(params.get("subscription").asText());
            resp.setResult(params.get("result"));
            return resp;
        } else if (node.has("id")) {
            Integer id = node.get("id").asInt();
            SubscriptionJson resp = new SubscriptionJson();
            resp.setId(id);
            resp.setResult(node.get("result"));
            resp.setError(node.get("error"));
            return resp;
        } else {
            throw new IOException("Unsupported message: " + node.toString());
        }
    }
}
