package io.emeraldpay.etherjar.abi;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

/**
 * Get data specified with an offset. I.e., the value itself is encoded after the initial array of 32-byte
 * elements, and the element at the current position is specifies the offset of the actual data. Used to encode structs
 * and arrays.
 *
 * Example:
 * <pre><code>
 * 0x0000000000000000000000000000000000000000000000000000000000000020
 *   0000000000000000000000000000000000000000000000000000000000000002
 *   00000000000000000000000000000000000000000000000000000000000000e1
 *   00000000000000000000000000000000000000000000000000000000000000e2
 * </code></pre>
 *
 * The first item there is an offset (<code>0x20</code> or 32 bytes) of the actual data, which right after
 * that. I.e. the encoded data is 1-element type of array with 2 items in it; and the encoded data
 * consists of _list of 32-byte items_ (1 elements) + additional data referenced by position (array starting after 0x20 bytes).
 *
 * Example:
 * <pre><code>
 * 0x00000000000000000000000000000000000000000000000000000000000000f1
 *   0000000000000000000000000000000000000000000000000000000000000060
 *   00000000000000000000000000000000000000000000000000000000000000f3
 *   0000000000000000000000000000000000000000000000000000000000000002
 *   00000000000000000000000000000000000000000000000000000000000000e1
 *   00000000000000000000000000000000000000000000000000000000000000e2
 * </code></pre>
 *
 * In that case we have 3-element value, where the second element is an array specified as a reference in the data blob.
 * I.e. the first element is <code>0xf1</code>, the second is only an offset <code>0x60</code> (i.e. the actual data starts
 * after the initial 96 bytes) and the third element is <code>0xf3</code>.
 */
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
        return decode(0, data);
    }

    /**
     * Read data specified with an offset, where the offset itself start not at the beginning. I.e., when
     * it's not a first element of an array.
     *
     * @param offset offset to the value specifying the position of the actual data
     * @param data full data
     * @return actual data at the offset
     */
    public HexData decode(int offset, HexData data) {
        if (data.getSize() + offset <= Hex32.SIZE_BYTES)
            throw new IllegalArgumentException("Wrong data length to decode offset bytes: " + data.getSize());

        int dataOffset = Type.decodeLength(data.skip(offset).extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        if (data.getSize() < dataOffset)
            throw new IllegalArgumentException("Wrong data length to decode offset bytes: " + data.getSize());

        return data.skip(dataOffset);
    }
}
