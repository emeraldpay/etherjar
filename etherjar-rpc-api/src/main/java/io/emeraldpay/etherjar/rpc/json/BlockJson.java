/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.BlockHash;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = BlockJsonDeserializer.class)
@JsonSerialize(using = BlockJsonSerializer.class)
public class BlockJson<T extends TransactionRefJson> implements Serializable {

    //TODO nonce or sealFields

    /**
     * the block number. null when its pending block.
     */
    private Long number;

    /**
     * hash of the block. null when its pending block.
     */
    private BlockHash hash;

    /**
     * hash of the parent block.
     */
    private BlockHash parentHash;

    /**
     * SHA3 of the uncles data in the block.
     */
    private HexData sha3Uncles;

    /**
     * the bloom filter for the logs of the block. null when its pending block.
     */
    private HexData logsBloom;

    /**
     * the root of the transaction trie of the block.
     */
    private HexData transactionsRoot;

    /**
     * the root of the final state trie of the block.
     */
    private HexData stateRoot;

    /**
     * the root of the receipts trie of the block.
     */
    private HexData receiptsRoot;

    /**
     * the address of the beneficiary to whom the mining rewards were given.
     */
    private Address miner;

    /**
     * the difficulty for this block.
     */
    private BigInteger difficulty;

    /**
     * total difficulty of the chain until this block.
     */
    private BigInteger totalDifficulty;

    /**
     * the "extra data" field of this block.
     */
    private HexData extraData;

    /**
     * the size of this block in bytes.
     */
    private Long size;

    /**
     * the maximum gas allowed in this block.
     */
    private Long gasLimit;

    /**
     * the total used gas by all transactions in this block.
     */
    private Long gasUsed;

    /**
     * when the block was collated
     */
    private Instant timestamp;

    /**
     * List of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.
     *
     * HexData or TransactionJson
     */
    private List<T> transactions;

    /**
     * list of uncle hashes.
     */
    private List<BlockHash> uncles;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public BlockHash getHash() {
        return hash;
    }

    public void setHash(BlockHash hash) {
        this.hash = hash;
    }

    public BlockHash getParentHash() {
        return parentHash;
    }

    public void setParentHash(BlockHash parentHash) {
        this.parentHash = parentHash;
    }

    public HexData getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(HexData sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    public HexData getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(HexData logsBloom) {
        this.logsBloom = logsBloom;
    }

    public HexData getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(HexData transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public HexData getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(HexData stateRoot) {
        this.stateRoot = stateRoot;
    }

    public HexData getReceiptsRoot() {
        return receiptsRoot;
    }

    public void setReceiptsRoot(HexData receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public Address getMiner() {
        return miner;
    }

    public void setMiner(Address miner) {
        this.miner = miner;
    }

    public BigInteger getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public HexData getExtraData() {
        return extraData;
    }

    public void setExtraData(HexData extraData) {
        this.extraData = extraData;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<T> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<T> transactions) {
        this.transactions = transactions;
    }

    public List<BlockHash> getUncles() {
        return uncles;
    }

    public void setUncles(List<BlockHash> uncles) {
        this.uncles = uncles;
    }

    /**
     * If this instance is empty or contains only references, then return as is. Otherwise
     * returns a copy of the BlockJson with transactions fields replaced with id references
     *
     * @return BlockJson instance with transactions ids only.
     */
    @SuppressWarnings("unchecked")
    public BlockJson<TransactionRefJson> withoutTransactionDetails() {
        // if empty then type doesn't matter
        if (this.transactions == null || this.transactions.isEmpty()) {
            return (BlockJson<TransactionRefJson>) this;
        }
        // if it's already just a reference
        if (this.transactions.stream().noneMatch((tx) -> tx instanceof TransactionJson)) {
            return (BlockJson<TransactionRefJson>) this;
        }
        BlockJson<TransactionRefJson> copy = (BlockJson<TransactionRefJson>) copy();
        copy.transactions = new ArrayList<>(this.transactions.size());
        for (T tx: this.transactions) {
            copy.transactions.add(new TransactionRefJson(tx.getHash()));
        }
        return copy;
    }

    /**
     *
     * @return copy of the current instance
     */
    public BlockJson<T> copy() {
        BlockJson<T> copy = new BlockJson<>();
        copy.number = this.number;
        copy.hash = this.hash;
        copy.parentHash = this.parentHash;
        copy.sha3Uncles = this.sha3Uncles;
        copy.logsBloom = this.logsBloom;
        copy.transactionsRoot = this.transactionsRoot;
        copy.stateRoot = this.stateRoot;
        copy.receiptsRoot = this.receiptsRoot;
        copy.miner = this.miner;
        copy.difficulty = this.difficulty;
        copy.totalDifficulty = this.totalDifficulty;
        copy.extraData = this.extraData;
        copy.size = this.size;
        copy.gasLimit = this.gasLimit;
        copy.gasUsed = this.gasUsed;
        copy.timestamp = this.timestamp;
        copy.transactions = this.transactions;
        copy.uncles = this.uncles;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockJson)) return false;

        BlockJson<?> blockJson = (BlockJson<?>) o;

        if (!Objects.equals(number, blockJson.number)) return false;
        if (!Objects.equals(hash, blockJson.hash)) return false;
        if (!Objects.equals(parentHash, blockJson.parentHash)) return false;
        if (!Objects.equals(sha3Uncles, blockJson.sha3Uncles)) return false;
        if (!Objects.equals(logsBloom, blockJson.logsBloom)) return false;
        if (!Objects.equals(transactionsRoot, blockJson.transactionsRoot)) return false;
        if (!Objects.equals(stateRoot, blockJson.stateRoot)) return false;
        if (!Objects.equals(receiptsRoot, blockJson.receiptsRoot)) return false;
        if (!Objects.equals(miner, blockJson.miner)) return false;
        if (!Objects.equals(difficulty, blockJson.difficulty)) return false;
        if (!Objects.equals(totalDifficulty, blockJson.totalDifficulty)) return false;
        if (!Objects.equals(extraData, blockJson.extraData)) return false;
        if (!Objects.equals(size, blockJson.size)) return false;
        if (!Objects.equals(gasLimit, blockJson.gasLimit)) return false;
        if (!Objects.equals(gasUsed, blockJson.gasUsed)) return false;
        if (!Objects.equals(timestamp, blockJson.timestamp)) return false;
        if (!Objects.equals(transactions, blockJson.transactions)) return false;
        return Objects.equals(uncles, blockJson.uncles);
    }

    @Override
    public int hashCode() {
        int result = number != null ? number.hashCode() : 0;
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
