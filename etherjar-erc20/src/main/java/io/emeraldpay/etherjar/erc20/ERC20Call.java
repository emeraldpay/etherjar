package io.emeraldpay.etherjar.erc20;

import io.emeraldpay.etherjar.contract.ContractData;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.MethodId;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.hex.HexQuantity;

import java.math.BigInteger;
import java.util.Objects;

public class ERC20Call {

    public static Base decode(HexData input) {
        MethodId methodId = MethodId.fromInput(input);
        Base parsed;
        if (methodId.equals(ERC20Method.ALLOWANCE.getMethodId())) {
            parsed = new Allowance();
        } else if (methodId.equals(ERC20Method.APPROVE.getMethodId())) {
            parsed = new Approve();
        } else if (methodId.equals(ERC20Method.BALANCE_OF.getMethodId())) {
            parsed = new BalanceOf();
        } else if (methodId.equals(ERC20Method.TOTAL_SUPPLY.getMethodId())) {
            parsed = new TotalSupply();
        } else if (methodId.equals(ERC20Method.TRANSFER.getMethodId())) {
            parsed = new Transfer();
        } else if (methodId.equals(ERC20Method.TRANSFER_FROM.getMethodId())) {
            parsed = new TransferFrom();
        } else {
            throw new IllegalStateException("Unsupported method: " + methodId);
        }
        parsed.decode(input);
        return parsed;
    }

    public static abstract class Base {
        private final ERC20Method method;

        public Base(ERC20Method method) {
            this.method = method;
        }

        public abstract ContractData encode();

        public abstract void decode(HexData input);

        public ERC20Method getMethod() {
            return method;
        }

        public ContractData.Builder encodeBuilder() {
            return ContractData.newBuilder()
                .method(getMethod().getMethodId());
        }

        public void verifyMethod(HexData input) {
            if (input.getSize() < MethodId.SIZE_BYTES) {
                throw new IllegalArgumentException("Empty or short methodId");
            }
            MethodId methodId = MethodId.fromInput(input);
            if (!methodId.equals(getMethod().getMethodId())) {
                throw new IllegalArgumentException("Invalid method id: " + methodId + " != " + getMethod().getMethodId());
            }
        }
    }

    public static class Transfer extends Base {
        private Address to;
        private BigInteger value;

        public Transfer() {
            super(ERC20Method.TRANSFER);
        }

        public Transfer(ERC20Method method, Address to, BigInteger value) {
            super(method);
            this.to = to;
            this.value = value;
        }

        public Address getTo() {
            return to;
        }

        public void setTo(Address to) {
            this.to = to;
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        @Override
        public ContractData encode() {
            return encodeBuilder()
                .argument(to)
                .argument(HexQuantity.from(value))
                .build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
            HexData[] args = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
            if (args.length != 2) {
                throw new IllegalArgumentException("Expected 2 arguments, received " + args.length);
            }
            to = Address.extract(Hex32.from(args[0]));
            value = new BigInteger(1, args[1].getBytes());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transfer)) return false;
            Transfer transfer = (Transfer) o;
            return Objects.equals(to, transfer.to) &&
                Objects.equals(value, transfer.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, value);
        }
    }

    public static class TransferFrom extends Base {
        private Address from;
        private Address to;
        private BigInteger value;

        public TransferFrom() {
            super(ERC20Method.TRANSFER_FROM);
        }

        public Address getFrom() {
            return from;
        }

        public void setFrom(Address from) {
            this.from = from;
        }

        public Address getTo() {
            return to;
        }

        public void setTo(Address to) {
            this.to = to;
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        @Override
        public ContractData encode() {
            return encodeBuilder()
                .argument(from)
                .argument(to)
                .argument(HexQuantity.from(value))
                .build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
            HexData[] args = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
            if (args.length != 3) {
                throw new IllegalArgumentException("Expected 3 arguments, received " + args.length);
            }
            from = Address.extract(Hex32.from(args[0]));
            to = Address.extract(Hex32.from(args[1]));
            value = new BigInteger(1, args[2].getBytes());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransferFrom)) return false;
            TransferFrom that = (TransferFrom) o;
            return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, value);
        }
    }

    public static class Approve extends Base {
        private Address spender;
        private BigInteger value;

        public Approve() {
            super(ERC20Method.APPROVE);
        }

        public Address getSpender() {
            return spender;
        }

        public void setSpender(Address spender) {
            this.spender = spender;
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        @Override
        public ContractData encode() {
            return encodeBuilder()
                .argument(spender)
                .argument(HexQuantity.from(value))
                .build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
            HexData[] args = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
            if (args.length != 2) {
                throw new IllegalArgumentException("Expected 2 arguments, received " + args.length);
            }
            spender = Address.extract(Hex32.from(args[0]));
            value = new BigInteger(1, args[1].getBytes());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Approve)) return false;
            Approve approve = (Approve) o;
            return Objects.equals(spender, approve.spender) &&
                Objects.equals(value, approve.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spender, value);
        }
    }

    public static class BalanceOf extends Base {

        private Address address;

        public BalanceOf() {
            super(ERC20Method.BALANCE_OF);
        }

        public BalanceOf(ERC20Method method, Address address) {
            super(method);
            this.address = address;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public ContractData encode() {
            return encodeBuilder()
                .argument(address)
                .build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
            HexData[] args = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
            if (args.length != 1) {
                throw new IllegalArgumentException("Expected 1 argument, received " + args.length);
            }
            address = Address.extract(Hex32.from(args[0]));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BalanceOf)) return false;
            BalanceOf balanceOf = (BalanceOf) o;
            return Objects.equals(address, balanceOf.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address);
        }
    }

    public static class TotalSupply extends Base {

        public TotalSupply() {
            super(ERC20Method.TOTAL_SUPPLY);
        }

        @Override
        public ContractData encode() {
            return encodeBuilder().build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
        }

    }

    public static class Allowance extends Base {
        private Address owner;
        private Address spender;

        public Allowance() {
            super(ERC20Method.ALLOWANCE);
        }

        public Address getOwner() {
            return owner;
        }

        public void setOwner(Address owner) {
            this.owner = owner;
        }

        public Address getSpender() {
            return spender;
        }

        public void setSpender(Address spender) {
            this.spender = spender;
        }

        @Override
        public ContractData encode() {
            return encodeBuilder()
                .argument(owner)
                .argument(spender)
                .build();
        }

        @Override
        public void decode(HexData input) {
            verifyMethod(input);
            HexData[] args = input.split(Hex32.SIZE_BYTES, MethodId.SIZE_BYTES);
            if (args.length != 2) {
                throw new IllegalArgumentException("Expected 2 argument, received " + args.length);
            }
            owner = Address.extract(Hex32.from(args[0]));
            spender = Address.extract(Hex32.from(args[1]));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Allowance)) return false;
            Allowance allowance = (Allowance) o;
            return Objects.equals(owner, allowance.owner) &&
                Objects.equals(spender, allowance.spender);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, spender);
        }
    }


}
