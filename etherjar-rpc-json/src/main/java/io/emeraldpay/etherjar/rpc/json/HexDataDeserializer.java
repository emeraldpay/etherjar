package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.IOException;

public class HexDataDeserializer extends StdDeserializer<HexData> {

    public HexDataDeserializer() {
        super(HexData.class);
    }

    @Override
    public HexData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if ("0x".equals(value)) {
                return null;
            }
            try {
                return HexData.from(value);
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid HexData value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid HexData type: " + token);
    }
}
