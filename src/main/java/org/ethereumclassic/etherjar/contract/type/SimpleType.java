package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;

/**
 * Fixed-size static elementary types (32 bytes length only).
 */
public interface SimpleType<T> extends StaticType<T> {

    @Override
    default int getFixedSize() {
        return Hex32.SIZE_BYTES;
    }

    @Override
    default HexData encode(T obj) {
        return encodeStatic(obj);
    }

    @Override
    default T decode(HexData data) {
        if (data.getSize() != getFixedSize())
            throw new IllegalArgumentException(
                    "Wrong hex data length to decode: " + data.getSize());

        return decodeStatic(Hex32.from(data));
    }

    /**
     * Encode an object to a {@link Hex32}.
     *
     * @param obj an object
     * @return encoded hex
     *
     * @see #decodeStatic(Hex32)
     */
    Hex32 encodeStatic(T obj);

    /**
     * Decode a {@link Hex32} to an object.
     *
     * @param hex32 a hex32
     * @return decoded object
     * @see #encodeStatic(Object)
     */
    T decodeStatic(Hex32 hex32);
}
