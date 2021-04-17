package io.emeraldpay.etherjar.erc20;

import io.emeraldpay.etherjar.contract.ContractCall;
import io.emeraldpay.etherjar.contract.ContractReadCall;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.hex.HexData;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

/**
 * Main interface to make ERC-20 calls.
 * <code>read*</code> methods are for reading from blockchain using <code>eth_call</code> and <code>execute*</code> for creating a new transaction.
 * <br>
 * See <a href="https://github.com/ethereum/eips/issues/20">ERC-20 Specification</a>
 */
public class ERC20Token {

    private static final Function<HexData, BigInteger> processor = (result) ->
        result.asQuantity().getValue();

    private final Address contract;

    public ERC20Token(Address contract) {
        this.contract = contract;
    }

    public Address getContract() {
        return contract;
    }

    public ContractReadCall<String> readSymbol() {
        ERC20Call.Symbol call = new ERC20Call.Symbol();
        ERC20Result.Symbol result = new ERC20Result.Symbol();
        return new ContractReadCall<>(contract, call.encode(), result);
    }

    public ContractReadCall<String> readName() {
        ERC20Call.Name call = new ERC20Call.Name();
        ERC20Result.Name result = new ERC20Result.Name();
        return new ContractReadCall<>(contract, call.encode(), result);
    }

    public ContractReadCall<Integer> readDecimals() {
        ERC20Call.Decimals call = new ERC20Call.Decimals();
        ERC20Result.Decimals result = new ERC20Result.Decimals();
        return new ContractReadCall<>(contract, call.encode(), result);
    }

    public ContractReadCall<BigInteger> readTotalSupply() {
        ERC20Call.TotalSupply call = new ERC20Call.TotalSupply();
        return new ContractReadCall<>(contract, call.encode(), processor);
    }

    public ContractReadCall<BigInteger> readBalanceOf(Address address) {
        ERC20Call.BalanceOf call = new ERC20Call.BalanceOf();
        call.setAddress(address);
        return new ContractReadCall<>(contract, call.encode(), processor);
    }

    public ContractReadCall<BigInteger> readAllowance(Address owner, Address spender) {
        ERC20Call.Allowance call = new ERC20Call.Allowance();
        call.setOwner(owner);
        call.setSpender(spender);
        return new ContractReadCall<>(contract, call.encode(), processor);
    }

    public ContractCall executeTransfer(Address to, BigInteger value) {
        ERC20Call.Transfer call = new ERC20Call.Transfer();
        call.setTo(to);
        call.setValue(value);
        return new ContractCall(contract, call.encode());
    }

    public ContractCall executeTransferFrom(Address from, Address to, BigInteger value) {
        ERC20Call.TransferFrom call = new ERC20Call.TransferFrom();
        call.setFrom(from);
        call.setTo(to);
        call.setValue(value);
        return new ContractCall(contract, call.encode());
    }

    public ContractCall executeApprove(Address spender, BigInteger value) {
        ERC20Call.Approve call = new ERC20Call.Approve();
        call.setSpender(spender);
        call.setValue(value);
        return new ContractCall(contract, call.encode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ERC20Token)) return false;
        ERC20Token that = (ERC20Token) o;
        return Objects.equals(contract, that.contract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contract);
    }
}
