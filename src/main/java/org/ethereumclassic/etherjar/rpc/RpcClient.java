package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.Address;
import org.ethereumclassic.etherjar.model.HexData;
import org.ethereumclassic.etherjar.model.Wei;
import org.ethereumclassic.etherjar.rpc.json.BlockJson;
import org.ethereumclassic.etherjar.rpc.json.BlockTag;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author Igor Artamonov
 */
public interface RpcClient {

    NetworkDetails network();


    public interface NetworkDetails {

        public Future<Integer> blockNumber() throws IOException;

        public Future<Wei> getBalance(Address address, BlockTag block) throws IOException;
        public Future<Wei> getBalance(Address address, Integer block) throws IOException;

        public Future<BlockJson> getBlock(int blockNumber, boolean includeTransactions) throws IOException;
        public Future<BlockJson> getBlock(HexData hash, boolean includeTransactions) throws IOException;
//

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
//    public void getTransactionByHash();
//
//    public void getTransactionByBlockHashAndIndex();
//
//    public void getTransactionByBlockNumberAndIndex();
//
//    public void getTransactionReceipt();
//
//    public void getUncleByBlockHashAndIndex();
//
//    public void getUncleByBlockNumberAndIndex();



    }
}
