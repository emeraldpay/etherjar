package io.infinitape.etherjar.contract.type;

import io.infinitape.etherjar.model.Hex32;
import io.infinitape.etherjar.model.HexData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dynamic sized array with given wrapped static type.
 */
public class DynamicArrayType<T> implements DynamicType<T[]> {

    final static String NAME_POSTFIX = "[]";

    final static Pattern NAME_PATTERN = Pattern.compile("(.+)\\[]");

    /**
     * Try to parse an {@link DynamicArrayType} string representation (either canonical form or not).
     *
     * @param repo a {@link Type} parsers repository
     * @param str a string
     * @return an {@link DynamicArrayType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if an {@link DynamicArrayType} has invalid
     * input or not a {@link StaticType} wrapped type
     *
     * @see #getCanonicalName()
     */
    @SuppressWarnings("unchecked")
    public static Optional<DynamicArrayType> from(Type.Repository repo, String str) {
        if (!str.endsWith(NAME_POSTFIX))
            return Optional.empty();

        Matcher matcher = NAME_PATTERN.matcher(str);

        if (!matcher.matches())
            throw new IllegalArgumentException("Wrong dynamic array type format: " + str);

        Optional<Type> type = repo.search(matcher.group(1));

        if (!type.isPresent())
            throw new IllegalArgumentException(
                    "Unknown dynamic array wrapped type: " + matcher.group(1));

        if (type.get().isDynamic())
            throw new IllegalArgumentException(
                    "Dynamic array wrapped type is not static: " + type.get());

        return Optional.of(
                new DynamicArrayType<>((StaticType) type.get()));
    }

    private final StaticType<T> type;

    /**
     * Create a dynamic array for predefined {@link StaticType}.
     *
     * @param type an array wrapped {@link StaticType}
     */
    public DynamicArrayType(StaticType<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    public StaticType<T> getWrappedType() {
        return type;
    }

    @Override
    public String getCanonicalName() {
        return type.getCanonicalName() + "[]";
    }

    @Override
    public HexData encode(T[] arr) {
        List<HexData> buf = new ArrayList<>(arr.length + 1);

        buf.add(Type.encodeLength(arr.length));

        for (T obj : arr) {
            buf.add(getWrappedType().encode(obj));
        }

        return HexData.combine(buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] decode(HexData data) {
        int len = Type.decodeLength(
                data.extract(Hex32.SIZE_BYTES, Hex32::from)).intValueExact();

        if (data.getSize() != Hex32.SIZE_BYTES + getWrappedType().getFixedSize() * len)
            throw new IllegalArgumentException("Wrong data length to decode dynamic array: " + data);

        HexData[] arr = data.split(getWrappedType().getFixedSize(), Hex32.SIZE_BYTES);

        return (T[]) Arrays.stream(arr).map(it -> getWrappedType().decode(it)).toArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        DynamicArrayType other = (DynamicArrayType) obj;

        return Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
