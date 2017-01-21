package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ethereumclassic.etherjar.model.HexQuantity;

import java.io.IOException;

/**
 * @author Igor Artamonov
 */
public class TransactionCallJsonSerializer extends JsonSerializer<TransactionCallJson> {

    @Override
    public void serialize(TransactionCallJson value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartObject();
        if (value.getFrom() != null) {
            gen.writeFieldName("from");
            gen.writeString(value.getFrom().toHex());
        }
        if (value.getTo() != null) {
            gen.writeFieldName("to");
            gen.writeString(value.getTo().toHex());
        }
        if (value.getGas() != null) {
            gen.writeFieldName("gas");
            gen.writeString(HexQuantity.from(value.getGas()).toHex());
        }
        if (value.getGasPrice() != null) {
            gen.writeFieldName("gasPrice");
            gen.writeString(value.getGasPrice().toHex());
        }
        if (value.getValue() != null) {
            gen.writeFieldName("value");
            gen.writeString(value.getValue().toHex());
        }
        if (value.getData() != null) {
            gen.writeFieldName("data");
            gen.writeString(value.getData().toHex());
        }
        gen.writeEndObject();
    }
}
