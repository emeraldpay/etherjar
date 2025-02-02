/*
 * Copyright (c) 2021 EmeraldPay Inc, All Rights Reserved.
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.etherjar.tx;

import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.TransactionId;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.HexData;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A transaction information
 */
public class Transaction {

    private long nonce;
    private Wei gasPrice;
    private long gas;
    private Address to;
    private Wei value;
    private HexData data;
    private Signature signature;

    /**
     * Used to _cache_ the current transaction id. Must be erased each time the components
     * of the transaction are changed.
     */
    protected transient TransactionId transactionId;

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.transactionId = null;
        this.nonce = nonce;
    }

    public Wei getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        setGasPrice(new Wei(gasPrice));
    }

    public void setGasPrice(Wei gasPrice) {
        this.transactionId = null;
        this.gasPrice = gasPrice;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.transactionId = null;
        this.gas = gas;
    }

    public Address getTo() {
        return to;
    }

    public void setTo(Address to) {
        this.transactionId = null;
        this.to = to;
    }

    public Wei getValue() {
        return value;
    }

    public void setValue(Wei value) {
        this.transactionId = null;
        this.value = value;
    }

    public HexData getData() {
        return data;
    }

    public void setData(HexData data) {
        this.transactionId = null;
        this.data = data;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.transactionId = null;
        this.signature = signature;
    }

    public boolean isSigned() {
        return signature != null;
    }

    /**
     *
     * @return type of the transaction
     */
    public TransactionType getType() {
        return TransactionType.STANDARD;
    }

    /**
     *
     * @return signer of the transaction, if it's signed. May return null if can't recover correct public key from
     * signature
     * @throws IllegalStateException if transaction is not signed
     * @see Signature#recoverAddress()
     */
    public Address extractFrom() {
        if (!isSigned()) {
            throw new IllegalStateException("Transaction is not signed");
        }
        if (signature.getMessage() == null) {
            signature.setMessage(hash());
        }
        return signature.recoverAddress();
    }

    /**
     * Hash of transaction. Usually used before signing it, at this case call it with signed=false and use resulting
     * hash as a message for the Signature. Signed hash is used as an identifier of the transaction.
     *
     * @return hash of the transaction
     * @see io.emeraldpay.etherjar.domain.TransactionId
     */
    public byte[] hash() {
        return hash(signature != null && signature.getType() == SignatureType.EIP155 ? ((SignatureEIP155)signature).getChainId() : null);
    }

    public byte[] hash(Integer chainId) {
        byte[] rlp = TransactionEncoder.DEFAULT.encodeLegacy(this, false, chainId);

        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }

    public TransactionId transactionId() {
        if (transactionId != null) {
            return transactionId;
        }
        if (signature == null) {
            throw new IllegalStateException("Transaction is not signed");
        }
        byte[] rlp = TransactionEncoder.DEFAULT.encode(this,true);

        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        this.transactionId = TransactionId.from(keccak.digest());
        return this.transactionId;
    }

    public boolean canEqual(Transaction that) {
        if (this == that) return true;
        return nonce == that.nonce
            && gas == that.gas
            && Objects.equals(gasPrice, that.gasPrice)
            && Objects.equals(to, that.to)
            && Objects.equals(value, that.value)
            && Objects.equals(data, that.data)
            && Objects.equals(signature, that.signature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return canEqual((Transaction) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonce, to);
    }

    @Override
    public String toString() {
        return "Transaction{" +
            "nonce=" + nonce +
            ", to=" + to +
            ", gasPrice=" + gasPrice +
            ", gas=" + gas +
            ", value=" + value +
            ", data=" + data +
            '}';
    }
}
