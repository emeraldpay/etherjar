package io.emeraldpay.etherjar.tx;

public class Signer {

    public Signature sign(Transaction tx, PrivateKey pk, Integer chainId) {
        byte[] hash = tx.hash(chainId);
        return Signature.create(hash, pk, chainId);
    }

}
