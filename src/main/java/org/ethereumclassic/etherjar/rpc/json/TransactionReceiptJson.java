package org.ethereumclassic.etherjar.rpc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.ethereumclassic.etherjar.model.*;

import java.util.List;

/**
 * @author Igor Artamonov
 */
@JsonDeserialize(using = TransactionReceiptJsonDeserializer.class)
public class TransactionReceiptJson {

    /**
     * hash of the transaction
     */
    private TransactionId transactionHash;

    /**
     * integer of the transactions index position in the block.
     */
    private Integer transactionIndex;

    /**
     * hash of the block where this transaction was in.
     */
    private BlockHash blockHash;

    /**
     * block number where this transaction was in.
     */
    private Integer blockNumber;

    /**
     * total amount of gas used when this transaction was executed in the block.
     */
    private HexQuantity cumulativeGasUsed;

    /**
     * amount of gas used by this specific transaction alone.
     */
    private HexQuantity gasUsed;

    /**
     * The contract address created, if the transaction was a contract creation, otherwise null.
     */
    private Address contractAddress;

    /**
     * Array of log objects, which this transaction generated.
     */
    private List<TransactionLogJson> logs;

    public TransactionId getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(TransactionId transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public HexData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(BlockHash blockHash) {
        this.blockHash = blockHash;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public HexQuantity getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(HexQuantity cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public HexQuantity getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(HexQuantity gasUsed) {
        this.gasUsed = gasUsed;
    }

    public Address getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(Address contractAddress) {
        this.contractAddress = contractAddress;
    }

    public List<TransactionLogJson> getLogs() {
        return logs;
    }

    public void setLogs(List<TransactionLogJson> logs) {
        this.logs = logs;
    }
}
