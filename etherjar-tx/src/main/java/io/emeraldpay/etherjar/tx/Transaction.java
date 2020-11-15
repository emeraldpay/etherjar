/*
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
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.HexData;
import io.emeraldpay.etherjar.rlp.RlpReader;
import io.emeraldpay.etherjar.rlp.RlpType;
import io.emeraldpay.etherjar.rlp.RlpWriter;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.math.BigInteger;

/**
 * A transaction information
 */
public class Transaction {

    private long nonce;
    private BigInteger gasPrice;
    private long gas;
    private Address to;
    private Wei value;
    private HexData data;
    private Signature signature;

    /**
     * Import transaction from an RLP encoded data
     *
     * @param encoded RLP encoded transaction
     * @return transaction
     * @throws IllegalArgumentException if RLP is invalid or corrupted
     */
    public static Transaction fromRlp(byte[] encoded) {
        RlpReader toprdr = new RlpReader(encoded);
        if (toprdr.getType() != RlpType.LIST) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Not a list");
        }
        if (!toprdr.isConsumed()) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Has additional data after tx definition");
        }

        RlpReader rdr = toprdr.nextList();
        Transaction tx = new Transaction();

        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setNonce(rdr.nextLong());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Nonce");
        }

        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setGasPrice(rdr.nextBigInt());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas Price");
        }

        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setGas(rdr.nextLong());
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Gas");
        }


        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setTo(Address.from(rdr.next()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: To");
        }

        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setValue(new Wei(rdr.nextBigInt()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Value");
        }

        if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
            tx.setData(new HexData(rdr.next()));
        } else {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: Data");
        }

        if (rdr.hasNext()) {
            Signature signature;
            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                int v = rdr.nextInt();
                if (v == 27 || v == 28) {
                    signature = new Signature();
                } else {
                    int chainId = SignatureEip155.extractChainId(v);
                    signature = new SignatureEip155(chainId);
                }
                signature.setV(v);
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: V");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setR(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: R");
            }

            if (rdr.hasNext() && rdr.getType() == RlpType.BYTES) {
                signature.setS(rdr.nextBigInt());
            } else {
                throw new IllegalArgumentException("Transaction has invalid RLP encoding. Cannot extract: S");
            }

            tx.setSignature(signature);
        }

        if (!rdr.isConsumed()) {
            throw new IllegalArgumentException("Transaction has invalid RLP encoding. Has more data than expected");
        }

        return tx;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public Address getTo() {
        return to;
    }

    public void setTo(Address to) {
        this.to = to;
    }

    public Wei getValue() {
        return value;
    }

    public void setValue(Wei value) {
        this.value = value;
    }

    public HexData getData() {
        return data;
    }

    public void setData(HexData data) {
        this.data = data;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public boolean isSigned() {
        return signature != null;
    }

    /**
     *
     * @return signer of the transaction, if it's signed. May return null if can't recover correct public key from
     * signature
     * @throws IllegalStateException if transaction is not signed
     * @see Signature#recoverAddress()
     */
    public Address getFrom() {
        if (!isSigned()) {
            throw new IllegalStateException("Transaction is not signed");
        }
        signature.setMessage(hash(false));
        return signature.recoverAddress();
    }

    /**
     * Hash of transaction. Usually used before signing it, at this case call it with signed=false and use resulting
     * hash as a message for the Signature. Signed hash is used as an identifier of the transaction.
     *
     * @param signed if true then include signature
     * @return hash of the transaction
     * @see io.emeraldpay.etherjar.domain.TransactionId
     */
    public byte[] hash(boolean signed) {
        RlpWriter wrt = new RlpWriter();
        wrt.startList()
                .write(getNonce())
                .write(getGasPrice())
                .write(getGas())
                .write(getTo().getBytes())
                .write(getValue().getAmount())
                .write(getData().getBytes());
        if (signed) {
            wrt.write(signature.getV())
                    .write(signature.getR())
                    .write(signature.getS());
        } else if (signature instanceof SignatureEip155) {
            wrt.write(((SignatureEip155) signature).getChainId())
                    .write(0)
                    .write(0);
        }
        wrt.closeList();
        byte[] rlp = wrt.toByteArray();

        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(rlp);
        return keccak.digest();
    }

}
