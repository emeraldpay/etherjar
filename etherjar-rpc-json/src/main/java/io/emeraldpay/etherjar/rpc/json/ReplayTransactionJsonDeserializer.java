/*
 * Copyright (c) 2022 EmeraldPay Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.emeraldpay.etherjar.domain.TransactionId;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.IOException;

public class ReplayTransactionJsonDeserializer extends JsonDeserializer<ReplayTransactionJson> {

    @Override
    public ReplayTransactionJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!jp.getCurrentToken().isStructStart()) {
            DeserializationDebug.LOGGER.error("Not an object", jp.getCurrentToken());
            return null;
        }
        jp.nextToken();

        ReplayTransactionJson result = new ReplayTransactionJson();

        while (jp.currentToken() != null && !jp.currentToken().isStructEnd()) {
            String type = jp.getCurrentName();
            if ("output".equals(type)) {
                jp.nextToken();
                result.setOutput(HexData.from(jp.getText()));
                jp.nextToken();
            } else if ("transactionHash".equals(type)) {
                jp.nextToken();
                result.setTransactionHash(TransactionId.from(jp.getText()));
                jp.nextToken();
            } else if ("stateDiff".equals(type)) {
                jp.nextToken();
                result.setStateDiff(jp.readValueAs(StateDiffJson.class));
                jp.nextToken();
            } else if ("trace".equals(type)) {
                //TODO
                jp.nextToken();
                jp.skipChildren();
                jp.nextToken();
            } else if ("vmTrace".equals(type)) {
                //TODO
                jp.nextToken();
                jp.skipChildren();
                jp.nextToken();
            } else {
                DeserializationDebug.LOGGER.info("Unsupported field for ReplayTransactionJson", type);
                jp.nextToken();
                jp.skipChildren();
                jp.nextToken();
            }
        }

        return result;
    }


}
