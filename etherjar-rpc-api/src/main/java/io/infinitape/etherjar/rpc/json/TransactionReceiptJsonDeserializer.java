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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionReceiptJsonDeserializer extends EtherJsonDeserializer<TransactionReceiptJson> {

    private TransactionLogJsonDeserializer transactionLogJsonDeserializer = new TransactionLogJsonDeserializer();

    @Override
    public TransactionReceiptJson deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        TransactionReceiptJson receipt = new TransactionReceiptJson();

        receipt.setBlockHash(getBlockHash(node, "blockHash"));
        receipt.setBlockNumber(getLong(node, "blockNumber"));
        receipt.setContractAddress(getAddress(node, "contractAddress"));
        receipt.setCumulativeGasUsed(getLong(node, "cumulativeGasUsed"));
        receipt.setGasUsed(getLong(node, "gasUsed"));
        receipt.setTransactionHash(getTxHash(node, "transactionHash"));
        receipt.setTransactionIndex(getLong(node, "transactionIndex"));

        List<TransactionLogJson> logs = new ArrayList<>();
        if (node.hasNonNull("logs")) {
            for (JsonNode log: node.get("logs")) {
                logs.add(transactionLogJsonDeserializer.deserialize(log));
            }
        }
        receipt.setLogs(logs);

        return receipt;
    }
}
