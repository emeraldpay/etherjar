package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;

public class HexQuantitySerializer  extends StdSerializer<HexQuantity> {

    public HexQuantitySerializer() {
        super(HexQuantity.class);
    }

    @Override
    public void serialize(HexQuantity value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toHex());
        }
    }
}
