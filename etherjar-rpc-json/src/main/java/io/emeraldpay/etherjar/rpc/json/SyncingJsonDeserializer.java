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
package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public class SyncingJsonDeserializer extends EtherJsonDeserializer<SyncingJson> {

    @Override
    public SyncingJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        if (node.isBoolean()) {
            return new SyncingJson.Status(node.asBoolean());
        } else if (node.isObject()) {
            SyncingJson.AtBlock resp = new SyncingJson.AtBlock();
            resp.setStartingBlock(getLong(node, "startingBlock"));
            resp.setCurrentBlock(getLong(node, "currentBlock"));
            resp.setHighestBlock(getLong(node, "highestBlock"));
            if (node.has("stages") && node.get("stages").isArray()) {
                List<SyncingJson.Stage> stages = new java.util.ArrayList<>();
                for (JsonNode stageNode : node.get("stages")) {
                    SyncingJson.Stage stage = new SyncingJson.Stage();
                    stage.setStageName(stageNode.get("stage_name").asText());
                    stage.setBlock(getLong(stageNode, "block_number"));
                    stages.add(stage);
                }
                resp.setStages(stages);
            }
            return resp;
        } else {
            throw new IOException("Invalid syncing value: " + node);
        }
    }
}
