package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.IOException;

public class DirectHex32Deserializer extends StdDeserializer<Hex32> {

    public DirectHex32Deserializer() {
        super(Hex32.class);
    }

    @Override
    public Hex32 deserialize(JsonParser p, DeserializationContext
    ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                String value = p.getValueAsString();
                if (value.startsWith("0x")) {
                    return Hex32.from(value);
                }
                return Hex32.from(HexData.fromDirect(value).getBytes());
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid Hex32 value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid Hex32 type: " + token);
    }

    public static class FromKey extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try {
                if (key.startsWith("0x")) {
                    return Hex32.from(key);
                }
                return Hex32.from(HexData.fromDirect(key).getBytes());
            } catch (Throwable t) {
                t.printStackTrace();
                throw new InvalidFormatException(ctxt.getParser(), "Invalid Hex32 value", key, Hex32.class);
            }
        }
    }
}
