package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Serialize BigInteger as Hex Quantity
 */
public class BigIntegerSerializer extends StdSerializer<BigInteger> {

    public BigIntegerSerializer() {
        super(BigInteger.class);
    }

    @Override
    public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(HexQuantity.from(value).toHex());
        }
    }
}
