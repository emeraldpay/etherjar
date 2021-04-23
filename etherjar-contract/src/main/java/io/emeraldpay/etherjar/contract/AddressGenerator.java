package io.emeraldpay.etherjar.contract;

import io.emeraldpay.etherjar.domain.Address;
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
}
