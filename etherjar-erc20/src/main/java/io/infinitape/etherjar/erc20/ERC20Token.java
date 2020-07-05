package io.infinitape.etherjar.erc20;

import io.infinitape.etherjar.contract.ContractCall;
import io.infinitape.etherjar.contract.ContractReadCall;
import io.infinitape.etherjar.domain.Address;
import io.infinitape.etherjar.hex.HexData;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;

public class ERC20Token {

    private static final Function<HexData, BigInteger> processor = (result) ->
        result.asQuantity().getValue();

    private final Address contract;

    public ERC20Token(Address contract) {
        this.contract = contract;
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
