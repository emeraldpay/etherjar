package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.*;

/**
 * @author Igor Artamonov
 */
@JsonDeserialize(using = TransactionJsonDeserializer.class)
public class TransactionJson {

    /**
     * hash of the transaction
     */
    private TransactionId hash;

    /**
     * the number of transactions made by the sender prior to this one.
     */
    private Long nonce;

    /**
     * 32 Bytes - hash of the block where this transaction was in. null when its pending.
     */
    private HexValue blockHash;

    /**
     * block number where this transaction was in. null when its pending.
     */
    private Integer blockNumber;

    /**
     * integer of the transactions index position in the block. null when its pending.
     */
    private Integer transactionIndex;

    /**
     * address of the sender.
     */
    private Address from;

    /**
     * address of the receiver. null when its a contract creation transaction.
     */
    private Address to;

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
    private HexNumber gas;

    /**
     * the data send along with the transaction.
     */
    private HexValue input;

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

    public HexValue getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(HexValue blockHash) {
        this.blockHash = blockHash;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Integer transactionIndex) {
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

    public HexNumber getGas() {
        return gas;
    }

    public void setGas(HexNumber gas) {
        this.gas = gas;
    }

    public HexValue getInput() {
        return input;
    }

    public void setInput(HexValue input) {
        this.input = input;
    }
}
