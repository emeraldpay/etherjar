package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.TransactionId;

import java.util.List;

/**
 * @author Igor Artamonov
 */
@JsonDeserialize(using = TransactionLogJsonDeserializer.class)
public class TransactionLogJson {

    /**
     * true when the log was removed, due to a chain reorganization. false if its a valid log.
     */
    private Boolean removed;

    /**
     * log index position in the block. null when its pending log.
     */
    private Integer logIndex;

    /**
     * transactions index position log was created from. null when its pending log.
     */
    private Integer transactionIndex;

    /**
     * hash of the transactions this log was created from. null when its pending log.
     */
    private TransactionId transactionHash;

    /**
     * hash of the block where this log was in. null when its pending. null when its pending log.
     */
    private HexData blockHash;

    /**
     * the block number where this log was in. null when its pending. null when its pending log.
     */
    private Integer blockNumber;

    /**
     * address from which this log originated.
     */
    private Address address;

    /**
     * contains one or more 32 Bytes non-indexed arguments of the log.
     */
    private HexData data;

    /**
     * Array of 0 to 4 32 Bytes DATA of indexed log arguments.
     *
     * In solidity: The first topic is the hash of the signature of the event (e.g. Deposit(address,bytes32,uint256)),
     * except you declared the event with the anonymous specifier.
     */
    private List<HexData> topics;

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public Integer getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(Integer logIndex) {
        this.logIndex = logIndex;
    }

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public TransactionId getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(TransactionId transactionHash) {
        this.transactionHash = transactionHash;
    }

    public HexData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(HexData blockHash) {
        this.blockHash = blockHash;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public HexData getData() {
        return data;
    }

    public void setData(HexData data) {
        this.data = data;
    }

    public List<HexData> getTopics() {
        return topics;
    }

    public void setTopics(List<HexData> topics) {
        this.topics = topics;
    }
}
