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

package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.domain.*;
import io.infinitape.etherjar.hex.Hex32;
import io.infinitape.etherjar.hex.HexData;
import io.infinitape.etherjar.rpc.json.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RpcClient {

    EthCommands eth();
    TraceCommands trace();

    public interface EthCommands {

        /**
         *
         * @return number of most recent block.
         */
        public CompletableFuture<Long> getBlockNumber();

        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         */
        public CompletableFuture<Wei> getBalance(Address address, BlockTag block);

        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         */
        public CompletableFuture<Wei> getBalance(Address address, long block);

        /**
         *
         * @param blockNumber block number
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         */
        public <X> CompletableFuture<BlockJson<X>> getBlock(long blockNumber, boolean includeTransactions);

        /**
         *
         * @param hash block hash
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         */
        public <X> CompletableFuture<BlockJson<X>> getBlock(BlockHash hash, boolean includeTransactions);

        /**
         *
         * @param hash keystore hash
         * @return information about a transaction
         */
        public CompletableFuture<TransactionJson> getTransaction(TransactionId hash);

        /**
         *
         * @param block block hash
         * @param index keystore index in the block
         * @return information about a transaction
         */
        public CompletableFuture<TransactionJson> getTransaction(BlockHash block, long index);;

        /**
         *
         * @param block block number
         * @param index keystore index in the block
         * @return information about a transaction
         */

        public CompletableFuture<TransactionJson> getTransaction(long block, long index);;

        /**
         *
         * @param hash transaction hash
         * @return receipt of a transaction
         */
        public CompletableFuture<TransactionReceiptJson> getTransactionReceipt(TransactionId hash);

        /**
         * Returns the number of transactions (nonce) sent from an address.
         * @param address address to check
         * @param block block to check
         * @return nonce value
         */
        public CompletableFuture<Long> getTransactionCount(Address address, BlockTag block);

        /**
         * Returns the number of transactions sent from an address.
         * @param address address to check
         * @param block block to check
         * @return nonce value
         */
        public CompletableFuture<Long> getTransactionCount(Address address, long block);

        /**
         *
         * @param block block hash
         * @return number of transactions in a block from a block matching the given block
         */
        public CompletableFuture<Long> getBlockTransactionCount(BlockHash block);

        /**
         *
         * @param block block height
         * @return number of transactions in a block from a block matching the given block
         */
        public CompletableFuture<Long> getBlockTransactionCount(long block);

        /**
         *
         * @param block block hash
         * @return number of uncles in a block from a block matching the given block hash.
         */
        public CompletableFuture<Long> getUncleCount(BlockHash block);

        /**
         *
         * @param block
         * @return number of uncles in a block from a block matching the given block number.
         */
        public CompletableFuture<Long> getUncleCount(long block);

        /**
         *
         * @param block block hash
         * @param index uncle index
         * @return uncle block
         */
        public CompletableFuture<BlockJson> getUncle(BlockHash block, long index);

        /**
         *
         * @param block block number
         * @param index uncle index
         * @return uncle block
         */
        public CompletableFuture<BlockJson> getUncle(long block, long index);

        /**
         *
         * @param address address
         * @param block block number
         * @return code at a given address
         */
        public CompletableFuture<HexData> getCode(Address address, long block);

        /**
         *
         * @param address address
         * @param block block tag
         * @return code at a given address
         */
        public CompletableFuture<HexData> getCode(Address address, BlockTag block);

        /**
         *
         * @return the hash of the current block, the seedHash, and the boundary condition to be met ("target").
         */
        public CompletableFuture<HexData[]> getWork();

        /**
         * Used for submitting a proof-of-work solution.
         * @param nonce 8 Bytes - The nonce found (64 bits)
         * @param powHash 32 Bytes - The header's pow-hash (256 bits)
         * @param digest 32 Bytes - The mix digest (256 bits)
         * @return true if the provided solution is valid, otherwise false.
         */
        public CompletableFuture<Boolean> submitWork(Nonce nonce, Hex32 powHash, Hex32 digest);

        /**
         * Used for submitting mining hashrate.
         * @param hashrate a hexadecimal string representation (32 bytes) of the hash rate
         * @param id  A random hexadecimal(32 bytes) ID identifying the client
         * @return true if submitting went through succesfully and false otherwise.
         */
        public CompletableFuture<Boolean> submitHashrate(Hex32 hashrate, Hex32 id);

        /**
         * Returns the client getCoinbase address.
         * @return getCoinbase address
         */
        public CompletableFuture<Address> getCoinbase();

        /**
         * @return the number of hashes per second that the node is mining with.
         */
        public CompletableFuture<Long> getHashrate();

        /**
         * @return true if client is actively mining new blocks.
         */
        public CompletableFuture<Boolean> isMining();

        /**
         * @return the current price per gas in wei.
         */
        public CompletableFuture<Long> getGasPrice();

        /**
         * @return a list of addresses owned by client.
         */
        public CompletableFuture<Address[]> getAccounts();

        /**
         * @return a list of available compilers in the client.
         */
        public CompletableFuture<String[]> getCompilers();

        /**
         * Executes a new message call immediately without creating a transaction on the block chain.
         *
         * @param call the transaction call object
         * @param block target block
         * @return return value of executed contract
         */
        public CompletableFuture<HexData> call(TransactionCallJson call, BlockTag block);

        /**
         * Creates new message call transaction or a contract creation, if the data.data field contains code.
         *
         * @param data transaction object
         * @return transaction id
         */
        public CompletableFuture<TransactionId> sendTransaction(TransactionCallJson data);

        /**
         * Creates new message call transaction or a contract creation for signed transactions
         *
         * @param raw signed transaction data
         * @return transaction id
         */
        public CompletableFuture<TransactionId> sendTransaction(HexData raw);

        /**
         * Signs data with a given address.
         * Note: the address to sign must be unlocked.
         *
         * @param signer signer address
         * @param hash sha3 hash of data to sign
         * @return signature
         */
        public CompletableFuture<HexData> sign(Address signer, HexData hash);
    }

    interface TraceCommands {

        CompletableFuture<TraceList> getTransaction(TransactionId hash);

    }
}
