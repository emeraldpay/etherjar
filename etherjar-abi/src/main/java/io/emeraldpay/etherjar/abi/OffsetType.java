package io.emeraldpay.etherjar.abi;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

public class OffsetType implements DynamicType<HexData> {

    private static final HexData ZERO_START = HexData.from("0x0000000000000000000000000000000000000000000000000000000000000020");

    @Override
    public String getCanonicalName() {
        return "byte[]";
    }

    @Override
    public HexData encode(HexData obj) {
        return ZERO_START.concat(obj);
    }

    @Override
    public HexData decode(HexData data) {
        if (data.getSize() <= Hex32.SIZE_BYTES)
            throw new IllegalArgumentException("Wrong data length to decode offset bytes: " + data.getSize());

        int offset = Type.decodeLength(data.extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        if (data.getSize() < offset)
            throw new IllegalArgumentException("Wrong data length to decode offset bytes: " + data.getSize());

        return data.skip(offset);
    }
}
