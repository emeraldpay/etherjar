package io.emeraldpay.etherjar.erc20;

import io.emeraldpay.etherjar.abi.DynamicBytesType;
import io.emeraldpay.etherjar.abi.OffsetType;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ERC-20 contract result encoders/decoders
 */
public class ERC20Result {

    /**
     * Base class to encode/decode results of ERC-20 call result.
     * Result here means the value of JSON field <code>result</code> after execution of <code>eth_call</code> RPC method.
     */
    static abstract class Base<T, R extends Base<T, ?>> implements Function<HexData, T>, Supplier<T> {
        /**
         * @return hex encoded result
         */
        public abstract HexData encode();

        /**
         * Decode data from the existing result
         *
         * @param input value of the result field
         */
        public abstract void decode(HexData input);

        @Override
        @SuppressWarnings("unchecked")
        public T apply(HexData hexData) {
            this.decode(hexData);
            return this.get();
        }
    }

    static abstract class ValueResult<T extends Base<BigInteger, ?>> extends Base<BigInteger, T> {
        private BigInteger value;

        @Override
        public HexData encode() {
            return Hex32.extendFrom(HexQuantity.from(value));
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        @Override
        public BigInteger get() {
            return getValue();
        }

        @Override
        public void decode(HexData input) {
            this.value = Hex32.from(input).asQuantity().getValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ValueResult)) return false;
            ValueResult<?> that = (ValueResult<?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * Result for call of <code>balanceOf</code> method of a contract.
     * The value is the balance of originally specified address
     */
    public static class BalanceOf extends ValueResult<BalanceOf> {
    }

    /**
     * Result for call of <code>totalSupply</code> method of a contract
     * The value is the total supply of the token
     */
    public static class TotalSupply extends ValueResult<TotalSupply> {
    }

    /**
     * Result for call of <code>allowance</code> method of a contract
     * The value is the allowance for the specified spender address allowed by owner
     */
    public static class Allowance extends ValueResult<Allowance> {
    }

    /**
     * String parsing with handling of non-standard encoded values.
     * <br/>
     * Encoded by abi spec (ex. _symbol_):
     * <code><pre>
     *  0000000000000000000000000000000000000000000000000000000000000020
     *  0000000000000000000000000000000000000000000000000000000000000004
     *  5553445400000000000000000000000000000000000000000000000000000000
     * </pre></code>
     * Or:<br/>
     * <code><pre>
     *  0000000000000000000000000000000000000000000000000000000000000020
     *  0000000000000000000000000000000000000000000000000000000000000022
     *  434f5645525f594541524e5f323032315f30325f32385f4441495f305f434c41
     *  494d000000000000000000000000000000000000000000000000000000000000
     * </pre></code>
     *
     * But some tokens respond with a null-terminated string, i.e. just characters.<br/>
     * Such as: 0x339D73f9a0FBD064Ea81F274437760a9db934806, 0x9f8F72aA9304c8B593d555F12eF6589cC3A579A2, 0xeb269732ab75A6fD61Ea60b06fE994cD32a83549
     * <br/>
     * Ex _name_:
     *  <code>4772617a696e6720537573686900000000000000000000000000000000000000</code>
     *
     */
    static abstract class StringResult<T extends Base<String, ?>> extends Base<String, T> {

        private static final OffsetType OFFSET_TYPE = new OffsetType();
        private static final DynamicBytesType DYNAMIC_BYTES_TYPE = new DynamicBytesType();

        private String value;

        @Override
        public HexData encode() {
            return OFFSET_TYPE.encode(
                DYNAMIC_BYTES_TYPE.encode(value.getBytes())
            );
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public void decode(HexData input) {
            if (input.getSize() == 0) {
                throw new IllegalArgumentException("Empty value");
            }
            if (input.getSize() > Hex32.SIZE_BYTES * 2) {
                // seems to be a standard encoded token
                HexData chars = OFFSET_TYPE.decode(input);
                byte[] str = DYNAMIC_BYTES_TYPE.decode(chars);
                setValue(new String(str));
            } else {
                // just zero-terminated string
                byte[] data = input.getBytes();
                int len = 0;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == 0x0) {
                        len = i;
                        break;
                    }
                }
                if (len > 0) {
                    setValue(new String(data, 0, len));
                } else {
                    throw new IllegalArgumentException("Zero length value");
                }
            }
        }
    }

    /**
     * Result for call of <code>symbol</code> method of a contract
     */
    public static class Symbol extends StringResult<Symbol> {
    }

    /**
     * Result for call of <code>name</code> method of a contract
     */
    public static class Name extends StringResult<Name> {
    }

    /**
     * Result for call of <code>decimals</code> method of a contract
     */
    public static class Decimals extends Base<Integer, Decimals> {

        public static final int MAX_VALUE = 255;

        private int value = 0;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            if (value < 0 || value > MAX_VALUE) {
                throw new IllegalArgumentException("Invalid value: " + value);
            }
            this.value = value;
        }

        @Override
        public HexData encode() {
            return HexQuantity.from((long)value).asData();
        }

        @Override
        public void decode(HexData input) {
            if (input.getSize() != Hex32.SIZE_BYTES) {
                throw new IllegalArgumentException("Invalid value " + input.toHex());
            }
            int value = input.asQuantity().getValue().intValueExact();
            setValue(value);
        }

        @Override
        public Integer get() {
            return value;
        }
    }

}
