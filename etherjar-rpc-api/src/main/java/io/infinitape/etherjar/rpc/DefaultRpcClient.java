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
import io.infinitape.etherjar.rpc.transport.BatchStatus;
import io.infinitape.etherjar.rpc.transport.RpcTransport;

import java.util.concurrent.CompletableFuture;

public class DefaultRpcClient implements RpcClient {

    private RpcTransport transport;

    public DefaultRpcClient(RpcTransport transport) {
        this.transport = transport;
    }

    public ExecutableBatch batch() {
        return new ExecutableBatch(this);
    }

    @Override
    public CompletableFuture<BatchStatus> execute(Batch batch) {
        return transport.execute(batch.getItems());
    }

    @Override
    public <RES> CompletableFuture<RES> execute(RpcCall<?, RES> call) {
        ExecutableBatch batch = this.batch();
        CompletableFuture<RES> result = batch.add(call);
        batch.execute();
        return result;
    }

    // DEPRECATED

    @Override
    @Deprecated
    public EthCommands eth() {
        return new EthCommandsImpl(this);
    }
    @Override
    @Deprecated
    public TraceCommands trace() {
        return new TraceCommandsImpl(this);
    }

    @Deprecated
    public static class EthCommandsImpl implements EthCommands {

        private final DefaultRpcClient client;

        public EthCommandsImpl(DefaultRpcClient client) {
            this.client = client;
        }

        @Override
        public CompletableFuture<Long> getBlockNumber() {
            return client.execute(Commands.eth().getBlockNumber());
        }

        @Override
        public CompletableFuture<Wei> getBalance(Address address, BlockTag block) {
            return client.execute(Commands.eth().getBalance(address, block));
        }

        @Override
        public CompletableFuture<Wei> getBalance(Address address, long block) {
            return client.execute(Commands.eth().getBalance(address, block));
        }

        @Override
        public CompletableFuture<BlockJson> getBlock(long blockNumber, boolean includeTransactions) {
            if (includeTransactions) {
                return client.execute(Commands.eth().getBlockWithTransactions(blockNumber)).thenApply((b) -> b);
            }
            return client.execute(Commands.eth().getBlock(blockNumber)).thenApply((b) -> b);
        }

        @Override
        public CompletableFuture<BlockJson> getBlock(BlockHash hash, boolean includeTransactions) {
            if (includeTransactions) {
                return client.execute(Commands.eth().getBlockWithTransactions(hash)).thenApply((b) -> b);
            }
            return client.execute(Commands.eth().getBlock(hash)).thenApply((b) -> b);
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(TransactionId hash) {
            return client.execute(Commands.eth().getTransaction(hash));
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(BlockHash block, long index) {
            return client.execute(Commands.eth().getTransaction(block, index));
        }

        @Override
        public CompletableFuture<TransactionJson> getTransaction(long block, long index) {
            return client.execute(Commands.eth().getTransaction(block, index));
        }

        @Override
        public CompletableFuture<TransactionReceiptJson> getTransactionReceipt(TransactionId hash) {
            return client.execute(Commands.eth().getTransactionReceipt(hash));
        }

        @Override
        public CompletableFuture<Long> getTransactionCount(Address address, BlockTag block) {
            return client.execute(Commands.eth().getTransactionCount(address, block));
        }

        @Override
        public CompletableFuture<Long> getTransactionCount(Address address, long block) {
            return client.execute(Commands.eth().getTransactionCount(address, block));
        }

        @Override
        public CompletableFuture<Long> getBlockTransactionCount(BlockHash block) {
            return client.execute(Commands.eth().getBlockTransactionCount(block));
        }

        @Override
        public CompletableFuture<Long> getBlockTransactionCount(long block) {
            return client.execute(Commands.eth().getBlockTransactionCount(block));
        }

        @Override
        public CompletableFuture<Long> getUncleCount(BlockHash block) {
            return client.execute(Commands.eth().getUncleCount(block));
        }

        @Override
        public CompletableFuture<Long> getUncleCount(long block) {
            return client.execute(Commands.eth().getUncleCount(block));
        }

        @Override
        public CompletableFuture<BlockJson> getUncle(BlockHash block, long index) {
            return client.execute(Commands.eth().getUncle(block, index));
        }

        @Override
        public CompletableFuture<BlockJson> getUncle(long block, long index) {
            return client.execute(Commands.eth().getUncle(block, index));
        }

        @Override
        public CompletableFuture<HexData> getCode(Address address, long block) {
            return client.execute(Commands.eth().getCode(address, block));
        }

        @Override
        public CompletableFuture<HexData> getCode(Address address, BlockTag block) {
            return client.execute(Commands.eth().getCode(address, block));
        }

        @Override
        public CompletableFuture<HexData[]> getWork() {
            return client.execute(Commands.eth().getWork());
        }

        @Override
        public CompletableFuture<Boolean> submitWork(Nonce nonce, Hex32 powHash, Hex32 digest) {
            return client.execute(Commands.eth().submitWork(nonce, powHash, digest));
        }

        @Override
        public CompletableFuture<Boolean> submitHashrate(Hex32 hashrate, Hex32 id) {
            return client.execute(Commands.eth().submitHashrate(hashrate, id));
        }

        @Override
        public CompletableFuture<Address> getCoinbase() {
            return client.execute(Commands.eth().getCoinbase());
        }

        @Override
        public CompletableFuture<Long> getHashrate() {
            return client.execute(Commands.eth().getHashrate());
        }

        @Override
        public CompletableFuture<Boolean> isMining() {
            return client.execute(Commands.eth().isMining());
        }

        @Override
        public CompletableFuture<Wei> getGasPrice() {
            return client.execute(Commands.eth().getGasPrice());
        }

        @Override
        public CompletableFuture<Address[]> getAccounts() {
            return client.execute(Commands.eth().getAccounts());
        }

        @Override
        public CompletableFuture<String[]> getCompilers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<HexData> call(TransactionCallJson call, BlockTag block) {
            return client.execute(Commands.eth().call(call, block));
        }

        @Override
        public CompletableFuture<TransactionId> sendTransaction(TransactionCallJson data) {
            return client.execute(Commands.eth().sendTransaction(data));
        }

        @Override
        public CompletableFuture<TransactionId> sendTransaction(HexData raw) {
            return client.execute(Commands.eth().sendTransaction(raw));
        }

        @Override
        public CompletableFuture<HexData> sign(Address signer, HexData hash) {
            return client.execute(Commands.eth().sign(signer, hash));
        }
    }

    @Deprecated
    public static class TraceCommandsImpl implements TraceCommands {

        private final DefaultRpcClient client;

        public TraceCommandsImpl(DefaultRpcClient client) {
            this.client = client;
        }

        @Override
        public CompletableFuture<TraceList> getTransaction(TransactionId hash) {
            return client.execute(Commands.parity().traceTransaction(hash));
        }
    }
}
