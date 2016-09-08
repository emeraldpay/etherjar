package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.*;

import java.util.Date;
import java.util.List;

/**
 * @author Igor Artamonov
 */
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
    private HexQuantity difficulty;

    /**
     * total difficulty of the chain until this block.
     */
    private HexQuantity totalDifficulty;

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
    private HexQuantity gasLimit;

    /**
     * the total used gas by all transactions in this block.
     */
    private HexQuantity gasUsed;

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
    private List<HexData> uncles;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public HexData getHash() {
        return hash;
    }

    public void setHash(BlockHash hash) {
        this.hash = hash;
    }

    public HexData getParentHash() {
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

    public HexQuantity getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(HexQuantity difficulty) {
        this.difficulty = difficulty;
    }

    public HexQuantity getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(HexQuantity totalDifficulty) {
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

    public HexQuantity getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(HexQuantity gasLimit) {
        this.gasLimit = gasLimit;
    }

    public HexQuantity getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(HexQuantity gasUsed) {
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

    public List<HexData> getUncles() {
        return uncles;
    }

    public void setUncles(List<HexData> uncles) {
        this.uncles = uncles;
    }
}
