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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StateDiffJsonDeserializer extends JsonDeserializer<StateDiffJson> {

    protected static final Function<String, Wei> WEI_CONVERTER = Wei::from;
    protected static final Function<String, HexData> CODE_CONVERTER = HexData::from;
    protected static final Function<String, Long> NONCE_CONVERTER = (s) -> HexQuantity.from(s).getValue().longValueExact();
    protected static final Function<String, Hex32> STORAGE_CONVERTER = Hex32::from;

    @Override
    public StateDiffJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        StateDiffJson result = new StateDiffJson();
        if (!jp.getCurrentToken().isStructStart()) {
            DeserializationDebug.LOGGER.error("Cannot read StateDiff. Not an object", jp.getCurrentToken());
            return null;
        }
        jp.nextToken();

        while (!jp.currentToken().isStructEnd()) {
            try {
                Address address = Address.from(jp.getCurrentName());
                jp.nextToken();
                result.put(address, readAddressDiff(jp));
                jp.nextToken();
                jp.nextToken();
            } catch (Throwable t) {
                DeserializationDebug.LOGGER.error("Failed to read StateDiff", t);
            }
        }

        return result;
    }

    protected StateDiffJson.AddressDiff readAddressDiff(JsonParser jp) throws IOException {
        if (!jp.getCurrentToken().isStructStart()) {
            DeserializationDebug.LOGGER.error("Not an object", jp.getCurrentToken());
            return null;
        }
        jp.nextToken();

        StateDiffJson.AddressDiff result = new StateDiffJson.AddressDiff();
        while (!jp.currentToken().isStructEnd()) {
            String type = jp.getCurrentName();
            if ("balance".equals(type)) {
                jp.nextToken();
                result.setBalance(
                    readChange(jp, Wei.class, WEI_CONVERTER)
                );
            } else if ("code".equals(type)) {
                jp.nextToken();
                result.setCode(
                    readChange(jp, HexData.class, CODE_CONVERTER)
                );
            } else if ("nonce".equals(type)) {
                jp.nextToken();
                result.setNonce(
                    readChange(jp, Long.class, NONCE_CONVERTER)
                );
            } else if ("storage".equals(type)) {
                jp.nextToken();
                result.setStorage(
                    readStorageDiffs(jp)
                );
            } else {
                DeserializationDebug.LOGGER.info("Unsupported diff", type);
                throw new IllegalStateException("---");
            }
        }
        return result;
    }

    protected <T> StateDiffJson.Change<T> readChange(JsonParser jp, Class<T> type, Function<String, T> converter) throws IOException {
        JsonToken current = jp.getCurrentToken();

        if (current.isScalarValue()) {
            String changeType = jp.getText();
            if ("=".equals(changeType)) {
                jp.nextToken();
                return new StateDiffJson.NoChange<T>();
            }
            DeserializationDebug.LOGGER.error("Unknown change", changeType);
            return null;
        } else {

            if (!jp.getCurrentToken().isStructStart()) {
                DeserializationDebug.LOGGER.error("Not an object", jp.getCurrentToken());
                return null;
            }
            jp.nextToken();
            String changeType = jp.getCurrentName();

            if ("*".equals(changeType)) {
                if (!jp.nextToken().isStructStart()) {
                    DeserializationDebug.LOGGER.error("Not an object: ", jp.getCurrentToken());
                    return null;
                }
                jp.nextToken();
                T from = null;
                T to = null;
                if ("from".equals(jp.getCurrentName())) {
                    from = converter.apply(jp.nextTextValue());
                } else if ("to".equals(jp.getCurrentName())) {
                    to = converter.apply(jp.nextTextValue());
                } else {
                    DeserializationDebug.LOGGER.error("Unknown position: ", jp.getCurrentName());
                }
                jp.nextToken();
                if ("to".equals(jp.getCurrentName())) {
                    to = converter.apply(jp.nextTextValue());
                } else if ("from".equals(jp.getCurrentName())) {
                    from = converter.apply(jp.nextTextValue());
                } else {
                    DeserializationDebug.LOGGER.error("Unknown position: ", jp.getCurrentName());
                }
                if (from == null || to == null) {
                    DeserializationDebug.LOGGER.error("Not fully read for StateDiffJson.FullChange");
                    return null;
                }
                jp.nextToken(); jp.nextToken(); jp.nextToken();
                return new StateDiffJson.FullChange<T>(from, to);
            } else if ("-".equals(changeType)) {
                String value = jp.nextTextValue();
                jp.nextToken(); jp.nextToken();
                return new StateDiffJson.RemoveChange<T>(
                    converter.apply(value)
                );
            } else if ("+".equals(changeType)) {
                String value = jp.nextTextValue();
                jp.nextToken(); jp.nextToken();
                return new StateDiffJson.CreateChange<T>(
                    converter.apply(value)
                );
            }
            DeserializationDebug.LOGGER.error("Unknown change", changeType);
            return null;
        }
    }

    protected Map<Hex32, StateDiffJson.Change<Hex32>> readStorageDiffs(JsonParser jp) throws IOException {
        if (!jp.getCurrentToken().isStructStart()) {
            DeserializationDebug.LOGGER.error("Not an object", jp.getCurrentToken());
            return null;
        }

        Map<Hex32, StateDiffJson.Change<Hex32>> result = new HashMap<Hex32, StateDiffJson.Change<Hex32>>();

        jp.nextToken();
        while (!jp.currentToken().isStructEnd()) {
            Hex32 pos = Hex32.from(jp.getCurrentName());
            jp.nextToken();
            StateDiffJson.Change<Hex32> change = readChange(jp, Hex32.class, STORAGE_CONVERTER);
            result.put(pos, change);
        }

        return result;
    }
}
