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
        public Future<Wei> getBalance(Address address, Long block) throws IOException;

        public Future<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException;
        public Future<BlockJson> getBlock(HexData hash, boolean includeTransactions) throws IOException;

        public Future<TransactionJson> getTransaction(TransactionId hash) throws IOException;
        public Future<TransactionJson> getTransaction(HexData block, long index) throws IOException;;
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
        public Future<Long> getTransactionCount(Address address, Long block) throws IOException;

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
        public Future<Long> getBlockTransactionCount(Long block) throws IOException;

//
//    public void getUncleCountByBlockHash();
//
//    public void getUncleCountByBlockNumber();
//
//    public void getCode();
//
//    public void getUncleByBlockHashAndIndex();
//
//    public void getUncleByBlockNumberAndIndex();



    }
}
