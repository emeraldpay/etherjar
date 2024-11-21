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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.emeraldpay.etherjar.domain.Address;
import io.emeraldpay.etherjar.domain.Wei;
import io.emeraldpay.etherjar.hex.Hex32;
import io.emeraldpay.etherjar.hex.HexData;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionCallJson implements Serializable {

    /**
     * Address the transaction is simulated to have been sent from. Defaults to first account in the local keystore or the <code>0x00..0</code> address if no local accounts are available.
     */
    private Address from;

    /**
     * Address the transaction is sent to.
     */
    private Address to;

    /**
     * Maximum gas allowance for the code execution to avoid infinite loops. Defaults to 2^63 or whatever value the node operator specified via <code>--rpc.gascap</code>.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long gas;

    /**
     * Number of wei to simulate paying for each unit of gas during execution. Defaults to 1 gwei.
     */
    private Wei gasPrice;

    /**
     * Maximum fee per gas the transaction should pay in total. Relevant for type-2 transactions.
     */
    private Wei maxFeePerGas;

    /**
     * Maximum tip per gas that's given directly to the miner. Relevant for type-2 transactions.
     */
    private Wei maxPriorityFeePerGas;

    /**
     * Amount of wei to simulate sending along with the transaction. Defaults to 0.
     */
    private Wei value;

    /**
     * Nonce of sender account.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long nonce;

    /**
     * Binary data to send to the target contract. Generally the 4 byte hash of the method signature followed by the ABI encoded parameters. For details please see the Ethereum Contract ABI. This field was previously called data.
     */
    @JsonAlias({"data"})
    private HexData input;

    /**
     * A list of addresses and storage keys that the transaction plans to access. Used in non-legacy, i.e. type 1 and 2 transactions.
     */
    private List<TransactionJson.Access> accessList;

    /**
     * Transaction only valid on networks with this chain ID. Used in non-legacy, i.e. type 1 and 2 transactions.
     */
    @JsonDeserialize(using = HexLongDeserializer.class)
    @JsonSerialize(using = HexLongSerializer.class)
    private Long chainId;

    /**
     * Max fee per blob gas the transaction should pay in total. Relevant for blob transactions.
     */
    private Wei maxFeePerBlobGas;

    /**
     * Blob versioned hashes that can be accessed from EVM. They will be validated in case blobs also provided. Relevant for blob transactions.
     */
    private List<Hex32> blobVersionedHashes;

    /**
     * EIP-4844 Blobs.
     */
    private List<HexData> blobs;

    /**
     * Commitments to EIP-4844 Blobs. They will be generated if only blobs are present and validated if both blobs and commitments are provided.
     */
    private List<HexData> commitments;

    /**
     * Proofs for EIP-4844 Blobs. They must be provided along with commitments. Else they will be generated.
     */
    private List<HexData> proofs;


    public TransactionCallJson() {
    }

    public TransactionCallJson(Address to, HexData input) {
        this.to = to;
        this.input = input;
    }

    public TransactionCallJson(Address from, Address to, Wei value) {
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public TransactionCallJson(Address from, Address to, Long gas, Wei value, HexData input) {
        this.from = from;
        this.to = to;
        this.gas = gas;
        this.value = value;
        this.input = input;
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

    public Long getGas() {
        return gas;
    }

    public void setGas(Long gas) {
        this.gas = gas;
    }

    public Wei getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(Wei gasPrice) {
        this.gasPrice = gasPrice;
    }

    public Wei getValue() {
        return value;
    }

    public void setValue(Wei value) {
        this.value = value;
    }

    public HexData getInput() {
        return input;
    }

    public void setInput(HexData input) {
        this.input = input;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public Wei getMaxFeePerGas() {
        return maxFeePerGas;
    }

    public void setMaxFeePerGas(Wei maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public Wei getMaxPriorityFeePerGas() {
        return maxPriorityFeePerGas;
    }

    public void setMaxPriorityFeePerGas(Wei maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public List<TransactionJson.Access> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<TransactionJson.Access> accessList) {
        this.accessList = accessList;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public Wei getMaxFeePerBlobGas() {
        return maxFeePerBlobGas;
    }

    public void setMaxFeePerBlobGas(Wei maxFeePerBlobGas) {
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }

    public List<Hex32> getBlobVersionedHashes() {
        return blobVersionedHashes;
    }

    public void setBlobVersionedHashes(List<Hex32> blobVersionedHashes) {
        this.blobVersionedHashes = blobVersionedHashes;
    }

    public List<HexData> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<HexData> blobs) {
        this.blobs = blobs;
    }

    public List<HexData> getCommitments() {
        return commitments;
    }

    public void setCommitments(List<HexData> commitments) {
        this.commitments = commitments;
    }

    public List<HexData> getProofs() {
        return proofs;
    }

    public void setProofs(List<HexData> proofs) {
        this.proofs = proofs;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransactionCallJson that)) return false;
        return Objects.equals(from, that.from)
            && Objects.equals(to, that.to)
            && Objects.equals(gas, that.gas)
            && Objects.equals(gasPrice, that.gasPrice)
            && Objects.equals(maxFeePerGas, that.maxFeePerGas)
            && Objects.equals(maxPriorityFeePerGas, that.maxPriorityFeePerGas)
            && Objects.equals(value, that.value)
            && Objects.equals(nonce, that.nonce)
            && Objects.equals(input, that.input)
            && Objects.equals(accessList, that.accessList)
            && Objects.equals(chainId, that.chainId)
            && Objects.equals(maxFeePerBlobGas, that.maxFeePerBlobGas)
            && Objects.equals(blobVersionedHashes, that.blobVersionedHashes)
            && Objects.equals(blobs, that.blobs)
            && Objects.equals(commitments, that.commitments)
            && Objects.equals(proofs, that.proofs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, gas, gasPrice, maxFeePerGas, maxPriorityFeePerGas, value, nonce, input, accessList, chainId, maxFeePerBlobGas, blobVersionedHashes, blobs, commitments, proofs);
    }
}
