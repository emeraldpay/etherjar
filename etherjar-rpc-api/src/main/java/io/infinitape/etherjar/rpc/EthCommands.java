/*
 * Copyright (c) 2016-2019 Igor Artamonov, All Rights Reserved.
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
package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.domain.*;
import io.infinitape.etherjar.hex.Hex32;
import io.infinitape.etherjar.hex.HexData;
import io.infinitape.etherjar.hex.HexQuantity;
import io.infinitape.etherjar.rpc.json.*;

public class EthCommands {

    private final Class<BlockJson<TransactionJson>> blockWithTxJson = null;
    private final Class<BlockJson<TransactionId>> blockWithTxId = null;


    /**
     *
     * @return number of most recent block.
     */
    public RpcCall<String, Long> getBlockNumber() {
        return RpcCall.create("eth_blockNumber").converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param address address
     * @param block block to check
     * @return balance of the account of given address.
     */
    public RpcCall<String, Wei> getBalance(Address address, BlockTag block) {
        return RpcCall.create("eth_getBalance", address.toHex(), block.getCode()).converted(Wei.class, Wei::from);
    }

    /**
     *
     * @param address address
     * @param block block to check
     * @return balance of the account of given address.
     */
    public RpcCall<String, Wei> getBalance(Address address, long block) {
        return RpcCall.create("eth_getBalance", address.toHex(), HexQuantity.from(block).toHex()).converted(Wei.class, Wei::from);
    }

    /**
     *
     * @param blockNumber block number
     * @return information about a block
     */
    public RpcCall<BlockJson<TransactionId>, BlockJson<TransactionId>> getBlock(long blockNumber) {
        RpcCall call = RpcCall.create("eth_getBlockByNumber", blockWithTxId, HexQuantity.from(blockNumber).toHex(), false).withJsonType(BlockJson.class);
        call.setResultType(BlockJson.class);
        return call;
    }


    /**
     *
     * @param blockNumber block number
     * @return information about a block
     */
    public RpcCall<BlockJson<TransactionJson>, BlockJson<TransactionJson>> getBlockWithTransactions(long blockNumber) {
        RpcCall call = RpcCall.create("eth_getBlockByNumber", blockWithTxJson, HexQuantity.from(blockNumber).toHex(), true).withJsonType(BlockJson.class);
        call.setResultType(BlockJson.class);
        return call;
    }

    /**
     *
     * @param hash block hash
     * @return information about a block
     */
    public RpcCall<BlockJson<TransactionId>, BlockJson<TransactionId>> getBlock(BlockHash hash) {
        RpcCall call = RpcCall.create("eth_getBlockByHash", blockWithTxId, hash.toHex(), false).withJsonType(BlockJson.class);
        call.setResultType(BlockJson.class);
        return call;
    }

    /**
     *
     * @param hash block hash
     * @return information about a block
     */
    public RpcCall<BlockJson<TransactionJson>, BlockJson<TransactionJson>> getBlockWithTransactions(BlockHash hash) {
        RpcCall call = RpcCall.create("eth_getBlockByHash", blockWithTxJson, hash.toHex(), true).withJsonType(BlockJson.class);
        call.setResultType(BlockJson.class);
        return call;
    }

    /**
     *
     * @param hash keystore hash
     * @return information about a transaction
     */
    public RpcCall<TransactionJson, TransactionJson> getTransaction(TransactionId hash) {
        return RpcCall.create("eth_getTransactionByHash", TransactionJson.class, hash.toHex());
    }

    /**
     *
     * @param block block hash
     * @param index keystore index in the block
     * @return information about a transaction
     */
    public RpcCall<TransactionJson, TransactionJson> getTransaction(BlockHash block, long index) {
        return RpcCall.create("eth_getTransactionByBlockHashAndIndex", TransactionJson.class, block.toHex(), HexQuantity.from(index).toHex());
    }

    /**
     *
     * @param block block number
     * @param index keystore index in the block
     * @return information about a transaction
     */

    public RpcCall<TransactionJson, TransactionJson> getTransaction(long block, long index) {
        return RpcCall.create("eth_getTransactionByBlockNumberAndIndex", TransactionJson.class, HexQuantity.from(block).toHex(), HexQuantity.from(index).toHex());
    }

    /**
     *
     * @param hash transaction hash
     * @return receipt of a transaction
     */
    public RpcCall<TransactionReceiptJson, TransactionReceiptJson> getTransactionReceipt(TransactionId hash) {
        return RpcCall.create("eth_getTransactionReceipt", TransactionReceiptJson.class, hash.toHex());
    }

    /**
     * Returns the number of transactions (nonce) sent from an address.
     * @param address address to check
     * @param block block to check
     * @return nonce value
     */
    public RpcCall<String, Long> getTransactionCount(Address address, BlockTag block) {
        return RpcCall.create("eth_getTransactionCount", address.toHex(), block.getCode()).converted(Long.class, Conversion.asLong);
    }

    /**
     * Returns the number of transactions sent from an address.
     * @param address address to check
     * @param block block to check
     * @return nonce value
     */
    public RpcCall<String, Long> getTransactionCount(Address address, long block) {
        return RpcCall.create("eth_getTransactionCount", address.toHex(), HexQuantity.from(block).toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     * Returns the number of transactions sent from an address.
     * @param address address to check
     * @return nonce value
     */
    public RpcCall<String, Long> getTransactionCount(Address address) {
        return RpcCall.create("eth_getTransactionCount", address.toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param block block hash
     * @return number of transactions in a block from a block matching the given block
     */
    public RpcCall<String, Long> getBlockTransactionCount(BlockHash block) {
        return RpcCall.create("eth_getBlockTransactionCountByHash", block.toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param block block height
     * @return number of transactions in a block from a block matching the given block
     */
    public RpcCall<String, Long> getBlockTransactionCount(long block) {
        return RpcCall.create("eth_getBlockTransactionCountByNumber", HexQuantity.from(block).toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param block block hash
     * @return number of uncles in a block from a block matching the given block hash.
     */
    public RpcCall<String, Long> getUncleCount(BlockHash block) {
        return RpcCall.create("eth_getUncleCountByBlockHash", block.toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param block
     * @return number of uncles in a block from a block matching the given block number.
     */
    public RpcCall<String, Long> getUncleCount(long block) {
        return RpcCall.create("eth_getUncleCountByBlockNumber", HexQuantity.from(block).toHex()).converted(Long.class, Conversion.asLong);
    }

    /**
     *
     * @param block block hash
     * @param index uncle index
     * @return uncle block
     */
    public RpcCall<BlockJson, BlockJson> getUncle(BlockHash block, long index) {
        return RpcCall.create("eth_getUncleByBlockHashAndIndex", BlockJson.class, block.toHex(), HexQuantity.from(index).toHex());
    }

    /**
     *
     * @param block block number
     * @param index uncle index
     * @return uncle block
     */
    public RpcCall<BlockJson, BlockJson> getUncle(long block, long index) {
        return RpcCall.create("eth_getUncleByBlockNumberAndIndex", BlockJson.class, HexQuantity.from(block).toHex(), HexQuantity.from(index).toHex());
    }

    /**
     *
     * @param address address
     * @param block block number
     * @return code at a given address
     */
    public RpcCall<String, HexData> getCode(Address address, long block) {
        return RpcCall.create("eth_getCode", address.toHex(), HexQuantity.from(block).toHex()).converted(HexData.class, HexData::from);
    }

    /**
     *
     * @param address address
     * @param block block tag
     * @return code at a given address
     */
    public RpcCall<String, HexData> getCode(Address address, BlockTag block) {
        return RpcCall.create("eth_getCode", address.toHex(), block.getCode()).converted(HexData.class, HexData::from);
    }

    /**
     *
     * @return the hash of the current block, the seedHash, and the boundary condition to be met ("target").
     */
    public RpcCall<String[], HexData[]> getWork() {
        return RpcCall.create("eth_getWork", String[].class).converted(HexData[].class, Conversion.asHexArray);
    }

    /**
     * Used for submitting a proof-of-work solution.
     * @param nonce 8 Bytes - The nonce found (64 bits)
     * @param powHash 32 Bytes - The header's pow-hash (256 bits)
     * @param digest 32 Bytes - The mix digest (256 bits)
     * @return true if the provided solution is valid, otherwise false.
     */
    public RpcCall<Boolean, Boolean> submitWork(Nonce nonce, Hex32 powHash, Hex32 digest) {
        return RpcCall.create("eth_submitWork", Boolean.class, nonce.toHex(), powHash.toHex(), digest.toHex());
    }

    /**
     * Used for submitting mining hashrate.
     * @param hashrate a hexadecimal string representation (32 bytes) of the hash rate
     * @param id  A random hexadecimal(32 bytes) ID identifying the client
     * @return true if submitting went through succesfully and false otherwise.
     */
    public RpcCall<Boolean, Boolean> submitHashrate(Hex32 hashrate, Hex32 id) {
        return RpcCall.create("eth_submitHashrate", Boolean.class, hashrate.toHex(), id.toHex());
    }

    /**
     * Returns the client getCoinbase address.
     * @return getCoinbase address
     */
    public RpcCall<String, Address> getCoinbase() {
        return RpcCall.create("eth_coinbase").converted(Address.class, Address::from);
    }

    /**
     * @return the number of hashes per second that the node is mining with.
     */
    public RpcCall<String, Long> getHashrate() {
        return RpcCall.create("eth_hashrate").converted(Long.class, Conversion.asLong);
    }

    /**
     * @return true if client is actively mining new blocks.
     */
    public RpcCall<Boolean, Boolean> isMining() {
        return RpcCall.create("eth_mining", Boolean.class);
    }

    /**
     * @return the current price per gas in wei.
     */
    public RpcCall<String, Wei> getGasPrice() {
        return RpcCall.create("eth_gasPrice").converted(Wei.class, Wei::from);
    }

    /**
     * @return a list of addresses owned by client.
     */
    public RpcCall<String[], Address[]> getAccounts() {
        return RpcCall.create("eth_accounts", String[].class).converted(Address[].class, Conversion.asAddressArray);
    }

    /**
     * @return a chainId for EIP155
     */
    public RpcCall<String, Long> getChainId() {
        return RpcCall.create("eth_chainId").converted(Long.class, Conversion.asLong);
    }

    /**
     * Executes a new message call immediately without creating a transaction on the block chain.
     *
     * @param call the transaction call object
     * @param block target block
     * @return return value of executed contract
     */
    public RpcCall<String, HexData> call(TransactionCallJson call, BlockTag block) {
        return RpcCall.create("eth_call", call, block.getCode()).converted(HexData.class, HexData::from);
    }

    /**
     * Creates new message call transaction or a contract creation, if the data.data field contains code.
     *
     * @param data transaction object
     * @return transaction id
     */
    public RpcCall<String, TransactionId> sendTransaction(TransactionCallJson data) {
        return RpcCall.create("eth_sendTransaction", String.class, data).converted(TransactionId.class, TransactionId::from);
    }

    /**
     * Creates new message call transaction or a contract creation for signed transactions
     *
     * @param raw signed transaction data
     * @return transaction id
     */
    public RpcCall<String, TransactionId> sendTransaction(HexData raw) {
        return RpcCall.create("eth_sendRawTransaction", String.class, raw.toHex()).converted(TransactionId.class, TransactionId::from);
    }

    /**
     * Signs data with a given address, as sign(keccak256("Ethereum Signed Message: " + len(message) + message)))
     * Note: the address to sign must be unlocked.
     *
     * @param signer signer address
     * @param hash sha3 hash of data to sign
     * @return signature
     */
    public RpcCall<String, HexData> sign(Address signer, HexData hash) {
        return RpcCall.create("eth_sign", signer.toHex(), hash.toHex()).converted(HexData.class, HexData::from);
    }

    /**
     * Checks the syncing status of the node
     *
     * @return syncing status (false or information about current progress if true)
     */
    public RpcCall<SyncingJson, SyncingJson> syncing() {
        return RpcCall.create("eth_syncing", SyncingJson.class);
    }

}
