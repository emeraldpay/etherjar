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
import io.infinitape.etherjar.core.ChainId;
import io.infinitape.etherjar.core.TransactionSignature;

import java.io.IOException;
import java.math.BigInteger;

public class TransactionJsonDeserializer extends EtherJsonDeserializer<TransactionJson> {

    @Override
    public TransactionJson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.readValueAsTree();
        return deserialize(node);
    }

    public TransactionJson deserialize(JsonNode node) {
        TransactionJson tx = new TransactionJson();
        tx.setHash(getTxHash(node, "hash"));
        tx.setNonce(getQuantity(node, "nonce").longValue());
        tx.setBlockHash(getBlockHash(node, "blockHash"));
        BigInteger blockNumber = getQuantity(node, "blockNumber");
        if (blockNumber != null)  {
            tx.setBlockNumber(blockNumber.longValue());
        }
        BigInteger txIndex = getQuantity(node, "transactionIndex");
        if (txIndex != null) {
            tx.setTransactionIndex(txIndex.longValue());
        }
        tx.setFrom(getAddress(node, "from"));
        tx.setTo(getAddress(node, "to"));
        tx.setValue(getWei(node, "value"));
        tx.setGasPrice(getWei(node, "gasPrice"));
        tx.setGas(getQuantity(node, "gas"));
        tx.setInput(getData(node, "input"));

        if (node.has("r") && node.has("v") && node.has("s")) {
            TransactionSignature signature = new TransactionSignature();

            if (node.hasNonNull("networkId")) {
                signature.setChainId(new ChainId(node.get("networkId").intValue()));
            }
            signature.setR(getData(node, "r"));
            signature.setS(getData(node, "s"));
            signature.setV(getLong(node, "v").intValue());
            signature.setPublicKey(getData(node, "publicKey"));

            tx.setSignature(signature);
        }

        return tx;
    }
}
