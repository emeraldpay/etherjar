/*
 * Copyright (c) 2016-2018 Infinitape Inc, All Rights Reserved.
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
package io.emeraldpay.etherjar.rpc.http;

import io.emeraldpay.etherjar.rpc.Commands;
import io.emeraldpay.etherjar.rpc.DefaultBatch;
import io.emeraldpay.etherjar.rpc.FuturesRpcClient;
import io.emeraldpay.etherjar.rpc.UpstreamValidator;
import io.emeraldpay.etherjar.rpc.json.SyncingJson;

import java.util.concurrent.TimeUnit;

/**
 * Basic upstream validator that verifies that upstream host is available (answer RPC requests) and is not in
 * an initial sync mode
 *
 * @author Igor Artamonov
 */
public class BasicUpstreamValidator implements UpstreamValidator {

    private int minPeers = 0;

    public void setMinPeers(int minPeers) {
        if (minPeers < 0) {
            throw new IllegalArgumentException("minPeers can't be less than 0. Provided: " + minPeers);
        }
        this.minPeers = minPeers;
    }

    public boolean validateSyncing(SyncingJson result) {
        return !result.isSyncing();
    }

    public boolean validatePeers(Integer peers) {
        return peers >= minPeers;
    }

    @Override
    public boolean validate(FuturesRpcClient uri) {
        DefaultBatch batch = new DefaultBatch();
        DefaultBatch.FutureBatchItem<?, SyncingJson> onSyncing = batch.add(Commands.eth().syncing());
        DefaultBatch.FutureBatchItem<?, Integer> onPeers = batch.add(Commands.net().peerCount());
        uri.execute(batch);

        try {
            boolean validPeers = validatePeers(onPeers.getResult().get(3, TimeUnit.SECONDS));
            boolean validSync = validateSyncing(onSyncing.getResult().get(3, TimeUnit.SECONDS));
            return validSync && validPeers;
        } catch (Exception e) {
            return false;
        }
    }
}
