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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import io.infinitape.etherjar.domain.Address;
import io.infinitape.etherjar.domain.BlockHash;
import io.infinitape.etherjar.domain.TransactionId;
import io.infinitape.etherjar.domain.Wei;
import io.infinitape.etherjar.hex.HexData;
import io.infinitape.etherjar.hex.HexEncoding;

import java.math.BigInteger;

/**
 * Utility class for Ethereum RPC JSON deserialization
 */
public abstract class EtherJsonDeserializer<T> extends JsonDeserializer<T> {

    protected String getHexString(JsonNode node) {
        if (node == null) {
            return null;
        }
        String value = node.textValue();
        if (value == null || value.length() == 0 || value.equals("0x")) {
            return null;
        }
        return value;
    }

    protected String getHexString(JsonNode node, String name) {
        return getHexString(node.get(name));
    }

    protected HexData getData(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return HexData.from(value);
    }

    protected BigInteger getQuantity(JsonNode node, String name) {
        return getQuantity(node.get(name));
    }

    protected BigInteger getQuantity(JsonNode node) {
        if (node instanceof NumericNode) {
            return BigInteger.valueOf(node.longValue());
        }
        String value = getHexString(node);
        if (value == null) return null;
        if (!value.startsWith("0x")) {
            return new BigInteger(value, 10);
        }
        return HexEncoding.fromHex(value);
    }

    protected Long getLong(JsonNode node, String name) {
        BigInteger quantity = getQuantity(node, name);
        if (quantity == null) return null;
        return quantity.longValue();
    }

    protected Long getLong(JsonNode node) {
        BigInteger quantity = getQuantity(node);
        if (quantity == null) return null;
        return quantity.longValue();
    }

    protected Address getAddress(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return Address.from(value);
    }

    protected TransactionId getTxHash(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return TransactionId.from(value);
    }

    protected Wei getWei(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return new Wei(HexEncoding.fromHex(value));
    }

    protected BlockHash getBlockHash(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return BlockHash.from(value);
    }
}
