/*
 * Copyright (c) 2020 EmeraldPay Inc, All Rights Reserved.
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

package io.emeraldpay.etherjar.rpc.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.*;
import io.emeraldpay.etherjar.hex.Hex32;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionReceiptJson implements TransactionRef, Serializable {

    /**
     * hash of the transaction
     */
    private TransactionId transactionHash;

    /**
     * position in the block
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long transactionIndex;

    /**
     * hash of the block where this transaction was in.
     */
    private BlockHash blockHash;

    /**
     * block number where this transaction was in
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long blockNumber;

    /**
     * total amount of gas used when this transaction was executed in the block.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long cumulativeGasUsed;

    /**
     * Sender
     */
    private Address from;

    /**
     * Target address
     */
    private Address to;

    /**
     * amount of gas used by this specific transaction alone.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gasUsed;

    /**
     * The contract address created, if the transaction was a contract creation, otherwise null.
     */
    private Address contractAddress;

    /**
     * Array of log objects, which this transaction generated.
     */
    private List<TransactionLogJson> logs;

    private Bloom logsBloom;

    /**
     * Optional tx status. 0 if failed, 1 if successfull
     */
    @JsonDeserialize(using = HexIntDeserializer.class)
    @JsonSerialize(using = HexIntSerializer.class)
    private Integer status;

    /**
     * The actual value per gas deducted from the sender's account. Before EIP-1559, this is equal to the transaction's gas price. After, it is equal to baseFeePerGas + min(maxFeePerGas - baseFeePerGas, maxPriorityFeePerGas).
     */
    private Wei effectiveGasPrice;

    /**
     * The actual value per gas deducted from the sender's account for blob gas. Only specified for blob transactions as defined by EIP-4844.
     */
    private Wei blobGasPrice;

    /**
     * Transaction type
     *
     * @see <a href="https://eips.ethereum.org/EIPS/eip-2718">EIP-2718: Typed Transaction Envelope</a>
     */
    @JsonDeserialize(using = HexIntDeserializer.class)
    @JsonSerialize(using = HexIntSerializer.class)
    private Integer type = 0;

    /**
     * The post-transaction state root. Only specified for transactions included before the Byzantium upgrade.
     */
    private Hex32 root;

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

    public Bloom getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(Bloom logsBloom) {
        this.logsBloom = logsBloom;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Wei getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    public void setEffectiveGasPrice(Wei effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }

    public Wei getBlobGasPrice() {
        return blobGasPrice;
    }

    public void setBlobGasPrice(Wei blobGasPrice) {
        this.blobGasPrice = blobGasPrice;
    }

    public int getType() {
        if (type == null) {
            return 0;
        }
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Hex32 getRoot() {
        return root;
    }

    public void setRoot(Hex32 root) {
        this.root = root;
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
        if (!Objects.equals(from, that.from)) return false;
        if (!Objects.equals(to, that.to)) return false;
        if (!Objects.equals(blockHash, that.blockHash)) return false;
        if (!Objects.equals(blockNumber, that.blockNumber)) return false;
        if (!Objects.equals(cumulativeGasUsed, that.cumulativeGasUsed)) return false;
        if (!Objects.equals(gasUsed, that.gasUsed)) return false;
        if (!Objects.equals(contractAddress, that.contractAddress)) return false;
        if (!Objects.equals(logsBloom, that.logsBloom)) return false;
        return Objects.equals(logs, that.logs);
    }

    @Override
    public int hashCode() {
        int result = transactionHash != null ? transactionHash.hashCode() : 0;
        result = 31 * result + (blockHash != null ? blockHash.hashCode() : 0);
        return result;
    }
}
