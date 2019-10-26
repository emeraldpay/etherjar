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
import io.infinitape.etherjar.domain.TransactionRef;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = TransactionReceiptJsonDeserializer.class)
public class TransactionReceiptJson implements TransactionRef, Serializable {

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
    private Long cumulativeGasUsed;

    /**
     * amount of gas used by this specific transaction alone.
     */
    private Long gasUsed;

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

    public Long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(Long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public Long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(Long gasUsed) {
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
    public TransactionId getHash() {
        return transactionHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionReceiptJson)) return false;

        TransactionReceiptJson that = (TransactionReceiptJson) o;

        if (!Objects.equals(transactionHash, that.transactionHash)) return false;
        if (!Objects.equals(transactionIndex, that.transactionIndex)) return false;
        if (!Objects.equals(blockHash, that.blockHash)) return false;
        if (!Objects.equals(blockNumber, that.blockNumber)) return false;
        if (!Objects.equals(cumulativeGasUsed, that.cumulativeGasUsed)) return false;
        if (!Objects.equals(gasUsed, that.gasUsed)) return false;
        if (!Objects.equals(contractAddress, that.contractAddress)) return false;
        return Objects.equals(logs, that.logs);
    }

    @Override
    public int hashCode() {
        int result = transactionHash != null ? transactionHash.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        return result;
    }
}
