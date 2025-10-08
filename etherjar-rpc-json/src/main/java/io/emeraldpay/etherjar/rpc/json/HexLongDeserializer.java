package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;
import java.util.Objects;

/**
 * Deserialize Long from Hex Quantity
 */
public class HexLongDeserializer extends StdDeserializer<Long> {

    public HexLongDeserializer() {
        super(Long.class);
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return HexQuantity.from(p.getValueAsString()).getValue().longValueExact();
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid HexQuantity value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return p.getValueAsLong();
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid HexQuantity type: " + token);
    }

    public static class FromKey extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            if (key == null || key.isEmpty()) {
                return null;
            }
            return Objects.requireNonNull(HexQuantity.from(key)).getValue().longValueExact();
        }
    }
}
