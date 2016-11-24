package org.ethereumclassic.etherjar.contract;

import org.ethereumclassic.etherjar.contract.type.Type;
import org.ethereumclassic.etherjar.model.Hex32;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.MethodId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A smart contract method (a constructor or a function).
 *
 * @author Igor Artamonov
 * @see Contract
 */
public class ContractMethod {

    public static class Builder {

        private MethodId id;

        private String name;

        private boolean isConstant;

        private Collection<? extends Type> inputTypes;

        private Collection<? extends Type> outputTypes;

        /**
         * Build from canonical method signature like <tt>name(datatype1,datatype2)</tt>,
         * or <tt>transfer(address,uint256)</tt>.
         *
         * <p>Make sure you're using canonical name type, e.g <tt>uint256</tt>
         * instead of simple <tt>uint</tt>
         *
         * <p>The signature is defined as the canonical expression of the basic prototype,
         * i.e. the function name with the parenthesised list of parameter types.
         * Parameter types are split by a single comma - no spaces are used.
         *
         * @param signature canonical signature
         * @return contract method instance
         * @see MethodId#fromSignature(String)
         */
        public Builder withSignature(String signature) {
            id = MethodId.fromSignature(signature);

            return this;
        }

        /**
         * @param name a contract method name
         * @return the current builder object
         */
        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name);

            return this;
        }

        /**
         * Mark a contract method as a constant method.
         *
         * @return the current builder object
         */
        public Builder asConstant() {
            isConstant = true;

            return this;
        }

        /**
         * @param types a contract method input types
         * @return the current builder object
         */
        public Builder accepts(Type... types) {
            return accepts(Arrays.asList(types));
        }

        /**
         * @param types a contract method input types
         * @return the current builder object
         */
        public Builder accepts(Collection<? extends Type> types) {
            inputTypes = Objects.requireNonNull(types);

            return this;
        }

        /**
         * @param types a contract method output types
         * @return the current builder object
         */
        public Builder returns(Type... types) {
            return returns(Arrays.asList(types));
        }

        /**
         * @param types a contract method output types
         * @return the current builder object
         */
        public Builder returns(Collection<? extends Type> types) {
            outputTypes = Objects.requireNonNull(types);

            return this;
        }

        /**
         * Build a {@link ContractMethod} object with predefined by builder conditions.
         *
         * @return a {@link ContractMethod} object
         */
        public ContractMethod build() {
            if (Objects.isNull(id) && Objects.isNull(name))
                throw new IllegalStateException("Contract method id is absent");

            if (Objects.isNull(id)) {
                id = MethodId.fromSignature(toSignature(name, inputTypes));
            }

            return new ContractMethod(id, isConstant, inputTypes, outputTypes);
        }
    }

    public static String toSignature(String name, Type... inputTypes) {
        return toSignature(name, Arrays.asList(inputTypes));
    }

    public static String toSignature(String name, Collection<? extends Type> inputTypes) {
        Objects.requireNonNull(name);

        String args = inputTypes.stream()
                .map(Type::getName).collect(Collectors.joining(","));

        return name + '(' + args + ')';
    }

    private final MethodId id;

    private final boolean isConstant;

    private final List<Type> inputTypes;

    private final List<Type> outputTypes;

    public ContractMethod(MethodId id, Type... inputTypes) {
        this(id, false, Arrays.asList(inputTypes), Collections.emptyList());
    }

    public ContractMethod(MethodId id, Collection<? extends Type> inputTypes) {
        this(id, false, inputTypes, Collections.emptyList());
    }

    public ContractMethod(MethodId id, boolean isConstant, Type... inputTypes) {
        this(id, isConstant, Arrays.asList(inputTypes), Collections.emptyList());
    }

    public ContractMethod(MethodId id, boolean isConstant, Collection<? extends Type> inputTypes) {
        this(id, isConstant, inputTypes, Collections.emptyList());
    }

    public ContractMethod(MethodId id, boolean isConstant,
                          Collection<? extends Type> inputTypes,
                          Collection<? extends Type> outputTypes) {
        if (id == null)
            throw new IllegalArgumentException("Null contract method id");

        this.id = id;
        this.isConstant = isConstant;
        this.inputTypes = Collections.unmodifiableList(new ArrayList<>(inputTypes));
        this.outputTypes = Collections.unmodifiableList(new ArrayList<>(outputTypes));
    }

    /**
     * @return the method id
     */
    public MethodId getId() {
        return id;
    }

    /**
     * @return is the method doesn't change contract's state
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the method parameter types
     */
    public List<Type> getInputTypes() {
        return inputTypes;
    }

    /**
     * @return the method return types
     */
    public List<Type> getOutputTypes() {
        return outputTypes;
    }

    /**
     * Encodes call data, so you can call the contract through some other means (for example, through RPC).
     *
     * <p><b>Example:</b> <code>baz(uint32,bool)</code> with arguments <tt>(69, true)</tt> becomes
     * <tt>0xcdcd77c000000000000000000000000000000000000000000000000000000000000000450000000000000000000000000000000000000000000000000000000000000001</tt>
     *
     * @param params parameters of the call
     * @return encoded call
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#function-selector-and-argument-encoding">Function Selector and Argument Encoding</a>
     * @see <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#examples">Examples</a>
     */
    public HexData encodeCall(Object... params) {
        if (inputTypes.size() != params.length)
            throw new IllegalArgumentException("Wrong number of input parameters: " + params.length);

        int headBytesSize = 0;
        int tailBytesSize = 0;

        for (Type type : inputTypes) {
            headBytesSize += type.isDynamic() ?
                Hex32.SIZE_BYTES : type.getBytesFixedSize();
        }

        List<Hex32> head = new ArrayList<>(headBytesSize / Hex32.SIZE_BYTES);
        List<Hex32> tail = new ArrayList<>();

        for (int i = 0; i < inputTypes.size(); i++) {
            Type type = inputTypes.get(i);

            //noinspection unchecked
            Hex32[] data = type.encode(params[i]);

            if (!type.isDynamic()) {
                Collections.addAll(head, data);
            } else {
                Collections.addAll(tail, data);

                head.add(Hex32.from(MethodId.SIZE_BYTES + headBytesSize + tailBytesSize));
                tailBytesSize += data.length * Hex32.SIZE_BYTES;
            }
        }

        HexData[] data = new HexData[head.size() + tail.size() + 1];

        data[0] = id;
        System.arraycopy(head.toArray(new Hex32[head.size()]), 0, data, 1, head.size());
        System.arraycopy(tail.toArray(new Hex32[tail.size()]), 0, data, head.size() + 1, tail.size());

        return HexData.from(data);
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
        return String.format("%s!%h@%h{id=%s,constant=%b,accepts=%s,returns=%s}",
                getClass().getSimpleName(), System.identityHashCode(this), hashCode(),
                id, isConstant, inputTypes, outputTypes);
    }
}
