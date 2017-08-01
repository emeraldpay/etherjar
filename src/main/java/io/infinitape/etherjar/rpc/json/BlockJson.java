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
import io.infinitape.etherjar.core.Address;
import io.infinitape.etherjar.core.BlockHash;
import io.infinitape.etherjar.core.HexData;
import io.infinitape.etherjar.core.TransactionId;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@JsonDeserialize(using = BlockJsonDeserializer.class)
public class BlockJson<T> {

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
    private TransactionId transactionsRoot;

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
    private BigInteger gasLimit;

    /**
     * the total used gas by all transactions in this block.
     */
    private BigInteger gasUsed;

    /**
     * when the block was collated
     */
    private Date timestamp;

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

    public TransactionId getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(TransactionId transactionsRoot) {
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

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
}
