package io.emeraldpay.etherjar.erc20;

import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

/**
 * ERC-20 contract result encoders/decoders
 */
public class ERC20Result {

    /**
     * Base class to encode/decode results of ERC-20 call result.
     * Result here means the value of JSON field <code>result</code> after execution of <code>eth_call</code> RPC method.
     */
    static abstract class Base<T extends Base<?>> implements Function<HexData, T> {
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
            return (T) this;
        }
    }

    static abstract class ValueResult<T extends Base<?>> extends Base<T> {
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
}
