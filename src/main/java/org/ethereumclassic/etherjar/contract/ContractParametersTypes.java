package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.contract.type.Type;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A fixed-size contract arguments array of different types.
 *
 * @see Type
 * @see ContractMethod
 */
public class ContractParametersTypes {

    public static final ContractParametersTypes EMPTY = new ContractParametersTypes();

    final static Pattern ABI_PATTERN = Pattern.compile("[a-z0-9<>\\[\\]]*(,[a-z0-9<>\\[\\]]+)*");

    /**
     * Check parameters array types ABI signature.
     *
     * @param signature a parameters signature string representation
     * @return {@code true} if <code>signature</code> is valid, otherwise
     * {@code false}
     *
     * @see #ABI_PATTERN
     */
    static boolean isAbiValid(String signature) {
        return ABI_PATTERN.matcher(signature).matches();
    }

    /**
     * Create an instance from signature like <tt>'address,uint256'</tt>.
     *
     * <p>The signature is defined as a string expression of a list of either canonical
     * or not parameter types, are split by a single comma - no spaces are used.
     *
     * @param repo a {@link Type} parsers repository
     * @param signature a parameters signature string representation
     * @return a {@link ContractParametersTypes} instance
     */
    public static ContractParametersTypes fromAbi(Type.Repository repo, String signature) {
        if (signature.isEmpty()) return EMPTY;

        Matcher m = ABI_PATTERN.matcher(signature);

        if (!m.matches())
            throw new IllegalArgumentException("Wrong ABI parameters types signature: " + signature);

        List<Type> types = new ArrayList<>();

        for (String str : signature.split(",")) {
            Optional<Type> type = repo.search(str);

            if (!type.isPresent())
                throw new IllegalArgumentException("Unknown parameter type format: " + str);

            types.add(type.get());
        }

        return new ContractParametersTypes(types);
    }

    private final List<Type> types;

    public ContractParametersTypes(Type... types) {
        this(Arrays.asList(types));
    }

    public ContractParametersTypes(Collection<? extends Type> types) {
        this.types = Collections.unmodifiableList(new ArrayList<>(types));
    }

    /**
     * Returns {@code true} if, and only if, {@link #getTypes()} is an empty list.
     *
     * @return {@code true} if {@link #getTypes()} length is {@code 0}, otherwise
     * {@code false}
     */
    public boolean isEmpty() {
        return types.isEmpty();
    }

    /**
     * Get a list of parameters types.
     *
     * @return the parameters types
     */
    public List<Type> getTypes() {
        return types;
    }

    /**
     * Get a fixed-size bytes required for encoding.
     *
     * @return a number of fixed-size bytes
     */
    public int getFixedSize() {
        return types.stream().mapToInt(Type::getFixedSize).sum();
    }

    /**
     * Encode arguments values according parameters types.
     *
     * @param params an array of parameters values
     * @return an encoded hex data
     *
     * @see #encode(Collection)
     */
    public HexData encode(Object... params) {
        return encode(Arrays.asList(params));
    }

    /**
     * Encode arguments values according parameters types.
     *
     * @param args a collection of arguments values
     * @return an encoded hex data
     *
     * @see #encode(Object...)
     */
    @SuppressWarnings("unchecked")
    public HexData encode(Collection<?> args) {
        if (types.size() != args.size())
            throw new IllegalArgumentException("Wrong number of input parameters: " + args.size());

        long headBytesSize = getFixedSize();
        long tailBytesSize = 0;

        List<HexData> buf = new ArrayList<>();
        List<HexData> tail = new ArrayList<>();

        int i = 0;

        for (Object obj : args) {
            Type type = types.get(i++);

            HexData data = type.encode(obj);

            if (type.isStatic()) {
                buf.add(data);
            } else {
                buf.add(Type.encodeLength(headBytesSize + tailBytesSize));

                tailBytesSize += data.getSize();
                tail.add(data);
            }
        }

        buf.addAll(tail);

        return HexData.combine(buf);
    }

    /**
     * Decode a response hex data into a list of object values.
     *
     * @param data a hex data
     * @return a list of decoded objects
     *
     * @see #encode(Object...)
     * @see #encode(Collection)
     */
    public List<Object> decode(HexData data) {
        int headBytesSize = getFixedSize();

        if (data.getSize() < headBytesSize)
            throw new IllegalArgumentException(
                    "Insufficient data length to decode: " + data.getSize());

        List<Object> buf = new ArrayList<>(types.size());

        int headBytesPos = headBytesSize;
        int tailBytesPos = data.getSize();

        for (int i = types.size() - 1; i >= 0; i--) {
            Type type = types.get(i);

            headBytesPos -= type.getFixedSize();

            HexData chunk = data.extract(type.getFixedSize(), headBytesPos);

            if (type.isStatic()) {
                buf.add(type.decode(chunk));
            } else {
                int offset = Type.decodeLength(Hex32.from(chunk)).intValueExact();

                if (offset < headBytesSize || offset >= tailBytesPos)
                    throw new IllegalArgumentException("Illegal tail bytes offset: " + offset);

                buf.add(type.decode(data.extract(tailBytesPos - offset, offset)));

                tailBytesPos = offset;
            }
        }

        if (tailBytesPos != headBytesSize)
            throw new IllegalStateException("Wrong tail part of data to decode: " + data.toHex());

        Collections.reverse(buf);

        return Collections.unmodifiableList(buf);
    }

    /**
     * Get an array of parameters types canonical (ABI) names.
     *
     * @return a list of strings
     */
    public String[] toCanonicalNames() {
        return types.stream().map(Type::getCanonicalName).toArray(String[]::new);
    }

    /**
     * ABI encoded array of parameters types.
     *
     * @return a string
     */
    public String toAbi() {
        return types.stream()
                .map(Type::getCanonicalName).collect(Collectors.joining(","));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), types);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        ContractParametersTypes other = (ContractParametersTypes) obj;

        return Objects.equals(types, other.types);
    }

    @Override
    public String toString() {
        return String.format("%s{types=%s}", getClass().getSimpleName(), types);
    }
}
