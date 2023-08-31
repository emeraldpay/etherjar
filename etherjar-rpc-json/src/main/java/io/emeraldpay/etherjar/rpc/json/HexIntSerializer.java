package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;

/**
 * Serialize Long as Hex Quantity
 */
public class HexIntSerializer extends StdSerializer<Integer> {

    public HexIntSerializer() {
        super(Integer.class);
    }

    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(HexQuantity.from(value.longValue()).toHex());
        }
    }
}
