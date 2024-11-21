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
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.IOException;

public class HexDataSerializer extends StdSerializer<HexData> {

    public HexDataSerializer() {
        super(HexData.class);
    }

    @Override
    public void serialize(HexData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toHex());
        }
    }

    /**
     * To serialize Hex Data (including Hex32 and Address) as a JSON object key.
     */
    public static class AsKey extends StdSerializer<HexData> {
        public AsKey() {
            super(HexData.class);
        }

        @Override
        public void serialize(HexData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeFieldName(value.toHex());
            }
        }
    }
}
