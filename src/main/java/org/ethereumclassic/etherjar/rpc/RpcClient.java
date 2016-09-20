package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.*;
import org.ethereumclassic.etherjar.rpc.json.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Igor Artamonov
 */
public interface RpcClient {

    EthCommands eth();
    TraceCommands trace();

    public interface EthCommands {

        /**
         *
         * @return number of most recent block.
         * @throws IOException
         */
        public Future<Long> getBlockNumber() throws IOException;

        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         * @throws IOException
         */
        public Future<Wei> getBalance(Address address, BlockTag block) throws IOException;
        /**
         *
         * @param address address
         * @param block block to check
         * @return balance of the account of given address.
         * @throws IOException
         */
        public Future<Wei> getBalance(Address address, long block) throws IOException;

        /**
         *
         * @param blockNumber block number
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         * @throws IOException
         */
        public Future<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException;
        /**
         *
         * @param hash block hash
         * @param includeTransactions includes TransactionJson if true, or hashes as HexData only
         * @return information about a block
         * @throws IOException
         */
        public Future<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) throws IOException;

        /**
         *
         * @param hash tx hash
         * @return information about a transaction
         * @throws IOException
         */
        public Future<TransactionJson> getTransaction(TransactionId hash) throws IOException;
        /**
         *
         * @param block block hash
         * @param index tx index in the block
         * @return information about a transaction
         * @throws IOException
         */
        public Future<TransactionJson> getTransaction(BlockHash block, long index) throws IOException;;
        /**
         *
         * @param block block number
         * @param index tx index in the block
         * @return information about a transaction
         * @throws IOException
         */
        public Future<TransactionJson> getTransaction(long block, long index) throws IOException;;

        /**
         *
         * @param hash transaction hash
         * @return receipt of a transaction
         * @throws IOException
         */
        public Future<TransactionReceiptJson> getTransactionReceipt(TransactionId hash) throws IOException;

        /**
         * Returns the number of transactions sent from an address.
         * @param address
         * @param block
         * @return
         * @throws IOException
         */
        public Future<Long> getTransactionCount(Address address, BlockTag block) throws IOException;

        /**
         * Returns the number of transactions sent from an address.
         * @param address
         * @param block
         * @return
         * @throws IOException
         */
        public Future<Long> getTransactionCount(Address address, long block) throws IOException;

        /**
         *
         * @param block block hash
         * @return number of transactions in a block from a block matching the given block
         * @throws IOException
         */
        public Future<Long> getBlockTransactionCount(BlockHash block) throws IOException;

        /**
         *
         * @param block block height
         * @return number of transactions in a block from a block matching the given block
         * @throws IOException
         */
        public Future<Long> getBlockTransactionCount(long block) throws IOException;

        /**
         *
         * @param block block hash
         * @return number of uncles in a block from a block matching the given block hash.
         * @throws IOException
         */
        public Future<Long> getUncleCount(BlockHash block) throws IOException;

        /**
         *
         * @param block
         * @return number of uncles in a block from a block matching the given block number.
         * @throws IOException
         */
        public Future<Long> getUncleCount(long block) throws IOException;

        /**
         *
         * @param block block hash
         * @param index uncle index
         * @return uncle block
         * @throws IOException
         */
        public Future<BlockJson> getUncle(BlockHash block, long index) throws IOException;

        /**
         *
         * @param block block number
         * @param index uncle index
         * @return uncle block
         * @throws IOException
         */
        public Future<BlockJson> getUncle(long block, long index) throws IOException;

        /**
         *
         * @param address address
         * @param block block number
         * @return code at a given address
         * @throws IOException
         */
        public Future<HexData> getCode(Address address, long block) throws IOException;

        /**
         *
         * @param address address
         * @param block block tag
         * @return code at a given address
         * @throws IOException
         */
        public Future<HexData> getCode(Address address, BlockTag block) throws IOException;

    }

    interface TraceCommands {

        Future<TraceList> getTransaction(TransactionId hash) throws IOException;

    }
}
