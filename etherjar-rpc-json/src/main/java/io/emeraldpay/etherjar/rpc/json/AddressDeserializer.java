package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.domain.Address;

import java.io.IOException;

public class AddressDeserializer extends StdDeserializer<Address> {

    public AddressDeserializer() {
        super(Address.class);
    }

    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return Address.from(p.getValueAsString());
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid Address value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid Address type: " + token);
    }

    public static class FromKey extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return Address.from(key);
        }
    }
}
