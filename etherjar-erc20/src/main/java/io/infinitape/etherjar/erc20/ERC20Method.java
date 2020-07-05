package io.infinitape.etherjar.erc20;

import io.infinitape.etherjar.abi.AddressType;
import io.infinitape.etherjar.abi.Type;
import io.infinitape.etherjar.abi.UIntType;
import io.infinitape.etherjar.domain.MethodId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <ul>
 *     <li><a href="https://github.com/ethereum/eips/issues/20">Specification</a></li>
 *     <li><a href="https://ethereum.stackexchange.com/questions/38381/how-can-i-identify-that-transaction-is-erc20-token-creation-contract">List of methods</a></li>
 * </ul>
 */
public enum ERC20Method {

    /**
     * totalSupply() public view returns (uint256 totalSupply)
     * Code: 0x18160ddd
     * <p>
     * Get the total token supply
     *
     * @see ERC20Call.TotalSupply
     * @see ERC20Result.TotalSupply
     */
    TOTAL_SUPPLY("totalSupply", Collections.emptyList()),

    /**
     * balanceOf(address _owner) public view returns (uint256 balance)
     * Code: 0x70a08231
     * <p>
     * Get the account balance of another account with address _owner
     *
     * @see ERC20Call.BalanceOf
     * @see ERC20Result.BalanceOf
     */
    BALANCE_OF("balanceOf", Collections.singletonList(AddressType.DEFAULT)),

    /**
     * transfer(address _to, uint256 _value) public returns (bool success)
     * Code: 0xa9059cbb
     * <p>
     * Send _value amount of tokens to address _to
     *
     * @see ERC20Call.Transfer
     */
    TRANSFER("transfer", Arrays.asList(AddressType.DEFAULT, UIntType.DEFAULT)),

    /**
     * transferFrom(address _from, address _to, uint256 _value) public returns (bool success)
     * Code: 0x23b872dd
     * <p>
     * Send _value amount of tokens from address _from to address _to
     *
     * @see ERC20Call.TransferFrom
     */
    TRANSFER_FROM("transferFrom", Arrays.asList(AddressType.DEFAULT, AddressType.DEFAULT, UIntType.DEFAULT)),

    /**
     * approve(address _spender, uint256 _value) public returns (bool success)
     * Code: 0x095ea7b3
     * <p>
     * Allow _spender to withdraw from your account, multiple times, up to the _value amount. If this function is called again it overwrites the current allowance with _value
     *
     * @see ERC20Call.Approve
     */
    APPROVE("approve", Arrays.asList(AddressType.DEFAULT, UIntType.DEFAULT)),

    /**
     * allowance(address _owner, address _spender) public view returns (uint256 remaining)
     * Code: 0xdd62ed3e
     * <p>
     * Returns the amount which _spender is still allowed to withdraw from _owner
     *
     * @see ERC20Call.Allowance
     * @see ERC20Result.Allowance
     */
    ALLOWANCE("allowance", Arrays.asList(AddressType.DEFAULT, AddressType.DEFAULT));

    private final MethodId methodId;
    private final String methodName;
    private final List<Type<?>> arguments;

    ERC20Method(String methodName, List<Type<?>> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
        this.methodId = MethodId.fromSignature(
            methodName,
            arguments.stream().map(Type::getCanonicalName).collect(Collectors.toList())
        );
    }

    public MethodId getMethodId() {
        return methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Type<?>> getArguments() {
        return arguments;
    }
}
