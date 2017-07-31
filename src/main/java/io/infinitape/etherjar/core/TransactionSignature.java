package io.infinitape.etherjar.core;

/**
 * Transaction signature with support of Replay Protection (EIP-155)
 */
public class TransactionSignature {

    private ChainId chainId;

    private HexData publicKey;
    private HexData r;
    private HexData s;
    private Integer v;

    public TransactionSignature() {
    }

    public ChainId getChainId() {
        return chainId;
    }

    public void setChainId(ChainId chainId) {
        this.chainId = chainId;
    }

    public HexData getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(HexData publicKey) {
        this.publicKey = publicKey;
    }

    public HexData getR() {
        return r;
    }

    public void setR(HexData r) {
        this.r = r;
    }

    public HexData getS() {
        return s;
    }

    public void setS(HexData s) {
        this.s = s;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        if (v == null || v < 0 || v > 255) {
            throw new IllegalArgumentException("Invalid V: " + v);
        }
        this.v = v;
    }

    public ChainId getExtractedChainId() {
        if (!isProtected()) {
            return null;
        }
        return new ChainId((v - 35) / 2);
    }

    public Integer getNormalizedV() {
        if (chainId == null) {
            return v;
        }
        return v - chainId.getValue() * 2 - 35 + 27;
    }

    public boolean isProtected() {
        if (v == 27 || v == 28) {
            return false;
        }
        return true;
    }
}
