package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;

public class HexQuantityDeserializer extends StdDeserializer<HexQuantity> {

    public HexQuantityDeserializer() {
        super(HexQuantity.class);
    }

    @Override
    public HexQuantity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return HexQuantity.from(p.getValueAsString());
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid HexQuantity value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid HexQuantity type: " + token);
    }
}
