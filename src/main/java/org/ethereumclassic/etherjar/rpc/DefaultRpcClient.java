package org.ethereumclassic.etherjar.rpc;

import org.apache.http.util.Asserts;
import org.ethereumclassic.etherjar.model.*;
import org.ethereumclassic.etherjar.rpc.json.*;
import org.ethereumclassic.etherjar.rpc.transport.RpcTransport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;

import static org.apache.http.util.Asserts.check;

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
    public EthCommands eth() {
        return new EthCommandsImpl(transport, extractor);
    }
    @Override
    public TraceCommands trace() {
        return new TraceCommandsImpl(transport, extractor);
    }

    public static class EthCommandsImpl implements EthCommands {

        private final RpcTransport transport;
        private Extractor extractor;

        public EthCommandsImpl(RpcTransport transport, Extractor extractor) {
            this.transport = transport;
            this.extractor = extractor;
        }

        @Override
        public Future<Long> getBlockNumber() throws IOException {
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
        public Future<Wei> getBalance(Address address, long block) throws IOException {
            Future<String> resp = transport.execute("eth_getBalance",
                Arrays.asList(address.toHex(), HexQuantity.from(block).toHex()),
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
        public Future<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) throws IOException {
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
        public Future<TransactionJson> getTransaction(BlockHash block, long index) throws IOException {
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

        @Override
        public Future<Long> getTransactionCount(Address address, BlockTag block) throws IOException {
            Future<String> resp = transport.execute("eth_getTransactionCount",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Long> getTransactionCount(Address address, long block) throws IOException {
            Future<String> resp = transport.execute("eth_getTransactionCount",
                Arrays.asList(address.toHex(), HexQuantity.from(block).toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Long> getBlockTransactionCount(BlockHash block) throws IOException {
            Future<String> resp = transport.execute("eth_getBlockTransactionCountByHash",
                Collections.singletonList(block.toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Long> getBlockTransactionCount(long block) throws IOException {
            Future<String> resp = transport.execute("eth_getBlockTransactionCountByNumber",
                Collections.singletonList(HexQuantity.from(block).toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Long> getUncleCount(BlockHash block) throws IOException {
            Future<String> resp = transport.execute("eth_getUncleCountByBlockHash",
                Collections.singletonList(block.toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<Long> getUncleCount(long block) throws IOException {
            Future<String> resp = transport.execute("eth_getUncleCountByBlockNumber",
                Collections.singletonList(HexQuantity.from(block).toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public Future<BlockJson> getUncle(BlockHash block, long index) throws IOException {
            Future<BlockJson> resp = transport.execute("eth_getUncleByBlockHashAndIndex",
                Arrays.asList(block.toHex(), HexQuantity.from(index).toHex()),
                BlockJson.class);
            return resp;
        }

        @Override
        public Future<BlockJson> getUncle(long block, long index) throws IOException {
            Future<BlockJson> resp = transport.execute("eth_getUncleByBlockNumberAndIndex",
                Arrays.asList(HexQuantity.from(block).toHex(), HexQuantity.from(index).toHex()),
                BlockJson.class);
            return resp;
        }

        @Override
        public Future<HexData> getCode(Address address, long block) throws IOException {
            Future<String> resp = transport.execute("eth_getCode",
                Arrays.asList(address.toHex(), HexQuantity.from(block).toHex()),
                String.class);
            return extractor.extractData(resp);
        }

        @Override
        public Future<HexData> getCode(Address address, BlockTag block) throws IOException {
            Future<String> resp = transport.execute("eth_getCode",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return extractor.extractData(resp);
        }

        @Override
        public Future<HexData[]> getWork() throws IOException {
            Future<HexData[]> resp = transport.execute("eth_getWork",
                    Collections.emptyList(),
                    HexData[].class);
            return resp;
        }

        @Override
        public Future<Boolean> submitWork(HexData nonce, HexData powHash, HexData digest) throws IOException {
            check(nonce.getBytes().length == 8, "Nonce should be 8 bytes long");
            check(powHash.getBytes().length == 32, "PowHash should be 32 bytes long");
            check(digest.getBytes().length == 32, "Digest should be 32 bytes long");
            Future<Boolean> resp = transport.execute("eth_submitWork",
                    Arrays.asList(nonce.toHex(), powHash.toHex(), digest.toHex()),
                    Boolean.class);
            return resp;
        }

        @Override
        public Future<Boolean> submitHashrate(HexData hashrate, HexData id) throws IOException {
            check(hashrate.getBytes().length == 32, "Hashrate should be 32 bytes long");
            check(id.getBytes().length == 32, "ID should be 32 bytes long");
            Future<Boolean> resp = transport.execute("eth_submitHashrate",
                    Arrays.asList(hashrate.toHex(), id.toHex()),
                    Boolean.class);
            return resp;
        }

    }

    public static class TraceCommandsImpl implements TraceCommands {

        private final RpcTransport transport;
        private Extractor extractor;

        public TraceCommandsImpl(RpcTransport transport, Extractor extractor) {
            this.transport = transport;
            this.extractor = extractor;
        }

        @Override
        public Future<TraceList> getTransaction(TransactionId hash) throws IOException {
            Future<TraceList> resp = transport.execute("trace_transaction",
                Collections.singletonList(hash.toHex()),
                TraceList.class);
            return resp;
        }
    }
}
