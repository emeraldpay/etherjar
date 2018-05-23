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
import io.infinitape.etherjar.domain.Address;
import io.infinitape.etherjar.domain.BlockHash;
import io.infinitape.etherjar.domain.TransactionId;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@JsonDeserialize(using = TransactionReceiptJsonDeserializer.class)
public class TransactionReceiptJson implements Serializable {

    /**
     * hash of the transaction
     */
    private TransactionId transactionHash;

    /**
     * position in the block
     */
    private Long transactionIndex;

    /**
     * hash of the block where this transaction was in.
     */
    private BlockHash blockHash;

    /**
     * block number where this transaction was in
     */
    private Long blockNumber;

    /**
     * total amount of gas used when this transaction was executed in the block.
     */
    private BigInteger cumulativeGasUsed;

    /**
     * amount of gas used by this specific transaction alone.
     */
    private BigInteger gasUsed;

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

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Long transactionIndex) {
        this.transactionIndex = transactionIndex;
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

    public BigInteger getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(BigInteger cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionReceiptJson)) return false;

        TransactionReceiptJson that = (TransactionReceiptJson) o;

        if (transactionHash != null ? !transactionHash.equals(that.transactionHash) : that.transactionHash != null)
            return false;
        if (transactionIndex != null ? !transactionIndex.equals(that.transactionIndex) : that.transactionIndex != null)
            return false;
        if (blockHash != null ? !blockHash.equals(that.blockHash) : that.blockHash != null) return false;
        if (blockNumber != null ? !blockNumber.equals(that.blockNumber) : that.blockNumber != null) return false;
        if (cumulativeGasUsed != null ? !cumulativeGasUsed.equals(that.cumulativeGasUsed) : that.cumulativeGasUsed != null)
            return false;
        if (gasUsed != null ? !gasUsed.equals(that.gasUsed) : that.gasUsed != null) return false;
        if (contractAddress != null ? !contractAddress.equals(that.contractAddress) : that.contractAddress != null)
            return false;
        return logs != null ? logs.equals(that.logs) : that.logs == null;
    }

    @Override
    public int hashCode() {
        int result = transactionHash != null ? transactionHash.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        return result;
    }
}
