package org.ethereumclassic.etherjar.contract.type;

import org.ethereumclassic.etherjar.model.Hex32;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  An array of the given wrapped {@link Type}.
 */
public class ArrayType<T> implements ReferenceType<T[], T> {

    final static String NAME_POSTFIX = "]";

    final static Pattern NAME_PATTERN = Pattern.compile("(.+)\\[(\\d*)]");

    /**
     * Try to parse a {@link ArrayType} string representation (either canonical form or not).
     *
     * @param repo a {@link Type} parsers repository
     * @param str a string
     * @return an {@link ArrayType} instance is packed as {@link Optional} value,
     * or {@link Optional#empty()} instead
     * @throws NullPointerException if a {@code str} is <code>null</code>
     * @throws IllegalArgumentException if an {@link ArrayType} has invalid input
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

    private final long length;

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
    public ArrayType(Type<T> type, long length) {
        this.type = Objects.requireNonNull(type);
        this.length = length <= 0 ? -1 : length;
    }

    @Override
    public Type<T> getWrappedType() {
        return type;
    }

    @Override
    public OptionalLong getFixedLength() {
        return length == -1 ? OptionalLong.empty() : OptionalLong.of(length);
    }

    @Override
    public String getCanonicalName() {
        return type.getCanonicalName() +
                '[' + (getFixedLength().isPresent() ? getFixedLength().getAsLong() : "") + ']';
    }

    @Override
    public List<? extends Hex32> encode(T[] arr) {
        if (arr.length == 0)
            throw new IllegalArgumentException("Empty array to encode");

        if (getFixedLength().isPresent() && arr.length != getFixedLength().getAsLong())
            throw new IllegalArgumentException("Wrong array length to encode: " + arr.length);

        List<Hex32> buf = new ArrayList<>();

        if (!getFixedLength().isPresent()) {
            buf.add(Type.encodeLength(arr.length));
        }

        if (getWrappedType().isStatic()) {
            for (T obj : arr) {
                buf.addAll(getWrappedType().encode(obj));
            }

            return buf;
        }

        long headBytesSize = arr.length * Hex32.SIZE_BYTES;

        long tailBytesSize = 0;

        List<Hex32> tail = new ArrayList<>();

        for (T obj : arr) {
            List<? extends Hex32> data = getWrappedType().encode(obj);

            buf.add(Type.encodeLength(headBytesSize + tailBytesSize));

            tailBytesSize += data.size() * Hex32.SIZE_BYTES;
            tail.addAll(data);
        }

        buf.addAll(tail);

        return buf;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] decode(Collection<? extends Hex32> data) {
        if (data.isEmpty())
            throw new IllegalArgumentException("Empty data to decode");

        List<? extends Hex32> list = data instanceof List ?
                (List<? extends Hex32>) data : new ArrayList<>(data);

        ListIterator<? extends Hex32> iter = list.listIterator();

        int len = getFixedLength().isPresent() ?
                (int) getFixedLength().getAsLong() : Type.decodeLength(iter.next()).intValueExact();

        int size = (int) (getWrappedType().getFixedSize() / Hex32.SIZE_BYTES);

        if (list.size() < iter.nextIndex() + len * size)
            throw new IllegalArgumentException("Insufficient data length to decode: " + list.size());

        T[] buf = (T[]) new Object[len];

        if (getWrappedType().isStatic()) {
            if (list.size() > iter.nextIndex() + len * size)
                throw new IllegalArgumentException("Redundant data length to decode: " + list.size());

            for (int i = 0; i < len; i++) {
                buf[i] = getWrappedType().decode(
                        list.subList(iter.nextIndex() + i * size, iter.nextIndex() + (i + 1) * size));
            }

            return buf;
        }

        int headBytesOffset = iter.nextIndex() * Hex32.SIZE_BYTES;

        int endBytesOffset = list.size() * Hex32.SIZE_BYTES - headBytesOffset;

        int fromBytesOffset = Type.decodeLength(iter.next()).intValueExact();

        int toBytesOffset;

        if (fromBytesOffset != len * Hex32.SIZE_BYTES)
            throw new IllegalArgumentException("Illegal first dynamic bytes offset: " + fromBytesOffset);

        for (int i = 0; i < len; i++, fromBytesOffset = toBytesOffset) {
            toBytesOffset = i == len - 1 ?
                    endBytesOffset : Type.decodeLength(iter.next()).intValueExact();

            if (list.size() < (headBytesOffset + toBytesOffset) / Hex32.SIZE_BYTES)
                throw new IllegalArgumentException("Insufficient data length to decode: " + list.size());

            if (fromBytesOffset >= toBytesOffset)
                throw new IllegalArgumentException(
                        String.format("Illegal dynamic bytes offsets: from %d, to %d",
                                fromBytesOffset, toBytesOffset));

            buf[i] = getWrappedType().decode(
                    list.subList((headBytesOffset + fromBytesOffset) / Hex32.SIZE_BYTES,
                            (headBytesOffset + toBytesOffset) / Hex32.SIZE_BYTES));
        }

        return buf;
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
