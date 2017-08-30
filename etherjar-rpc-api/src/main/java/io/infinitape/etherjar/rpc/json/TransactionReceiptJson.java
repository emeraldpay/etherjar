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
import io.infinitape.etherjar.tx.TransactionId;

import java.math.BigInteger;
import java.util.List;

@JsonDeserialize(using = TransactionReceiptJsonDeserializer.class)
public class TransactionReceiptJson {

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
}
