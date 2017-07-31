package io.infinitape.etherjar.rpc;

import io.infinitape.etherjar.core.*;
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
         * @throws IOException
         */
        public CompletableFuture<Long> getBlockNumber() throws IOException;

        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         * @throws IOException
         */
        public CompletableFuture<Wei> getBalance(Address address, BlockTag block) throws IOException;

        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         * @throws IOException
         */
        public CompletableFuture<Wei> getBalance(Address address, long block) throws IOException;

        /**
         *
         * @param blockNumber block number
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         * @throws IOException
         */
        public CompletableFuture<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException;

        /**
         *
         * @param hash block hash
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         * @throws IOException
         */
        public CompletableFuture<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) throws IOException;

        /**
         *
         * @param hash tx hash
         * @return information about a transaction
         * @throws IOException
         */
        public CompletableFuture<TransactionJson> getTransaction(TransactionId hash) throws IOException;

        /**
         *
         * @param block block hash
         * @param index tx index in the block
         * @return information about a transaction
         * @throws IOException
         */
        public CompletableFuture<TransactionJson> getTransaction(BlockHash block, long index) throws IOException;;

        /**
         *
         * @param block block number
         * @param index tx index in the block
         * @return information about a transaction
         * @throws IOException
         */

        public CompletableFuture<TransactionJson> getTransaction(long block, long index) throws IOException;;

        /**
         *
         * @param hash transaction hash
         * @return receipt of a transaction
         * @throws IOException
         */
        public CompletableFuture<TransactionReceiptJson> getTransactionReceipt(TransactionId hash) throws IOException;

        /**
         * Returns the number of transactions sent from an address.
         * @param address
         * @param block
         * @return
         * @throws IOException
         */
        public CompletableFuture<Long> getTransactionCount(Address address, BlockTag block) throws IOException;

        /**
         * Returns the number of transactions sent from an address.
         * @param address
         * @param block
         * @return
         * @throws IOException
         */
        public CompletableFuture<Long> getTransactionCount(Address address, long block) throws IOException;

        /**
         *
         * @param block block hash
         * @return number of transactions in a block from a block matching the given block
         * @throws IOException
         */
        public CompletableFuture<Long> getBlockTransactionCount(BlockHash block) throws IOException;

        /**
         *
         * @param block block height
         * @return number of transactions in a block from a block matching the given block
         * @throws IOException
         */
        public CompletableFuture<Long> getBlockTransactionCount(long block) throws IOException;

        /**
         *
         * @param block block hash
         * @return number of uncles in a block from a block matching the given block hash.
         * @throws IOException
         */
        public CompletableFuture<Long> getUncleCount(BlockHash block) throws IOException;

        /**
         *
         * @param block
         * @return number of uncles in a block from a block matching the given block number.
         * @throws IOException
         */
        public CompletableFuture<Long> getUncleCount(long block) throws IOException;

        /**
         *
         * @param block block hash
         * @param index uncle index
         * @return uncle block
         * @throws IOException
         */
        public CompletableFuture<BlockJson> getUncle(BlockHash block, long index) throws IOException;

        /**
         *
         * @param block block number
         * @param index uncle index
         * @return uncle block
         * @throws IOException
         */
        public CompletableFuture<BlockJson> getUncle(long block, long index) throws IOException;

        /**
         *
         * @param address address
         * @param block block number
         * @return code at a given address
         * @throws IOException
         */
        public CompletableFuture<HexData> getCode(Address address, long block) throws IOException;

        /**
         *
         * @param address address
         * @param block block tag
         * @return code at a given address
         * @throws IOException
         */
        public CompletableFuture<HexData> getCode(Address address, BlockTag block) throws IOException;

        /**
         *
         * @return the hash of the current block, the seedHash, and the boundary condition to be met ("target").
         * @throws IOException
         */
        public CompletableFuture<HexData[]> getWork() throws IOException;

        /**
         * Used for submitting a proof-of-work solution.
         * @param nonce 8 Bytes - The nonce found (64 bits)
         * @param powHash 32 Bytes - The header's pow-hash (256 bits)
         * @param digest 32 Bytes - The mix digest (256 bits)
         * @return true if the provided solution is valid, otherwise false.
         * @throws IOException
         */
        public CompletableFuture<Boolean> submitWork(Nonce nonce, Hex32 powHash, Hex32 digest) throws IOException;

        /**
         * Used for submitting mining hashrate.
         * @param hashrate a hexadecimal string representation (32 bytes) of the hash rate
         * @param id  A random hexadecimal(32 bytes) ID identifying the client
         * @return true if submitting went through succesfully and false otherwise.
         * @throws IOException
         */
        public CompletableFuture<Boolean> submitHashrate(Hex32 hashrate, Hex32 id) throws IOException;

        /**
         * Returns the client getCoinbase address.
         * @return getCoinbase address
         * @throws IOException
         */
        public CompletableFuture<Address> getCoinbase() throws IOException;

        /**
         * @return the number of hashes per second that the node is mining with.
         * @throws IOException
         */
        public CompletableFuture<Long> getHashrate() throws IOException;

        /**
         * @return true if client is actively mining new blocks.
         * @throws IOException
         */
        public CompletableFuture<Boolean> isMining() throws IOException;

        /**
         * @return the current price per gas in wei.
         * @throws IOException
         */
        public CompletableFuture<Long> getGasPrice() throws IOException;

        /**
         * @return a list of addresses owned by client.
         * @throws IOException
         */
        public CompletableFuture<Address[]> getAccounts() throws IOException;

        /**
         * @return a list of available compilers in the client.
         * @throws IOException
         */
        public CompletableFuture<String[]> getCompilers() throws IOException;

        /**
         * Executes a new message call immediately without creating a transaction on the block chain.
         *
         * @param call the transaction call object
         * @param block target block
         * @return return value of executed contract
         * @throws IOException
         */
        public CompletableFuture<HexData> call(TransactionCallJson call, BlockTag block) throws IOException;

        /**
         * Creates new message call transaction or a contract creation, if the data.data field contains code.
         *
         * @param data transaction object
         * @return transaction id
         * @throws IOException
         */
        public CompletableFuture<TransactionId> sendTransaction(TransactionCallJson data) throws IOException;

        /**
         * Creates new message call transaction or a contract creation for signed transactions
         *
         * @param raw signed transaction data
         * @return transaction id
         * @throws IOException
         */
        public CompletableFuture<TransactionId> sendTransaction(HexData raw) throws IOException;

        /**
         * Signs data with a given address.
         * Note: the address to sign must be unlocked.
         *
         * @param signer signer address
         * @param hash sha3 hash of data to sign
         * @return signature
         * @throws IOException
         */
        public CompletableFuture<HexData> sign(Address signer, HexData hash) throws IOException;
    }

    interface TraceCommands {

        CompletableFuture<TraceList> getTransaction(TransactionId hash) throws IOException;

    }
}
