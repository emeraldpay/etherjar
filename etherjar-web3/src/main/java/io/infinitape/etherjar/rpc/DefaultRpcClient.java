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

import io.infinitape.etherjar.core.*;
import io.infinitape.etherjar.hex.HexEncoding;
import io.infinitape.etherjar.rpc.json.*;
import io.infinitape.etherjar.rpc.transport.RpcTransport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

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
        public CompletableFuture<Long> getBlockNumber() throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_blockNumber", Collections.emptyList(), String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Wei> getBalance(Address address, BlockTag block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getBalance",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return resp.thenApply(HexEncoding::fromHex).thenApply(Wei::new);
        }

        @Override
        public CompletableFuture<Wei> getBalance(Address address, long block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getBalance",
                Arrays.asList(address.toHex(), HexEncoding.toHex(block)),
                String.class);
            return resp.thenApply(HexEncoding::fromHex).thenApply(Wei::new);
        }

        @Override
        public CompletableFuture<BlockJson> getBlock(long blockNumber, boolean includeTransactions) throws IOException {
            CompletableFuture<BlockJson> resp = transport.execute("eth_getBlockByNumber",
                Arrays.asList(HexEncoding.toHex(blockNumber), includeTransactions),
                BlockJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) throws IOException {
            CompletableFuture<BlockJson> resp = transport.execute("eth_getBlockByHash",
                Arrays.asList(hash.toHex(), includeTransactions),
                BlockJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(TransactionId hash) throws IOException {
            CompletableFuture<TransactionJson> resp = transport.execute("eth_getTransactionByHash",
                Collections.singletonList(hash.toHex()),
                TransactionJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(BlockHash block, long index) throws IOException {
            CompletableFuture<TransactionJson> resp = transport.execute("eth_getTransactionByBlockHashAndIndex",
                Arrays.asList(block.toHex(), HexEncoding.toHex(index)),
                TransactionJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(long block, long index) throws IOException {
            CompletableFuture<TransactionJson> resp = transport.execute("eth_getTransactionByBlockNumberAndIndex",
                Arrays.asList(HexEncoding.toHex(block), HexEncoding.toHex(index)),
                TransactionJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<TransactionReceiptJson> getTransactionReceipt(TransactionId hash) throws IOException {
            CompletableFuture<TransactionReceiptJson> resp = transport.execute("eth_getTransactionReceipt",
                Collections.singletonList(hash.toHex()),
                TransactionReceiptJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<Long> getTransactionCount(Address address, BlockTag block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getTransactionCount",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Long> getTransactionCount(Address address, long block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getTransactionCount",
                Arrays.asList(address.toHex(), HexEncoding.toHex(block)),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Long> getBlockTransactionCount(BlockHash block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getBlockTransactionCountByHash",
                Collections.singletonList(block.toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Long> getBlockTransactionCount(long block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getBlockTransactionCountByNumber",
                Collections.singletonList(HexEncoding.toHex(block)),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Long> getUncleCount(BlockHash block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getUncleCountByBlockHash",
                Collections.singletonList(block.toHex()),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Long> getUncleCount(long block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getUncleCountByBlockNumber",
                Collections.singletonList(HexEncoding.toHex(block)),
                String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<BlockJson> getUncle(BlockHash block, long index) throws IOException {
            CompletableFuture<BlockJson> resp = transport.execute("eth_getUncleByBlockHashAndIndex",
                Arrays.asList(block.toHex(), HexEncoding.toHex(index)),
                BlockJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<BlockJson> getUncle(long block, long index) throws IOException {
            CompletableFuture<BlockJson> resp = transport.execute("eth_getUncleByBlockNumberAndIndex",
                Arrays.asList(HexEncoding.toHex(block), HexEncoding.toHex(index)),
                BlockJson.class);
            return resp;
        }

        @Override
        public CompletableFuture<HexData> getCode(Address address, long block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getCode",
                Arrays.asList(address.toHex(), HexEncoding.toHex(block)),
                String.class);
            return resp.thenApply(HexData::from);
        }

        @Override
        public CompletableFuture<HexData> getCode(Address address, BlockTag block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_getCode",
                Arrays.asList(address.toHex(), block.getCode()),
                String.class);
            return resp.thenApply(HexData::from);
        }

        @Override
        public CompletableFuture<HexData[]> getWork() throws IOException {
            CompletableFuture<HexData[]> resp = transport.execute("eth_getWork",
                    Collections.emptyList(),
                    HexData[].class);
            return resp;
        }

        @Override
        public CompletableFuture<Boolean> submitWork(Nonce nonce, Hex32 powHash, Hex32 digest) throws IOException {
            CompletableFuture<Boolean> resp = transport.execute("eth_submitWork",
                    Arrays.asList(nonce.toHex(), powHash.toHex(), digest.toHex()),
                    Boolean.class);
            return resp;
        }

        @Override
        public CompletableFuture<Boolean> submitHashrate(Hex32 hashrate, Hex32 id) throws IOException {
            CompletableFuture<Boolean> resp = transport.execute("eth_submitHashrate",
                    Arrays.asList(hashrate.toHex(), id.toHex()),
                    Boolean.class);
            return resp;
        }

        @Override
        public CompletableFuture<Address> getCoinbase() throws IOException {
            CompletableFuture<Address> resp = transport.execute("eth_coinbase",
                    Collections.emptyList(),
                    Address.class);
            return resp;
        }

        @Override
        public CompletableFuture<Long> getHashrate() throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_hashrate",
                    Collections.emptyList(),
                    String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Boolean> isMining() throws IOException {
            CompletableFuture<Boolean> resp = transport.execute("eth_mining",
                    Collections.emptyList(),
                    Boolean.class);
            return resp;
        }

        @Override
        public CompletableFuture<Long> getGasPrice() throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_gasPrice",
                    Collections.emptyList(),
                    String.class);
            return extractor.extractLong(resp);
        }

        @Override
        public CompletableFuture<Address[]> getAccounts() throws IOException {
            CompletableFuture<Address[]> resp = transport.execute("eth_accounts",
                    Collections.emptyList(),
                    Address[].class);
            return resp;
        }

        @Override
        public CompletableFuture<String[]> getCompilers() throws IOException {
            CompletableFuture<String[]> resp = transport.execute("eth_getCompilers",
                    Collections.emptyList(),
                    String[].class);
            return resp;
        }

        @Override
        public CompletableFuture<HexData> call(TransactionCallJson call, BlockTag block) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_call",
                Arrays.asList(call, block.getCode()),
                String.class);
            return resp.thenApply(HexData::from);
        }

        @Override
        public CompletableFuture<TransactionId> sendTransaction(TransactionCallJson data) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_sendTransaction",
                Collections.singletonList(data),
                String.class);
            return resp.thenApply(TransactionId::from);
        }

        @Override
        public CompletableFuture<TransactionId> sendTransaction(HexData raw) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_sendRawTransaction",
                Collections.singletonList(raw.toHex()),
                String.class);
            return resp.thenApply(TransactionId::from);
        }

        @Override
        public CompletableFuture<HexData> sign(Address signer, HexData hash) throws IOException {
            CompletableFuture<String> resp = transport.execute("eth_sign",
                Arrays.asList(signer.toHex(), hash.toHex()),
                String.class);
            return resp.thenApply(HexData::from);
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
        public CompletableFuture<TraceList> getTransaction(TransactionId hash) throws IOException {
            CompletableFuture<TraceList> resp = transport.execute("trace_transaction",
                Collections.singletonList(hash.toHex()),
                TraceList.class);
            return resp;
        }
    }
}
