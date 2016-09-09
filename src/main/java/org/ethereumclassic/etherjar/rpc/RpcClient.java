package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.*;
import org.ethereumclassic.etherjar.rpc.json.BlockJson;
import org.ethereumclassic.etherjar.rpc.json.BlockTag;
import org.ethereumclassic.etherjar.rpc.json.TransactionJson;
import org.ethereumclassic.etherjar.rpc.json.TransactionReceiptJson;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author Igor Artamonov
 */
public interface RpcClient {

    NetworkDetails network();


    public interface NetworkDetails {

        public Future<Long> blockNumber() throws IOException;

        public Future<Wei> getBalance(Address address, BlockTag block) throws IOException;
        public Future<Wei> getBalance(Address address, long block) throws IOException;

        public Future<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException;
        public Future<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) throws IOException;

        public Future<TransactionJson> getTransaction(TransactionId hash) throws IOException;
        public Future<TransactionJson> getTransaction(BlockHash block, long index) throws IOException;;
        public Future<TransactionJson> getTransaction(long block, long index) throws IOException;;

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
}
