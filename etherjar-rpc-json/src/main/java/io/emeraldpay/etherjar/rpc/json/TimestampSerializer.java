package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.io.IOException;
import java.time.Instant;

/**
 * Serialize Instant as Epoch Seconds encoded as a Hex Quantity
 */
public class TimestampSerializer extends StdSerializer<Instant> {

    public TimestampSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            long seconds = value.getEpochSecond();
            gen.writeString(HexQuantity.from(seconds).toHex());
        }
    }
}
