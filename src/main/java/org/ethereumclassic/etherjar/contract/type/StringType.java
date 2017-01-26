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

    public final static StringType DEFAULT = new StringType();

    final static Charset UTF8_CHARSET = Charset.forName(StandardCharsets.UTF_8.name());

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
    public static Optional<StringType> from(String str) {
        Objects.requireNonNull(str);

        if (!Objects.equals(str, "string"))
            return Optional.empty();

        return Optional.of(DEFAULT);
    }

    @Override
    public String getCanonicalName() {
        return "string";
    }

    @Override
    public HexData encode(String str) {
        return DynamicBytesType.DEFAULT.encode(
                str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String decode(HexData data) {
        ByteBuffer buffer = ByteBuffer.wrap(
                DynamicBytesType.DEFAULT.decode(data));

        try {
            return UTF8_CHARSET.newDecoder().decode(buffer).toString();
        } catch (CharacterCodingException e) {
            throw new RuntimeException(
                    "Incorrect 'UTF-8' character encoding: " + data, e);
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
