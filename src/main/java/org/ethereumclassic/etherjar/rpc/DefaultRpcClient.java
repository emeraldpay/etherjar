package org.ethereumclassic.etherjar.rpc;

import org.ethereumclassic.etherjar.model.*;
import org.ethereumclassic.etherjar.rpc.json.BlockJson;
import org.ethereumclassic.etherjar.rpc.json.BlockTag;
import org.ethereumclassic.etherjar.rpc.json.TransactionJson;
import org.ethereumclassic.etherjar.rpc.json.TransactionReceiptJson;
import org.ethereumclassic.etherjar.rpc.transport.RpcTransport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;

/**
 * @author Igor Artamonov
 */
public class DefaultRpcClient implements RpcClient {

    private RpcTransport transport;
    private Extractor extractor;

    public DefaultRpcClient(RpcTransport transport) {
        this(transport, new Extractor());
    }

    public DefaultRpcClient(RpcTransport transport, Extractor extractor) {
        this.transport = transport;
        this.extractor = extractor;
    }

    @Override
    public NetworkDetails network() {
        return new NetworkDetailsImpl(transport, extractor);
    }

    public static class NetworkDetailsImpl implements NetworkDetails {

        private final RpcTransport transport;
        private Extractor extractor;

        public NetworkDetailsImpl(RpcTransport transport, Extractor extractor) {
            this.transport = transport;
            this.extractor = extractor;
        }

        @Override
        public Future<Long> blockNumber() throws IOException {
            Future<String> resp = transport.execute("eth_blockNumber", Collections.emptyList(), String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Wei> getBalance(Address address, BlockTag block) throws IOException {
            Future<String> resp = transport.execute("eth_getBalance",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return extractor.extractWei(resp);
        }

        @Override
        public Future<Wei> getBalance(Address address, Long block) throws IOException {
            Future<String> resp = transport.execute("eth_getBalance",
                Arrays.asList(address.toHex(), HexQuantity.from(block)),
                String.class);
            return extractor.extractWei(resp);
        }

        @Override
        public Future<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException {
            Future<BlockJson> resp = transport.execute("eth_getBlockByNumber",
                Arrays.asList(HexQuantity.from(blockNumber).toHex(), includeTransactions),
                BlockJson.class);
            return resp;
        }
        @Override
        public Future<BlockJson> getBlock(HexData hash, boolean includeTransactions) throws IOException {
            Future<BlockJson> resp = transport.execute("eth_getBlockByHash",
                Arrays.asList(hash.toHex(), includeTransactions),
                BlockJson.class);
            return resp;
        }

        @Override
        public Future<TransactionJson> getTransaction(TransactionId hash) throws IOException {
            Future<TransactionJson> resp = transport.execute("eth_getTransactionByHash",
                Collections.singletonList(hash.toHex()),
                TransactionJson.class);
            return resp;
        }

        @Override
        public Future<TransactionJson> getTransaction(HexData block, long index) throws IOException {
            Future<TransactionJson> resp = transport.execute("eth_getTransactionByBlockHashAndIndex",
                Arrays.asList(block.toHex(), HexQuantity.from(index).toHex()),
                TransactionJson.class);
            return resp;
        }

        @Override
        public Future<TransactionJson> getTransaction(long block, long index) throws IOException {
            Future<TransactionJson> resp = transport.execute("eth_getTransactionByBlockNumberAndIndex",
                Arrays.asList(HexQuantity.from(block).toHex(), HexQuantity.from(index).toHex()),
                TransactionJson.class);
            return resp;
        }

        @Override
        public Future<TransactionReceiptJson> getTransactionReceipt(TransactionId hash) throws IOException {
            Future<TransactionReceiptJson> resp = transport.execute("eth_getTransactionReceipt",
                Collections.singletonList(hash.toHex()),
                TransactionReceiptJson.class);
            return resp;
        }

    }
}
