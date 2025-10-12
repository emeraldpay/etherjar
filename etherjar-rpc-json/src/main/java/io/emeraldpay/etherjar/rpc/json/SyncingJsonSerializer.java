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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SyncingJsonSerializer extends EtherJsonSerializer<SyncingJson> {

    @Override
    public void serialize(SyncingJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value instanceof SyncingJson.AtBlock) {
            SyncingJson.AtBlock atBlock = (SyncingJson.AtBlock) value;
            gen.writeStartObject();
            writeField(gen, "startingBlock", atBlock.getStartingBlock());
            writeField(gen, "currentBlock", atBlock.getCurrentBlock());
            writeField(gen, "highestBlock", atBlock.getHighestBlock());
            if (atBlock.getStages() != null && !atBlock.getStages().isEmpty()) {
                gen.writeArrayFieldStart("stages");
                for (SyncingJson.Stage stage : atBlock.getStages()) {
                    gen.writeStartObject();
                    gen.writeStringField("stage_name", stage.getStageName());
                    writeField(gen, "block_number", stage.getBlock());
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }
            gen.writeEndObject();
        } else {
            gen.writeBoolean(false);
        }
    }
}
