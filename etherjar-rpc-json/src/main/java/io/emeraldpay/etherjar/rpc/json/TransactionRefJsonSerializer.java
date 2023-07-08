package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class TransactionRefJsonSerializer extends StdSerializer<TransactionRefJson> {

    public TransactionRefJsonSerializer() {
        super(TransactionRefJson.class);
    }

    @Override
    public void serialize(TransactionRefJson value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            if (value instanceof TransactionJson) {
                gen.writeObject(value);
            } else {
                gen.writeString(value.getHash().toHex());
            }
        }
    }
}
