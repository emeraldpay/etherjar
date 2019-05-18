/*
 * Copyright (c) 2016-2017 Infinitape Inc, All Rights Reserved.
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

package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.infinitape.etherjar.domain.*;
import io.infinitape.etherjar.hex.HexData;

import java.io.Serializable;
import java.math.BigInteger;

@JsonDeserialize(using = TransactionJsonDeserializer.class)
@JsonSerialize(using = TransactionJsonSerializer.class)
public class TransactionJson implements Serializable {

    /**
     * hash of the transaction
     */
    private TransactionId hash;

    /**
     * the number of transactions made by the sender prior to this one.
     */
    private Long nonce;

    /**
     * hash of the block where this transaction was in. null when its pending.
     */
    private BlockHash blockHash;

    /**
     * block number where this transaction was in. null when its pending.
     */
    private Long blockNumber;

    /**
     * position in the block. null when its pending.
     */
    private Long transactionIndex;

    /**
     * address of the sender.
     */
    private Address from;

    /**
     * address of the receiver. null when its a contract creation transaction.
     */
    private Address to;

    /**
     * Address of a contract created from that transaction
     */
    private Address creates;

    /**
     * value transferred in Wei.
     */
    private Wei value;

    /**
     * gas price provided by the sender in Wei.
     */
    private Wei gasPrice;

    /**
     * gas provided by the sender.
     */
    private BigInteger gas;

    /**
     * the data send along with the transaction.
     */
    private HexData input;

    private TransactionSignature signature;

    public TransactionId getHash() {
        return hash;
    }

    public void setHash(TransactionId hash) {
        this.hash = hash;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public BlockHash getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(BlockHash blockHash) {
        this.blockHash = blockHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public Address getFrom() {
        return from;
    }

    public void setFrom(Address from) {
        this.from = from;
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

    public Wei getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(Wei gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public HexData getInput() {
        return input;
    }

    public void setInput(HexData input) {
        this.input = input;
    }

    public TransactionSignature getSignature() {
        return signature;
    }

    public void setSignature(TransactionSignature signature) {
        this.signature = signature;
    }

    public Address getCreates() {
        return creates;
    }

    public void setCreates(Address creates) {
        this.creates = creates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionJson)) return false;

        TransactionJson that = (TransactionJson) o;

        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        if (nonce != null ? !nonce.equals(that.nonce) : that.nonce != null) return false;
        if (blockHash != null ? !blockHash.equals(that.blockHash) : that.blockHash != null) return false;
        if (blockNumber != null ? !blockNumber.equals(that.blockNumber) : that.blockNumber != null) return false;
        if (transactionIndex != null ? !transactionIndex.equals(that.transactionIndex) : that.transactionIndex != null)
            return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (gasPrice != null ? !gasPrice.equals(that.gasPrice) : that.gasPrice != null) return false;
        if (gas != null ? !gas.equals(that.gas) : that.gas != null) return false;
        if (input != null ? !input.equals(that.input) : that.input != null) return false;
        if (creates != null ? !creates.equals(that.creates) : that.creates != null) return false;
        return signature != null ? signature.equals(that.signature) : that.signature == null;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        return result;
    }
}
