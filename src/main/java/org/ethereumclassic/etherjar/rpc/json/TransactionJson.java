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
    private HexData blockHash;

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
    private HexQuantity gas;

    /**
     * the data send along with the transaction.
     */
    private HexData input;

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

    public HexQuantity getGas() {
        return gas;
    }

    public void setGas(HexQuantity gas) {
        this.gas = gas;
    }

    public HexData getInput() {
        return input;
    }

    public void setInput(HexData input) {
        this.input = input;
    }
}
