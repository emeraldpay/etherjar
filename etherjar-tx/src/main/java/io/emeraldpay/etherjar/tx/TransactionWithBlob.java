package io.emeraldpay.etherjar.tx;

import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction with Blobs (EIP-2718)
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-2718">EIP-2718</a>
 */
public class TransactionWithBlob extends TransactionWithGasPriority {

    private Wei maxFeePerBlobGas;

    /**
     * Represents a list of hash outputs from kzg_to_versioned_hash
     */
    private List<Hex32> blobVersionedHashes;

    public TransactionWithBlob() {
    }

    public TransactionWithBlob(TransactionWithBlob other) {
        super(other);
        this.maxFeePerBlobGas = other.maxFeePerBlobGas;
        if (other.blobVersionedHashes != null) {
            this.blobVersionedHashes = new ArrayList<>(other.blobVersionedHashes.size());
            this.blobVersionedHashes.addAll(other.blobVersionedHashes);
        }
    }

    public Wei getMaxFeePerBlobGas() {
        return maxFeePerBlobGas;
    }

    public void setMaxFeePerBlobGas(Wei maxFeePerBlobGas) {
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }

    public List<Hex32> getBlobVersionedHashes() {
        return blobVersionedHashes;
    }

    public void setBlobVersionedHashes(List<Hex32> blobVersionedHashes) {
        this.blobVersionedHashes = blobVersionedHashes;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.BLOB;
    }

    @Override
    public byte[] hash() {
        byte[] rlp = TransactionEncoder.DEFAULT.encode(this, false);
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }
}
