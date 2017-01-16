package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.HexData;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Dynamic sized unicode string assumed to be UTF-8 encoded.
 */
public class StringType implements DynamicType<String> {

    final static Charset UTF_8_CHARSET = Charset.forName(StandardCharsets.UTF_8.name());

    final static BytesType BYTES_TYPE = new BytesType();

    /**
     * Try to parse a {@link StringType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link StringType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<BytesType> from(String str) {
        Objects.requireNonNull(str);

        return Objects.equals(str, "string") ?
                Optional.of(new BytesType()) : Optional.empty();
    }

    @Override
    public String getCanonicalName() {
        return "string";
    }

    @Override
    public HexData encode(String str) {
        return BYTES_TYPE.encode(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String decode(HexData data) {
        ByteBuffer buffer = ByteBuffer.wrap(BYTES_TYPE.decode(data));

        try {
            return UTF_8_CHARSET.newDecoder().decode(buffer).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Incorrect 'UTF-8' character encoding: " + data);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        return Objects.equals(getClass(), obj.getClass());
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
