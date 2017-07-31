package io.infinitape.etherjar.rpc.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import io.infinitape.etherjar.core.*;

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

    protected HexQuantity getQuantity(JsonNode node, String name) {
        return getQuantity(node.get(name));
    }
    protected HexQuantity getQuantity(JsonNode node) {
        if (node instanceof NumericNode) {
            return HexQuantity.from(node.longValue());
        }
        String value = getHexString(node);
        if (value == null) return null;
        return HexQuantity.from(value);
    }

    protected Long getLong(JsonNode node, String name) {
        HexQuantity quantity = getQuantity(node, name);
        if (quantity == null) return null;
        return quantity.getValue().longValue();
    }
    protected Long getLong(JsonNode node) {
        HexQuantity quantity = getQuantity(node);
        if (quantity == null) return null;
        return quantity.getValue().longValue();
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
        return Wei.from(value);
    }

    protected BlockHash getBlockHash(JsonNode node, String name) {
        String value = getHexString(node, name);
        if (value == null) return null;
        return BlockHash.from(value);
    }
}
