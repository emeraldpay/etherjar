package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  An array of a given wrapped static type.
 */
public class ArrayType<T> implements ReferenceType<T[], T> {

    final static String NAME_POSTFIX = "]";

    final static Pattern NAME_PATTERN = Pattern.compile("(.+)\\[(\\d*)]");

    /**
     * Try to parse an {@link ArrayType} string representation (either canonical form or not).
     *
     * @param repo a {@link Type} parsers repository
     * @param str a string
     * @return an {@link ArrayType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if an {@link ArrayType} has invalid
     * input or not a {@link StaticType} wrapped type
     *
     * @see #getCanonicalName()
     */
    @SuppressWarnings("unchecked")
    public static Optional<ArrayType> from(Type.Repository repo, String str) {
        if (!str.endsWith(NAME_POSTFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong 'array' type format: " + str);

        Optional<Type> type = repo.search(matcher.group(1));

        if (!type.isPresent())
            throw new IllegalArgumentException(
                    "Unknown array wrapped type: " + matcher.group(1));

        String digits = matcher.group(2);

        return Optional.of(digits.isEmpty() ?
                new ArrayType(type.get()) : new ArrayType(type.get(), Integer.parseInt(digits)));
    }

    private final Type<T> type;

    private final int length;

    /**
     * Create a dynamic array without fixed length.
     *
     * @param type an array wrapped {@link Type}
     */
    public ArrayType(Type<T> type) {
        this(type, -1);
    }

    /**
     * Create a static array with a fixed length.
     *
     * @param type an array wrapped {@link Type}
     * @param length a fixed number of array elements, should be positive
     */
    public ArrayType(Type<T> type, int length) {
        if (type.isDynamic())
            throw new IllegalArgumentException("Array wrapped type is not static: " + type);

        this.type = type;
        this.length = length <= 0 ? -1 : length;
    }

    @Override
    public Type<T> getWrappedType() {
        return type;
    }

    @Override
    public OptionalInt getLength() {
        return length == -1 ? OptionalInt.empty() : OptionalInt.of(length);
    }

    @Override
    public String getCanonicalName() {
        return type.getCanonicalName() +
                '[' + (getLength().isPresent() ? getLength().getAsInt() : "") + ']';
    }

    @Override
    public HexData encode(T[] arr) {
        if (arr.length == 0)
            throw new IllegalArgumentException("Empty array to encode");

        if (getLength().isPresent() && arr.length != getLength().getAsInt())
            throw new IllegalArgumentException("Wrong array length to encode: " + arr.length);

        List<HexData> buf = new ArrayList<>();

        if (!getLength().isPresent()) {
            buf.add(Type.encodeLength(arr.length));
        }

        for (T obj : arr) {
            buf.add(getWrappedType().encode(obj));
        }

        return HexData.combine(buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] decode(HexData data) {
        int len = getLength().isPresent() ? getLength().getAsInt() :
                Type.decodeLength(data.extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        HexData[] arr = data.split(
                getWrappedType().getFixedSize(), getLength().isPresent() ? 0 : Hex32.SIZE_BYTES);

        if (arr.length != len)
            throw new IllegalArgumentException("Wrong data length to decode: " + arr.length);

        return (T[]) Arrays.stream(arr).map(it -> getWrappedType().decode(it)).toArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), type, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        ArrayType other = (ArrayType) obj;

        return Objects.equals(type, other.type)
                && length == other.length;
    }

    @Override
    public String toString() {
        return String.format("%s{type=%s,length=%d}",
                getClass().getSimpleName(), type, length);
    }
}
