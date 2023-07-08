package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.IOException;

public class Hex32Deserializer extends StdDeserializer<Hex32> {

    public Hex32Deserializer() {
        super(Hex32.class);
    }

    @Override
    public Hex32 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return Hex32.from(p.getValueAsString());
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid Hex32 value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid Hex32 type: " + token);
    }
}
