package io.infinitape.etherjar.contract;

import io.infinitape.etherjar.domain.MethodId;
import io.infinitape.etherjar.hex.Hex32;
import io.infinitape.etherjar.hex.HexData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Operates data field of a transaction with regard to Smart Contract calls. Allows to prepare data for a call, or parse call details
 * from an existing transaction.
 */
public class ContractTransactionData {

    private final MethodId method;
    private final Hex32[] arguments;

    /**
     * Prepare a call without any arguments
     *
     * @param method method id, non-null
     */
    public ContractTransactionData(MethodId method) {
        this(method, new Hex32[0]);
    }

    /**
     * Prepare a call with arguments
     *
     * @param method method id, non null
     * @param arguments arguments, may be null or empty for no arguments
     */
    public ContractTransactionData(MethodId method, List<Hex32> arguments) {
        this(method, arguments == null ? null : arguments.toArray(new Hex32[0]));
    }

    /**
     * Prepare a call with arguments
     *
     * @param method method id, non null
     * @param arguments arguments, may be null or empty for no arguments
     */
    public ContractTransactionData(MethodId method, Hex32[] arguments) {
        if (method == null) {
            throw new IllegalArgumentException("MethodId must be not null");
        }
        this.method = method;
        if (arguments == null) {
            this.arguments = new Hex32[0];
        } else {
            for (Hex32 argument : arguments) {
                if (argument == null) {
                    throw new IllegalArgumentException("Argument values cannot be null");
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
    public static ContractTransactionData.Builder newBuilder() {
        return new ContractTransactionData.Builder();
    }

    /**
     * Extract call details from existing data
     *
     * @param input existing data
     * @return call details, or null if input is null or empty
     * @throws IllegalArgumentException if input is invalid call
     */
    public static ContractTransactionData extract(HexData input) {
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
        return new ContractTransactionData(method, arguments);
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
        if (!(o instanceof ContractTransactionData)) return false;
        ContractTransactionData that = (ContractTransactionData) o;
        return method.equals(that.method) &&
            Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    /**
     * Call builder
     */
    public static class Builder {
        private MethodId method;
        private List<Hex32> arguments = new ArrayList<>();

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
            arguments.add(value);
            return this;
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
        public ContractTransactionData build() {
            if (method == null) {
                throw new IllegalStateException("MethodId is not set");
            }
            return new ContractTransactionData(method, arguments);
        }
    }
}
