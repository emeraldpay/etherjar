package io.emeraldpay.etherjar.tx;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.rlp.RlpWriter;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A EIP-7702 transaction with authorization list.
 *
 * @see <a href="https://eips.ethereum.org/EIPS/eip-7702">EIP-7702</a>
 */
public class TransactionWithSetCode extends TransactionWithGasPriority {

    private List<Authorization> authorizationList;

    public TransactionWithSetCode() {
        super();
    }

    public TransactionWithSetCode(TransactionWithSetCode other) {
        super(other);
        this.authorizationList = new ArrayList<>(other.authorizationList.size());
        for (Authorization authorization : other.authorizationList) {
            this.authorizationList.add(new Authorization(authorization));
        }
    }

    public List<Authorization> getAuthorizationList() {
        return authorizationList;
    }

    public void setAuthorizationList(List<Authorization> authorizationList) {
        this.authorizationList = authorizationList;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.SET_CODE;
    }

    @Override
    public byte[] hash() {
        byte[] rlp = TransactionEncoder.DEFAULT.encode(this, false);
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionWithSetCode that = (TransactionWithSetCode) o;
        return Objects.equals(authorizationList, that.authorizationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authorizationList);
    }

    public static class Authorization {

        /**
         * Defined as "MAGIC" in the EIP-7702 specification. Used for preparing the message hash of this authorization.
         */
        private static final byte HASH_MAGIC = 0x05;

        private int chainId;

        /**
         * The address of the account that is authorized to set code.
         */
        private Address address;

        private long nonce;

        private SignatureEIP2930 signature = new SignatureEIP2930();

        public Authorization() {
        }

        public Authorization(Authorization other) {
            this.chainId = other.chainId;
            this.address = other.address;
            this.nonce = other.nonce;
            this.signature = new SignatureEIP2930(other.signature);
        }

        public int getChainId() {
            return chainId;
        }

        public void setChainId(int chainId) {
            this.chainId = chainId;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public long getNonce() {
            return nonce;
        }

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        public int getYParity() {
            return signature.getYParity();
        }

        public void setYParity(int yParity) {
            signature.setYParity(yParity);
        }

        public BigInteger getR() {
            return signature.getR();
        }

        public void setR(BigInteger r) {
            this.signature.setR(r);
        }

        public BigInteger getS() {
            return signature.getS();
        }

        public void setS(BigInteger s) {
            this.signature.setS(s);
        }

        public SignatureEIP2930 getSignature() {
            return signature;
        }

        public void setSignature(SignatureEIP2930 signature) {
            this.signature = signature;
        }

        public boolean isSigned() {
            return signature != null;
        }

        public byte[] hash() {
            RlpWriter wrt = new RlpWriter();
            wrt.startList()
                .write(chainId)
                .write(address.getBytes())
                .write(nonce)
                .closeList();

            Keccak.Digest256 keccak = new Keccak.Digest256();
            keccak.update(HASH_MAGIC);
            keccak.update(wrt.toByteArray());
            return keccak.digest();
        }

        public Address extractFrom() {
            if (!isSigned()) {
                throw new IllegalStateException("Transaction is not signed");
            }
            if (signature.getMessage() == null) {
                signature.setMessage(hash());
            }
            return signature.recoverAddress();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Authorization that)) return false;
            return chainId == that.chainId
                && nonce == that.nonce
                && Objects.equals(address, that.address)
                && Objects.equals(signature, that.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, nonce);
        }
    }
}
