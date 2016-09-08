package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.TransactionId;
import org.ethereumclassic.etherjar.model.Wei;
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

//    public void getTransactionCount();
//
//    public void getBlockTransactionCountByHash();
//
//    public void getBlockTransactionCountByNumber();
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
