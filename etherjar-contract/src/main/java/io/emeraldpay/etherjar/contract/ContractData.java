package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.MethodId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.math.BigInteger;
import java.util.*;

/**
 * Operates data field of a transaction with regard to Smart Contract calls. Allows to prepare data for a call, or parse call details
 * from an existing transaction.
 */
public class ContractData {

    private final MethodId method;
    private final Hex32[] arguments;

    /**
     * Prepare a call without any arguments
     *
     * @param method method id, non-null
     */
    public ContractData(MethodId method) {
        this(method, new Hex32[0]);
    }

    /**
     * Prepare a call with arguments
     *
     * @param method    method id, non null
     * @param arguments arguments, may be null or empty for no arguments
     */
    public ContractData(MethodId method, List<Hex32> arguments) {
        this(method, arguments == null ? null : arguments.toArray(new Hex32[0]));
    }

    /**
     * Prepare a call with arguments
     *
     * @param method    method id, non null
     * @param arguments arguments, may be null or empty for no arguments
     */
    public ContractData(MethodId method, Hex32[] arguments) {
        if (method == null) {
            throw new NullPointerException("MethodId must be not null");
        }
        this.method = method;
        if (arguments == null) {
            this.arguments = new Hex32[0];
        } else {
            for (Hex32 argument : arguments) {
                if (argument == null) {
                    throw new NullPointerException("Argument values cannot be null");
                }
            }
            this.arguments = arguments;
        }
    }

    /**
     * Start to prepare a call by using the builder
     *
     * @return new builder to prepare a call
     */
    public static ContractData.Builder newBuilder() {
        return new ContractData.Builder();
    }

    /**
     * Extract call details from existing data
     *
     * @param input existing data
     * @return call details, or null if input is null or empty
     * @throws IllegalArgumentException if input is invalid call
     */
    public static ContractData extract(HexData input) {
        if (input == null || input.getSize() == 0) {
            return null;
        }
        if (input.getSize() < MethodId.SIZE_BYTES || (input.getSize() - MethodId.SIZE_BYTES) % Hex32.SIZE_BYTES != 0) {
            throw new IllegalArgumentException("Invalid size: " + input.getSize());
        }
        MethodId method = MethodId.fromInput(input);
        HexData[] rawArguments = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
        Hex32[] arguments = new Hex32[rawArguments.length];
        for (int i = 0; i < rawArguments.length; i++) {
            HexData arg = rawArguments[i];
            arguments[i] = Hex32.from(arg);
        }
        return new ContractData(method, arguments);
    }

    /**
     * @return call method id
     */
    public MethodId getMethod() {
        return method;
    }

    /**
     * @return call arguments
     */
    public Hex32[] getArguments() {
        return arguments;
    }

    /**
     * Encode call to a data suitable to pass into a transaction
     *
     * @return encoded call
     */
    public HexData toData() {
        byte[] result = new byte[MethodId.SIZE_BYTES + arguments.length * Hex32.SIZE_BYTES];
        System.arraycopy(method.getBytes(), 0, result, 0, MethodId.SIZE_BYTES);
        for (int i = 0; i < arguments.length; i++) {
            System.arraycopy(arguments[i].getBytes(), 0, result, MethodId.SIZE_BYTES + i * Hex32.SIZE_BYTES, Hex32.SIZE_BYTES);
        }
        return new HexData(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractData)) return false;
        ContractData that = (ContractData) o;
        return method.equals(that.method) &&
            Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    private static class Argument {
        private final boolean struct;
        private final List<Hex32> value;

        public Argument(List<Hex32> value) {
            this.struct = true;
            this.value = value;
        }

        public Argument(Hex32 value) {
            this.struct = false;
            this.value = Collections.singletonList(value);
        }

        public boolean isStruct() {
            return struct;
        }

        public List<Hex32> getValue() {
            return value;
        }
    }

    /**
     * Call builder
     */
    public static class Builder {
        private MethodId method;
        private final List<Argument> arguments = new ArrayList<>();

        /**
         * @param method method id
         * @return builder
         */
        public Builder method(MethodId method) {
            this.method = method;
            return this;
        }

        /**
         *
         * @param hex method id as hex, must be with 0x prefix
         * @return builder
         */
        public Builder method(String hex) {
            return method(MethodId.from(hex));
        }

        /**
         *
         * @param value method id as bytes, must be 4 bytes long
         * @return builder
         */
        public Builder method(byte[] value) {
            return method(MethodId.from(value));
        }

        /**
         * Make method from a specification. For example `transfer(address,uint256)` will be `builder.method("transfer", "address", "uint256")`)
         *
         * @param name method full name
         * @param arguments method argument types
         * @return builder
         */
        public Builder method(String name, String... arguments) {
            return method(MethodId.fromSignature(name, arguments));
        }

        /**
         * Add argument to the call
         *
         * @param value argument value
         * @return builder
         */
        public Builder argument(Hex32 value) {
            arguments.add(new Argument(value));
            return this;
        }

        public Builder argument(Address value) {
            return argument(Hex32.extendFrom(value));
        }

        public Builder argument(HexQuantity value) {
            return argument(Hex32.extendFrom(value));
        }

        public Builder argument(BigInteger value) {
            return argument(HexQuantity.from(value));
        }

        public Builder argument(Long value) {
            return argument(Hex32.extendFrom(value));
        }

        /**
         * Add argument to the call
         *
         * @param hex argument value as hex, must be with 0x prefix
         * @return builder
         */
        public Builder argument(String hex) {
            return argument(Hex32.from(hex));
        }

        /**
         * Add argument which type is array of elements
         *
         * @param array value
         * @return builder
         */
        public Builder argumentArray(Hex32[] array) {
            arguments.add(new Argument(Arrays.asList(array)));
            return this;
        }

        public Builder argumentArray(List<Hex32> array) {
            arguments.add(new Argument(array));
            return this;
        }

        /**
         * Add argument to the call
         *
         * @param value argument value, must be 32 bytes value
         * @return builder
         */
        public Builder argument(byte[] value) {
            return argument(Hex32.from(value));
        }

        /**
         * Build the call
         *
         * @return call data
         */
        public ContractData build() {
            if (method == null) {
                throw new NullPointerException("MethodId is not set");
            }
            List<Hex32> base = new ArrayList<>(arguments.size());
            List<Hex32> structs = new ArrayList<>();
            long position = arguments.size();
            for (Argument arg: arguments) {
                if (arg.struct) {
                    base.add(Hex32.extendFrom(position * 32));
                    structs.add(Hex32.extendFrom(Integer.toUnsignedLong(arg.value.size())));
                    structs.addAll(arg.value);
                    position += 1 + arg.value.size();
                } else {
                    base.add(arg.value.get(0));
                }
            }
            if (!structs.isEmpty()) {
                base.addAll(structs);
            }
            return new ContractData(method, base);
        }
    }
}
