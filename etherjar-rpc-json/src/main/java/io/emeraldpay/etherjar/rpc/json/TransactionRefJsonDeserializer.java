package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.emeraldpay.etherjar.domain.TransactionId;

import java.io.IOException;

public class TransactionRefJsonDeserializer extends StdDeserializer<TransactionRefJson> {

    protected TransactionRefJsonDeserializer() {
        super(TransactionRefJson.class);
    }
    @Override
    public TransactionRefJson deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            try {
                return new TransactionRefJson(TransactionId.from(p.getValueAsString()));
            } catch (Throwable t) {
                throw JsonMappingException.from(p,"Invalid TransactionId value: " + p.getValueAsString(), t);
            }
        }
        if (token == JsonToken.START_OBJECT) {
            return ctxt.readValue(p, TransactionJson.class);
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        throw JsonMappingException.from(p,"Invalid Transaction Ref type: " + token);
    }

}
