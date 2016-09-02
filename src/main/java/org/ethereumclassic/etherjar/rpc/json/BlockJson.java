package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexNumber;
import org.ethereumclassic.etherjar.model.HexValue;
import org.ethereumclassic.etherjar.model.TransactionId;

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
    private Integer number;

    /**
     * 32 Bytes - hash of the block. null when its pending block.
     */
    private HexValue hash;

    /**
     * 32 Bytes - hash of the parent block.
     */
    private HexValue parentHash;

    /**
     * SHA3 of the uncles data in the block.
     */
    private HexValue sha3Uncles;

    /**
     * the bloom filter for the logs of the block. null when its pending block.
     */
    private HexValue logsBloom;

    /**
     * the root of the transaction trie of the block.
     */
    private TransactionId transactionsRoot;

    /**
     * the root of the final state trie of the block.
     */
    private HexValue stateRoot;

    /**
     * the root of the receipts trie of the block.
     */
    private HexValue receiptsRoot;

    /**
     * the address of the beneficiary to whom the mining rewards were given.
     */
    private Address miner;

    /**
     * the difficulty for this block.
     */
    private HexNumber difficulty;

    /**
     * total difficulty of the chain until this block.
     */
    private HexNumber totalDifficulty;

    /**
     * the "extra data" field of this block.
     */
    private HexValue extraData;

    /**
     * the size of this block in bytes.
     */
    private Long size;

    /**
     * the maximum gas allowed in this block.
     */
    private HexNumber gasLimit;

    /**
     * the total used gas by all transactions in this block.
     */
    private HexNumber gasUsed;

    /**
     * when the block was collated
     */
    private Date timestamp;

    /**
     * List of transaction objects, or 32 Bytes transaction hashes depending on the last given parameter.
     *
     * HexValue or TransactionJson
     */
    private List<T> transactions;

    /**
     * list of uncle hashes.
     */
    private List<HexValue> uncles;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public HexValue getHash() {
        return hash;
    }

    public void setHash(HexValue hash) {
        this.hash = hash;
    }

    public HexValue getParentHash() {
        return parentHash;
    }

    public void setParentHash(HexValue parentHash) {
        this.parentHash = parentHash;
    }

    public HexValue getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(HexValue sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    public HexValue getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(HexValue logsBloom) {
        this.logsBloom = logsBloom;
    }

    public TransactionId getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(TransactionId transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public HexValue getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(HexValue stateRoot) {
        this.stateRoot = stateRoot;
    }

    public HexValue getReceiptsRoot() {
        return receiptsRoot;
    }

    public void setReceiptsRoot(HexValue receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public Address getMiner() {
        return miner;
    }

    public void setMiner(Address miner) {
        this.miner = miner;
    }

    public HexNumber getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(HexNumber difficulty) {
        this.difficulty = difficulty;
    }

    public HexNumber getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(HexNumber totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public HexValue getExtraData() {
        return extraData;
    }

    public void setExtraData(HexValue extraData) {
        this.extraData = extraData;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public HexNumber getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(HexNumber gasLimit) {
        this.gasLimit = gasLimit;
    }

    public HexNumber getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(HexNumber gasUsed) {
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

    public List<HexValue> getUncles() {
        return uncles;
    }

    public void setUncles(List<HexValue> uncles) {
        this.uncles = uncles;
    }
}
