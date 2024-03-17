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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.BlockHash;
import io.emeraldpay.etherjar.domain.Bloom;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @see <a href="https://github.com/ethereum/execution-apis/blob/main/src/schemas/block.yaml">https://github.com/ethereum/execution-apis/blob/main/src/schemas/block.yaml</a>
 * @param <T> type of transactions structure (full or ref)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockJson<T extends TransactionRefJson> implements Serializable {

    /**
     * the block number. `null` when it's a pending block.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long number;

    /**
     * hash of the block. `null` when it's a pending block.
     */
    private BlockHash hash;

    /**
     * hash of the parent block.
     */
    private BlockHash parentHash;

    /**
     * SHA3 of the uncles data in the block.
     */
    private Hex32 sha3Uncles;

    /**
     * the bloom filter for the logs of the block. null when its pending block.
     */
    private Bloom logsBloom;

    /**
     * the root of the transaction trie of the block.
     */
    private Hex32 transactionsRoot;

    /**
     * the root of the final state trie of the block.
     */
    private Hex32 stateRoot;

    /**
     * the root of the receipts trie of the block.
     */
    private Hex32 receiptsRoot;

    /**
     * the address of the beneficiary to whom the mining rewards were given.
     */
    private Address miner;

    /**
     * the difficulty for this block.
     */
    @JsonDeserialize(using = BigIntegerDeserializer.class)
    @JsonSerialize(using = BigIntegerSerializer.class)
    private BigInteger difficulty;

    /**
     * total difficulty of the chain until this block.
     */
    @JsonDeserialize(using = BigIntegerDeserializer.class)
    @JsonSerialize(using = BigIntegerSerializer.class)
    private BigInteger totalDifficulty;

    /**
     * the "extra data" field of this block.
     */
    private HexData extraData;

    private Hex32 mixHash;

    /**
     * 8-bytes data
     */
    private HexData nonce;

    /**
     * the size of this block in bytes.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long size;

    /**
     * the maximum gas allowed in this block.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gasLimit;

    /**
     * the total used gas by all transactions in this block.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gasUsed;

    /**
     * when the block was collated
     */
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonSerialize(using = TimestampSerializer.class)
    private Instant timestamp;

    /**
     * List of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.
     * <p>
     * HexData or TransactionJson
     */
    @JsonDeserialize(contentUsing = TransactionRefJsonDeserializer.class)
    @JsonSerialize(contentUsing = TransactionRefJsonSerializer.class)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<T> transactions;

    /**
     * list of uncle hashes.
     */
    private List<BlockHash> uncles;

    /**
     * basefee value for that block, as per EIP-1559
     */
    private Wei baseFeePerGas;

    private Hex32 withdrawalsRoot;

    private List<WithdrawalJson> withdrawals;

    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long blobGasUsed;

    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long excessBlobGas;

    private Hex32 parentBeaconBlockRoot;

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

    public Hex32 getSha3Uncles() {
        return sha3Uncles;
    }

    @JsonSetter
    public void setSha3Uncles(Hex32 sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    public void setSha3Uncles(HexData sha3Uncles) {
        setSha3Uncles(Hex32.from(sha3Uncles));
    }

    public Bloom getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(Bloom logsBloom) {
        this.logsBloom = logsBloom;
    }

    public Hex32 getTransactionsRoot() {
        return transactionsRoot;
    }

    @JsonSetter
    public void setTransactionsRoot(Hex32 transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public void setTransactionsRoot(HexData transactionsRoot) {
        setTransactionsRoot(Hex32.from(transactionsRoot));
    }

    public Hex32 getStateRoot() {
        return stateRoot;
    }

    @JsonSetter
    public void setStateRoot(Hex32 stateRoot) {
        this.stateRoot = stateRoot;
    }

    public void setStateRoot(HexData stateRoot) {
        setStateRoot(Hex32.from(stateRoot));
    }

    public Hex32 getReceiptsRoot() {
        return receiptsRoot;
    }

    @JsonSetter
    public void setReceiptsRoot(Hex32 receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public void setReceiptsRoot(HexData receiptsRoot) {
        setReceiptsRoot(Hex32.from(receiptsRoot));
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

    public Wei getBaseFeePerGas() {
        return baseFeePerGas;
    }

    public void setBaseFeePerGas(Wei baseFeePerGas) {
        this.baseFeePerGas = baseFeePerGas;
    }

    public Hex32 getMixHash() {
        return mixHash;
    }

    public void setMixHash(Hex32 mixHash) {
        this.mixHash = mixHash;
    }

    public HexData getNonce() {
        return nonce;
    }

    public void setNonce(HexData nonce) {
        this.nonce = nonce;
    }

    public Hex32 getWithdrawalsRoot() {
        return withdrawalsRoot;
    }

    public void setWithdrawalsRoot(Hex32 withdrawalsRoot) {
        this.withdrawalsRoot = withdrawalsRoot;
    }

    public List<WithdrawalJson> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<WithdrawalJson> withdrawals) {
        this.withdrawals = withdrawals;
    }

    public Long getBlobGasUsed() {
        return blobGasUsed;
    }

    public void setBlobGasUsed(Long blobGasUsed) {
        this.blobGasUsed = blobGasUsed;
    }

    public Long getExcessBlobGas() {
        return excessBlobGas;
    }

    public void setExcessBlobGas(Long excessBlobGas) {
        this.excessBlobGas = excessBlobGas;
    }

    public Hex32 getParentBeaconBlockRoot() {
        return parentBeaconBlockRoot;
    }

    public void setParentBeaconBlockRoot(Hex32 parentBeaconBlockRoot) {
        this.parentBeaconBlockRoot = parentBeaconBlockRoot;
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
        for (T tx : this.transactions) {
            copy.transactions.add(new TransactionRefJson(tx.getHash()));
        }
        return copy;
    }

    /**
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
        copy.baseFeePerGas = this.baseFeePerGas;
        copy.mixHash = this.mixHash;
        copy.nonce = this.nonce;
        copy.withdrawalsRoot = this.withdrawalsRoot;
        copy.withdrawals = this.withdrawals;
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
        if (!Objects.equals(baseFeePerGas, blockJson.baseFeePerGas)) return false;
        if (!Objects.equals(mixHash, blockJson.mixHash)) return false;
        if (!Objects.equals(nonce, blockJson.nonce)) return false;
        if (!Objects.equals(withdrawalsRoot, blockJson.withdrawalsRoot)) return false;
        if (!Objects.equals(withdrawals, blockJson.withdrawals)) return false;
        return Objects.equals(uncles, blockJson.uncles);
    }

    @Override
    public int hashCode() {
        int result = number != null ? number.hashCode() : 0;
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
