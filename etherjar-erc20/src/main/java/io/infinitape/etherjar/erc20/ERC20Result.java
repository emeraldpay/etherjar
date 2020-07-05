package io.infinitape.etherjar.erc20;

import io.infinitape.etherjar.hex.Hex32;
import io.infinitape.etherjar.hex.HexData;
import io.infinitape.etherjar.hex.HexQuantity;

import java.math.BigInteger;
import java.util.Objects;

public class ERC20Result {

    static abstract class Base {
        public abstract HexData encode();

        public abstract void decode(HexData input);
    }

    static abstract class ValueResult extends Base {
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
            ValueResult that = (ValueResult) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class BalanceOf extends ValueResult {
    }

    public static class TotalSupply extends ValueResult {
    }

    public static class Allowance extends ValueResult {
    }
}
