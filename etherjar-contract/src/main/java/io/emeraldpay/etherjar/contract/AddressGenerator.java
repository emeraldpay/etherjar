package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rlp.RlpWriter;
import org.bouncycastle.jcajce.provider.digest.Keccak;

public class AddressGenerator {

    /**
     * Base contract address generation available in Ethereum from the beginning
     * as <code>CREATE (0xf0)</code> opcode
     *
     * @param from contract creator. A tx sender if sent as tx, or originating contract if create from a contract
     * @param nonce transaction nonce
     * @return address of a contract created after the execution of the tx
     */
    public Address create(Address from, Long nonce) {
        byte[] rlp = new RlpWriter()
            .startList()
            .write(from.getBytes())
            .write(nonce)
            .closeList()
            .toByteArray();

        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        byte[] digest = keccak.digest();

        byte[] address = new byte[Address.SIZE_BYTES];
        System.arraycopy(digest, digest.length - address.length, address, 0, address.length);

        return Address.from(address);
    }

    /**
     * Contract address generation introduced with EIP-1014.
     * Available as <code>CREATE2 (0xf0)</code> opcode.
     *
     * @param from contract creator. A tx sender if sent as tx, or originating contract if create from a contract
     * @param salt user provided randomization value
     * @param initCode contract init code
     * @return address of a contract created after the execution of the tx
     * @see <a href="https://eips.ethereum.org/EIPS/eip-1014">https://eips.ethereum.org/EIPS/eip-1014</a>
     */
    public Address create2(Address from, Hex32 salt, HexData initCode) {
        //
        // see https://eips.ethereum.org/EIPS/eip-1014
        // keccak256( 0xff ++ address ++ salt ++ keccak256(init_code))[12:]
        //

        Keccak.Digest256 keccak = new Keccak.Digest256();

        keccak.update(initCode.getBytes());
        byte[] initCodeHash = keccak.digest();

        keccak.reset();
        keccak.update((byte)0xff);
        keccak.update(from.getBytes());
        keccak.update(salt.getBytes());
        keccak.update(initCodeHash);
        byte[] digest = keccak.digest();

        byte[] address = new byte[Address.SIZE_BYTES];
        System.arraycopy(digest, digest.length - address.length, address, 0, address.length);

        return Address.from(address);
    }
}
