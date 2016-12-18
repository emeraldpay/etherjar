package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.contract.type.Type;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A smart contract methods (either a constructor or a function).
 *
 * @author Igor Artamonov
 * @see Contract
 */
public class ContractMethod {

    public static class Builder {

        final static Pattern ABI_PATTERN = Pattern.compile("([_a-zA-Z][_a-zA-Z0-9]*)\\((\\S*)\\)");

        /**
         * Check contract method ABI signature.
         *
         * @param signature a contract method signature string representation
         * @return {@code true} if <code>signature</code> is valid, otherwise
         * {@code false}
         *
         * @see #ABI_PATTERN
         */
        static boolean isAbiValid(String signature) {
            return ABI_PATTERN.matcher(signature).matches();
        }

        /**
         * Create a {@link Builder} instance from methods signature like
         * <tt>name(datatype1,datatype2)</tt>, or <tt>transfer(address,uint256)</tt>.
         *
         * <p>The signature is defined as the canonical expression of the basic prototype,
         * i.e. the function name with the parenthesised list of parameter types.
         * Parameter types are split by a single comma - no spaces are used.
         *
         * @param repo a {@link Type} parsers repository
         * @param signature a contract method signature string representation
         * @return builder instance
         */
        public static Builder fromAbi(Type.Repository repo, String signature) {
            Matcher m = ABI_PATTERN.matcher(signature);

            if (!m.matches())
                throw new IllegalArgumentException("Wrong ABI method signature: " + signature);

            String name = m.group(1);

            List<Type> types = new ArrayList<>();

            for (String str : m.group(2).split(",")) {
                Optional<Type> type = repo.search(str);

                if (!type.isPresent())
                    throw new IllegalArgumentException("Unknown input parameter type format: " + str);

                types.add(type.get());
            }

            return new Builder().withName(name).expects(types);
        }

        private String name = null;

        private boolean isConstant = false;

        private Collection<? extends Type> inputTypes = Collections.emptyList();

        private Collection<? extends Type> outputTypes = Collections.emptyList();

        /**
         * @param name a contract methods name
         * @return builder instance
         */
        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name);

            return this;
        }

        /**
         * Mark a contract methods as a constant methods.
         *
         * @return builder instance
         */
        public Builder asConstant() {
            isConstant = true;

            return this;
        }

        /**
         * @param types a contract methods input types
         * @return builder instance
         */
        public Builder expects(Type... types) {
            return expects(Arrays.asList(types));
        }

        /**
         * @param types a contract methods input types
         * @return builder instance
         */
        public Builder expects(Collection<? extends Type> types) {
            inputTypes = Objects.requireNonNull(types);

            return this;
        }

        /**
         * @param types a contract methods output types
         * @return builder instance
         */
        public Builder returns(Type... types) {
            return returns(Arrays.asList(types));
        }

        /**
         * @param types a contract methods output types
         * @return builder instance
         */
        public Builder returns(Collection<? extends Type> types) {
            outputTypes = Objects.requireNonNull(types);

            return this;
        }

        /**
         * Build a {@link ContractMethod} instance with predefined conditions.
         *
         * @return a {@link ContractMethod} instance
         */
        public ContractMethod build() {
            if (Objects.isNull(name))
                throw new IllegalStateException("Undefined contract method name");

            return new ContractMethod(name, isConstant, inputTypes, outputTypes);
        }
    }

    /**
     * Convert a type collection to a collection of canonical names.
     *
     * @param types a {@link Collection} of {@link Type}
     * @return a {@link Collection} of {@link String}
     */
    static Collection<String> convert(Collection<? extends Type> types) {
        return types.stream().map(Type::getCanonicalName).collect(Collectors.toList());
    }

    /**
     * Join a type collection to a string with the help of {@code delimiter}.
     *
     * @param types a {@link Collection} of {@link Type}
     * @param delimiter the delimiter to be used between each names
     * @return a joined by {@code delimiter} string
     */
    static String join(Collection<? extends Type> types, CharSequence delimiter) {
        return types.stream().map(Type::getCanonicalName).collect(Collectors.joining(delimiter));
    }

    private final MethodId id;

    private final String name;

    private final boolean isConstant;

    private final List<Type> inputTypes;

    private final List<Type> outputTypes;

    private final transient long headFixedSize;

    public ContractMethod(String name, Type... inputTypes) {
        this(name, false, Arrays.asList(inputTypes), Collections.emptyList());
    }

    public ContractMethod(String name, Collection<? extends Type> inputTypes) {
        this(name, false, inputTypes, Collections.emptyList());
    }

    public ContractMethod(String name, boolean isConstant, Type... inputTypes) {
        this(name, isConstant, Arrays.asList(inputTypes), Collections.emptyList());
    }

    public ContractMethod(String name, boolean isConstant, Collection<? extends Type> inputTypes) {
        this(name, isConstant, inputTypes, Collections.emptyList());
    }

    public ContractMethod(String name, boolean isConstant,
                          Collection<? extends Type> inputTypes,
                          Collection<? extends Type> outputTypes) {
        this.id = MethodId.fromSignature(name, convert(inputTypes));
        this.name = Objects.requireNonNull(name);
        this.isConstant = isConstant;
        this.inputTypes = Collections.unmodifiableList(new ArrayList<>(inputTypes));
        this.outputTypes = Collections.unmodifiableList(new ArrayList<>(outputTypes));
        /* FIXME: extract this logic into separate array related type class */
        this.headFixedSize = inputTypes.stream().mapToLong(Type::getFixedSize).sum();
    }

    /**
     * @return the methods id
     */
    public MethodId getId() {
        return id;
    }

    /**
     * @return the method name
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@code true} if this method change contract's state,
     * otherwise {@code false}
     *
     * @see Contract
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the methods parameter types
     */
    public List<Type> getInputTypes() {
        return inputTypes;
    }

    /**
     * @return the methods return types
     */
    public List<Type> getOutputTypes() {
        return outputTypes;
    }

    /**
     * Encodes call data, so you can call the contract through some other means (for example, through RPC).
     *
     * @param params parameters of the call
     * @return {@link HexData} encoded call
     *
     * @see #encodeCall(Collection)
     */
    public HexData encodeCall(Object... params) {
        return encodeCall(Arrays.asList(params));
    }

    /**
     * Encodes call data, so you can call the contract through some other means (for example, through RPC).
     *
     * <p><b>Example:</b> <code>baz(uint32,bool)</code> with arguments <tt>(69, true)</tt> becomes
     * <tt>0xcdcd77c000000000000000000000000000000000000000000000000000000000000000450000000000000000000000000000000000000000000000000000000000000001</tt>
     *
     * @param params parameters of the call
     * @return {@link HexData} encoded call
     *
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#function-selector-and-argument-encoding">Function Selector and Argument Encoding</a>
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#examples">Examples</a>
     */
    @SuppressWarnings("unchecked")
    public HexData encodeCall(Collection<?> params) {
        if (inputTypes.size() != params.size())
            throw new IllegalArgumentException("Wrong number of input parameters: " + params.size());

        List<HexData> buf =
                new ArrayList<>((int) (headFixedSize / Hex32.SIZE_BYTES) + 1);

        buf.add(id);

        int i = 0;

        long tailBytesSize = 0;

        List<Hex32> tail = new ArrayList<>();

        for (Object obj : params) {
            Type type = inputTypes.get(i++);

            List<Hex32> data = type.encode(obj);

            if (type.isStatic()) {
                buf.addAll(data);
            } else {
                buf.add(Type.encodeLength(headFixedSize + tailBytesSize));

                tailBytesSize += data.size() * Hex32.SIZE_BYTES;
                tail.addAll(data);
            }
        }

        buf.addAll(tail);

        return HexData.from(buf);
    }

    /**
     * ABI encoded contract method signature.
     *
     * @return a string
     */
    public String toAbi() {
        return name + '(' + join(inputTypes, ",") + ')';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj)) return false;

        if (!Objects.equals(getClass(), obj.getClass()))
            return false;

        ContractMethod other = (ContractMethod) obj;

        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%s,name=%s,isConstant=%b,expects=%s,returns=%s}",
                getClass().getSimpleName(), id, name, isConstant, inputTypes, outputTypes);
    }
}
