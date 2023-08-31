package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;

/**
 * Deserialize Long from Hex Quantity
 */
public class HexIntDeserializer extends StdDeserializer<Integer> {

    public HexIntDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return HexQuantity.from(p.getValueAsString()).getValue().intValueExact();
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid HexQuantity value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return p.getValueAsInt();
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid HexQuantity type: " + token);
    }
}
