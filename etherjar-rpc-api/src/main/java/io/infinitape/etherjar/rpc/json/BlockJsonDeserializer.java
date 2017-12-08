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

package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.infinitape.etherjar.domain.BlockHash;
import io.infinitape.etherjar.domain.TransactionId;
import io.infinitape.etherjar.hex.HexData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockJsonDeserializer extends EtherJsonDeserializer<BlockJson<?>> {

    private TransactionJsonDeserializer transactionJsonDeserializer = new TransactionJsonDeserializer();

    @Override @SuppressWarnings("unchecked")
    public BlockJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        BlockJson blockJson = new BlockJson();

        JsonNode node = jp.readValueAsTree();
        blockJson.setNumber(getQuantity(node, "number").longValue());
        blockJson.setHash(getBlockHash(node, "hash"));
        blockJson.setTimestamp(new Date(getQuantity(node, "timestamp").longValue() * 1000L));

        if (node.has("transactions")) {
            List txes = new ArrayList();
            for (JsonNode tx: node.get("transactions")) {
                if (tx.isObject()) {
                    txes.add(transactionJsonDeserializer.deserialize(tx));
                } else {
                    txes.add(TransactionId.from(tx.textValue()));
                }
            }
            blockJson.setTransactions(txes);
        }

        blockJson.setParentHash(getBlockHash(node, "parentHash"));
        blockJson.setSha3Uncles(getData(node, "sha3Uncles"));
        blockJson.setMiner(getAddress(node, "miner"));
        blockJson.setDifficulty(getQuantity(node, "difficulty"));
        blockJson.setTotalDifficulty(getQuantity(node, "totalDifficulty"));
        blockJson.setSize(node.get("size").longValue());
        blockJson.setGasLimit(getQuantity(node, "gasLimit"));
        blockJson.setGasUsed(getQuantity(node, "gasUsed"));
        blockJson.setExtraData(getData(node, "extraData"));

        List<HexData> uncles = new ArrayList<>();
        for (JsonNode tx: node.get("uncles")) {
            uncles.add(BlockHash.from(tx.textValue()));
        }
        blockJson.setUncles(uncles);

        return blockJson;
    }

}
