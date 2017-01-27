package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Binary type of fixed bytes.
 */
public class BytesType implements SimpleType<byte[]> {

    public final static BytesType DEFAULT = new BytesType();

    public final static BytesType DEFAULT_ONE_BYTE = new BytesType(1);

    final static Map<Integer, BytesType> CACHED_INSTANCES =
            Stream.of(1, 2, 4, 8, 16, 32).collect(Collectors.collectingAndThen(
                    Collectors.toMap(Function.identity(), BytesType::new), Collections::unmodifiableMap));

    final static String NAME_PREFIX = "bytes";

    final static String NAME_PREFIX_FOR_ONE_BYTE = "byte";

    final static Pattern NAME_PATTERN = Pattern.compile("bytes(\\d{1,2})");

    /**
     * Try to parse a {@link BytesType} string representation (either canonical form or not).
     *
     * @param str a string
     * @return a {@link BytesType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     *
     * @see #getCanonicalName()
     */
    public static Optional<BytesType> from(String str) {
        if (!(str.startsWith(NAME_PREFIX) && !str.equals(NAME_PREFIX))
                && !str.equals(NAME_PREFIX_FOR_ONE_BYTE))
            return Optional.empty();

        if (str.equals(NAME_PREFIX_FOR_ONE_BYTE))
            return Optional.of(DEFAULT_ONE_BYTE);

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'bytes<M>' type format: " + str);

        int bytes = Integer.parseInt(matcher.group(1));

        return Optional.of(CACHED_INSTANCES.containsKey(bytes) ?
                CACHED_INSTANCES.get(bytes) : new BytesType(bytes));
    }

    private final int length;

    public BytesType() {
        this(32);
    }

    public BytesType(int length) {
        if (length <= 0 || length > 32)
            throw new IllegalArgumentException("Illegal bytes type length: " + length);

        this.length = length;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getCanonicalName() { return "bytes" + length; }

    @Override
    public Hex32 encodeSimple(byte... bytes) {
        if (bytes.length != length)
            throw new IllegalArgumentException("Wrong bytes length to encode: " + bytes.length);

        return new Hex32(Arrays.copyOf(bytes, Hex32.SIZE_BYTES));
    }

    @Override
    public byte[] decodeSimple(Hex32 hex32) {
        return Arrays.copyOf(hex32.getBytes(), length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        BytesType other = (BytesType) obj;

        return length == other.length;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
